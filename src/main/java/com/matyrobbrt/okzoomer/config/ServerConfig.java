package com.matyrobbrt.okzoomer.config;

import com.matyrobbrt.okzoomer.network.packet.AcknowledgeModPacket;
import com.matyrobbrt.okzoomer.network.packet.DisableZoomPacket;
import com.matyrobbrt.okzoomer.network.packet.DisableZoomScrollingPacket;
import com.matyrobbrt.okzoomer.network.packet.ExistingPacket;
import com.matyrobbrt.okzoomer.network.packet.ForceClassicModePacket;
import com.matyrobbrt.okzoomer.network.packet.ForceOverlayPacket;
import com.matyrobbrt.okzoomer.network.packet.ForceSpyglassPacket;
import com.matyrobbrt.okzoomer.network.packet.ForceZoomDivisorPacket;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;

public class ServerConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ALLOW_ZOOM;
    public static final ModConfigSpec.BooleanValue DISABLE_ZOOM_SCROLLING;
    public static final ModConfigSpec.BooleanValue FORCE_CLASSIC_MODE;
    public static final ModConfigSpec.DoubleValue MINIMUM_ZOOM_DIVISOR;
    public static final ModConfigSpec.DoubleValue MAXIMUM_ZOOM_DIVISOR;
    public static final ModConfigSpec.EnumValue<ConfigEnums.SpyglassDependency> SPYGLASS_DEPENDENCY;
    public static final ModConfigSpec.EnumValue<ConfigEnums.ZoomOverlays> ZOOM_OVERLAY;

    static {
        final var builder = new ModConfigSpec.Builder();

        ALLOW_ZOOM = builder.comment("If players should be allowed to zoom using OkZoomer.")
                .define("allow_zoom", true);
        DISABLE_ZOOM_SCROLLING = builder.comment("If false, allows players to use the scroll key to adjust zoom.")
                .define("disable_zoom_scrolling", false);
        FORCE_CLASSIC_MODE = builder.comment("If true, players will be forced to use classic mode for zooming.")
                .define("force_classic_mode", false);

        MINIMUM_ZOOM_DIVISOR = builder.comment("The minimum value that players can scroll down.")
                .defineInRange("minimum_zoom_divisor", 1.0D, 0D, Double.MAX_VALUE);
        MAXIMUM_ZOOM_DIVISOR = builder.comment("The maximum value that players can scroll down.")
                .defineInRange("maximum_zoom_divisor", 50.0D, 0D, Double.MAX_VALUE);

        SPYGLASS_DEPENDENCY = builder.comment("Enforce a spyglass dependency for zoom.")
                .defineEnum("spyglass_dependency", ConfigEnums.SpyglassDependency.OFF);
        ZOOM_OVERLAY = builder.comment("Enforce a zoom overlay over the zoom.")
                .defineEnum("zoom_overlay", ConfigEnums.ZoomOverlays.OFF);

        SPEC = builder.build();
    }

    @SubscribeEvent
    static void configChanged(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() != ModConfig.Type.SERVER)
            return;
        ZoomUtils.LOGGER.info("THe OkZoomer server config has been changed!");
        final var currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            currentServer.getPlayerList().getPlayers().forEach(ServerConfig::sendPacket);
        }
    }

    public static void sendPacket(ServerPlayer player) {
        if (player.connection.hasChannel(ExistingPacket.TYPE)) {
            final var packets = new ArrayList<CustomPacketPayload>();
            packets.add(new DisableZoomScrollingPacket(DISABLE_ZOOM_SCROLLING.get()));
            packets.add(new ForceClassicModePacket(FORCE_CLASSIC_MODE.get()));
            packets.add(new ForceZoomDivisorPacket(MINIMUM_ZOOM_DIVISOR.get(), MAXIMUM_ZOOM_DIVISOR.get()));
            packets.add(new ForceSpyglassPacket(SPYGLASS_DEPENDENCY.get()));
            packets.add(new ForceOverlayPacket(ZOOM_OVERLAY.get()));
            packets.add(new AcknowledgeModPacket());
            PacketDistributor.sendToPlayer(player, new DisableZoomPacket(!ALLOW_ZOOM.get()), packets.toArray(CustomPacketPayload[]::new));
        }
    }
}
