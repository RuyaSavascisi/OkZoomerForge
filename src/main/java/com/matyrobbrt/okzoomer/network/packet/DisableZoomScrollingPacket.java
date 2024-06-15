package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DisableZoomScrollingPacket(boolean disableScrolling) implements Packet {
    public static final Type<DisableZoomScrollingPacket> TYPE = Packet.type("disable_zoom_scrolling");

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(disableScrolling);
    }

    @Override
    public void handle(IPayloadContext context) {
        OkZoomerNetwork.disableZoomScrolling = disableScrolling;
        OkZoomerNetwork.checkRestrictions();
        if (disableScrolling) {
            ZoomUtils.LOGGER.info("This server has disabled zoom scrolling");
        }
        OkZoomerNetwork.configureZoomInstance();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static DisableZoomScrollingPacket decode(FriendlyByteBuf buf) {
        return new DisableZoomScrollingPacket(buf.readBoolean());
    }
}
