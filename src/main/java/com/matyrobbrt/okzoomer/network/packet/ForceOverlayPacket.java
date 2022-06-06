package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.OkZoomer;
import com.matyrobbrt.okzoomer.config.ConfigEnums;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record ForceOverlayPacket(ConfigEnums.ZoomOverlays overlay) implements Packet {

    public static ForceOverlayPacket decode(FriendlyByteBuf buf) {
        return new ForceOverlayPacket(buf.readEnum(ConfigEnums.ZoomOverlays.class));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(overlay());
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (overlay != ConfigEnums.ZoomOverlays.OFF) {
            ZoomUtils.LOGGER.info("This server has imposed an overlay on the zoom: {}", overlay);
            OkZoomerNetwork.spyglassOverlay = overlay;
            OkZoomerNetwork.checkRestrictions();
            OkZoomerNetwork.configureZoomInstance();
        }
    }
}
