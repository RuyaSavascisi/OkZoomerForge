package com.matyrobbrt.okzoomer;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.config.ClientConfig;
import com.matyrobbrt.okzoomer.config.ServerConfig;
import com.matyrobbrt.okzoomer.network.packet.AcknowledgeModPacket;
import com.matyrobbrt.okzoomer.network.packet.DisableZoomPacket;
import com.matyrobbrt.okzoomer.network.packet.DisableZoomScrollingPacket;
import com.matyrobbrt.okzoomer.network.packet.ExistingPacket;
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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.function.Function;

@Mod(OkZoomerAPI.MOD_ID)
public class OkZoomer {

    public OkZoomer(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, OkZoomerAPI.MOD_ID + "-client.toml");
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, OkZoomerAPI.MOD_ID + "-sever.toml");

        modBus.addListener(OkZoomer::registerPayloads);
        modBus.register(ClientConfig.class);
        modBus.register(ServerConfig.class);

        NeoForge.EVENT_BUS.addListener(OkZoomer::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(OkZoomer::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(OkZoomer::onPlayerLogin);
    }

    static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.0.0");
        class PacketRegister {
            <T extends Packet> void register(CustomPacketPayload.Type<T> clazz, Function<FriendlyByteBuf, T> decode) {
                registrar.playToClient(clazz, StreamCodec.of((buf, pkt) -> pkt.encode(buf), decode::apply), Packet::handle);
            }
        }

        final var packets = new PacketRegister();
        packets.register(DisableZoomPacket.TYPE, DisableZoomPacket::decode);
        packets.register(DisableZoomScrollingPacket.TYPE, DisableZoomScrollingPacket::decode);
        packets.register(ForceClassicModePacket.TYPE, ForceClassicModePacket::decode);
        packets.register(ForceZoomDivisorPacket.TYPE, ForceZoomDivisorPacket::decode);
        packets.register(AcknowledgeModPacket.TYPE, AcknowledgeModPacket::decode);
        packets.register(ForceSpyglassPacket.TYPE, ForceSpyglassPacket::decode);
        packets.register(ForceOverlayPacket.TYPE, ForceOverlayPacket::decode);
        packets.register(ResetRestrictionsPacket.TYPE, ResetRestrictionsPacket::decode);

        registrar.playBidirectional(ExistingPacket.TYPE, StreamCodec.unit(new ExistingPacket()), (pkt, handler) -> {});
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
                                context.getSource().sendSuccess(() -> Component.translatable("command.okzoomer.client.config_present", Component.literal(preset.toString())
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
        if (event.getEntity() instanceof ServerPlayer player && player.connection.hasChannel(ExistingPacket.TYPE)) {
            PacketDistributor.sendToPlayer(player, new ResetRestrictionsPacket());
        }
    }
}
