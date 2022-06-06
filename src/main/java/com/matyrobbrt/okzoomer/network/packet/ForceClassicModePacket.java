package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record ForceClassicModePacket(boolean forceClassicMode) implements Packet {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(forceClassicMode);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        OkZoomerNetwork.disableZoomScrolling = forceClassicMode;
        OkZoomerNetwork.forceClassicMode = forceClassicMode;
        OkZoomerNetwork.configureZoomInstance();
        OkZoomerNetwork.checkRestrictions();
        if (forceClassicMode) {
            ZoomUtils.LOGGER.info("This server has imposed classic mode");
        }
    }

    public static ForceClassicModePacket decode(FriendlyByteBuf buf) {
        return new ForceClassicModePacket(buf.readBoolean());
    }
}
