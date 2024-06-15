package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResetRestrictionsPacket() implements Packet {
    public static final Type<ResetRestrictionsPacket> TYPE = Packet.type("reset_restrictions");

    @Override
    public void encode(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(IPayloadContext context) {
        ZoomUtils.LOGGER.info("Disconnected from server... Resetting restrictions.");
        OkZoomerNetwork.resetRestrictions();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static ResetRestrictionsPacket decode(FriendlyByteBuf buf) {
        return new ResetRestrictionsPacket();
    }
}
