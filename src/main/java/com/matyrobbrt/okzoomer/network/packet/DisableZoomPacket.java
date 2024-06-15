package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DisableZoomPacket(boolean disableZoom) implements Packet {
    public static final Type<DisableZoomPacket> TYPE = Packet.type("disable_zoom");
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(disableZoom);
    }

    @Override
    public void handle(IPayloadContext context) {
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
