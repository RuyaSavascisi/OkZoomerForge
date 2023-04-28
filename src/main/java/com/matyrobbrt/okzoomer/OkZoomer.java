package com.matyrobbrt.okzoomer;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.config.ClientConfig;
import com.matyrobbrt.okzoomer.config.ServerConfig;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.network.packet.AcknowledgeModPacket;
import com.matyrobbrt.okzoomer.network.packet.DisableZoomPacket;
import com.matyrobbrt.okzoomer.network.packet.DisableZoomScrollingPacket;
import com.matyrobbrt.okzoomer.network.packet.ForceClassicModePacket;
import com.matyrobbrt.okzoomer.network.packet.ForceOverlayPacket;
import com.matyrobbrt.okzoomer.network.packet.ForceSpyglassPacket;
import com.matyrobbrt.okzoomer.network.packet.ForceZoomDivisorPacket;
import com.matyrobbrt.okzoomer.network.packet.Packet;
import com.matyrobbrt.okzoomer.network.packet.ResetRestrictionsPacket;
import com.mojang.brigadier.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.command.EnumArgument;

import java.util.function.Function;

@Mod(OkZoomerAPI.MOD_ID)
public class OkZoomer {

    public OkZoomer() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, OkZoomerAPI.MOD_ID + "-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, OkZoomerAPI.MOD_ID + "-sever.toml");

        final var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(OkZoomer::commonSetup);
        modBus.register(ClientConfig.class);
        modBus.register(ServerConfig.class);

        MinecraftForge.EVENT_BUS.addListener(OkZoomer::registerClientCommands);
        MinecraftForge.EVENT_BUS.addListener(OkZoomer::onPlayerLogout);
        MinecraftForge.EVENT_BUS.addListener(OkZoomer::onPlayerLogin);
    }

    static void commonSetup(final FMLCommonSetupEvent event) {
        class PacketRegister {
            int pktIndex = 0;

            <T extends Packet> void register(Class<T> clazz, Function<FriendlyByteBuf, T> decode) {
                OkZoomerNetwork.CHANNEL.messageBuilder(clazz, pktIndex++)
                        .encoder(Packet::encode)
                        .decoder(decode)
                        .consumerMainThread((pkt, sup) -> pkt.handle(sup.get()))
                        .add();
            }
        }

        final var packets = new PacketRegister();
        packets.register(DisableZoomPacket.class, DisableZoomPacket::decode);
        packets.register(DisableZoomScrollingPacket.class, DisableZoomScrollingPacket::decode);
        packets.register(ForceClassicModePacket.class, ForceClassicModePacket::decode);
        packets.register(ForceZoomDivisorPacket.class, ForceZoomDivisorPacket::decode);
        packets.register(AcknowledgeModPacket.class, AcknowledgeModPacket::decode);
        packets.register(ForceSpyglassPacket.class, ForceSpyglassPacket::decode);
        packets.register(ForceOverlayPacket.class, ForceOverlayPacket::decode);
        packets.register(ResetRestrictionsPacket.class, ResetRestrictionsPacket::decode);
    }

    static void registerClientCommands(final RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(OkZoomerAPI.MOD_ID)
            .then(Commands.literal("client")
                .then(Commands.literal("config")
                    .then(Commands.literal("preset")
                            .then(Commands.argument("preset", EnumArgument.enumArgument(ClientConfig.ZoomPresets.class))
                            .executes(context -> {
                                final var preset = context.getArgument("preset", ClientConfig.ZoomPresets.class);
                                ClientConfig.resetToPreset(preset);
                                context.getSource().sendSuccess(Component.translatable("command.okzoomer.client.config_present", Component.literal(preset.toString())
                                        .withStyle(ChatFormatting.AQUA)), false);
                                return Command.SINGLE_SUCCESS;
                            }))))));
    }

    static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
             ServerConfig.sendPacket(player);
        }
    }

    static void onPlayerLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && OkZoomerNetwork.EXISTENCE_CHANNEL.isRemotePresent(player.connection.connection)) {
            OkZoomerNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ResetRestrictionsPacket());
        }
    }
}
