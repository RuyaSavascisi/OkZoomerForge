package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ForceZoomDivisorPacket(double min, double max) implements Packet {
    public static final Type<ForceZoomDivisorPacket> TYPE = Packet.type("force_zoom_divisor");

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(min());
        buf.writeDouble(max());
    }

    @Override
    public void handle(IPayloadContext context) {
        if (min <= 0.0 || max <= 0.0) {
            ZoomUtils.LOGGER.info("This server has attempted to set invalid divisor values! (min {}, max {})", min, max);
        } else if (min != 1.0D && max != 50.0D) {
            ZoomUtils.LOGGER.info("This server has set the zoom divisors to minimum {} and maximum {}", min, max);
            OkZoomerNetwork.maximumZoomDivisor = max;
            OkZoomerNetwork.minimumZoomDivisor = min;
            OkZoomerNetwork.forceZoomDivisors = true;
            OkZoomerNetwork.checkRestrictions();
            OkZoomerNetwork.configureZoomInstance();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static ForceZoomDivisorPacket decode(FriendlyByteBuf buf) {
        return new ForceZoomDivisorPacket(buf.readDouble(), buf.readDouble());
    }
}
