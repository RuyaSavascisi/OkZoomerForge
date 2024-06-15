package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.OkZoomerClient;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AcknowledgeModPacket() implements Packet {
    public static final Type<AcknowledgeModPacket> TYPE = Packet.type("acknowledge");

    public static AcknowledgeModPacket decode(FriendlyByteBuf buf) {
        return new AcknowledgeModPacket();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handleClient(IPayloadContext context) {
        final var res = OkZoomerNetwork.checkRestrictions();
        if (res == OkZoomerNetwork.Acknowledgement.HAS_RESTRICTIONS) {
            ZoomUtils.LOGGER.info("This server acknowledges the mod and has established some restrictions");
            doSendToast(Component.translatable("toast.okzoomer.acknowledge_mod_restrictions"));
        } else {
            ZoomUtils.LOGGER.info("This server acknowledges the mod and establishes no restrictions");
            // doSendToast(Component.translatable("toast.okzoomer.acknowledge_mod"));
        }
    }

    private void doSendToast(Component component) {
        OkZoomerClient.sendToast(component);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
