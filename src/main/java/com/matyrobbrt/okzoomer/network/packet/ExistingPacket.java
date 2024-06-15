package com.matyrobbrt.okzoomer.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ExistingPacket implements Packet {
    public static final Type<ExistingPacket> TYPE = Packet.type("exists");

    @Override
    public void encode(FriendlyByteBuf buf) {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
