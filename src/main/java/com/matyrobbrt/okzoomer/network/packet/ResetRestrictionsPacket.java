package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record ResetRestrictionsPacket() implements Packet {
    @Override
    public void encode(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ZoomUtils.LOGGER.info("Disconnected from server... Resetting restrictions.");
        OkZoomerNetwork.resetRestrictions();
    }

    public static ResetRestrictionsPacket decode(FriendlyByteBuf buf) {
        return new ResetRestrictionsPacket();
    }
}
