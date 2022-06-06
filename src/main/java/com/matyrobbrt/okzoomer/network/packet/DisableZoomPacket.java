package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record DisableZoomPacket(boolean disableZoom) implements Packet {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(disableZoom);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        OkZoomerNetwork.disableZoom = disableZoom;
        OkZoomerNetwork.checkRestrictions();
        if (disableZoom) {
            ZoomUtils.LOGGER.info("This server has disabled zooming");
        }
        OkZoomerNetwork.configureZoomInstance();
    }

    public static DisableZoomPacket decode(FriendlyByteBuf buf) {
        return new DisableZoomPacket(buf.readBoolean());
    }
}
