package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.network.NetworkEvent;

public record AcknowledgeModPacket() implements Packet {

    public static AcknowledgeModPacket decode(FriendlyByteBuf buf) {
        return new AcknowledgeModPacket();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        final var res = OkZoomerNetwork.checkRestrictions();
        if (res == OkZoomerNetwork.Acknowledgement.HAS_RESTRICTIONS) {
            ZoomUtils.LOGGER.info("This server acknowledges the mod and has established some restrictions");
            OkZoomerNetwork.sendToast(new TranslatableComponent("toast.okzoomer.acknowledge_mod_restrictions"));
        } else {
            ZoomUtils.LOGGER.info("This server acknowledges the mod and establishes no restrictions");
            OkZoomerNetwork.sendToast(new TranslatableComponent("toast.okzoomer.acknowledge_mod"));
        }
    }
}
