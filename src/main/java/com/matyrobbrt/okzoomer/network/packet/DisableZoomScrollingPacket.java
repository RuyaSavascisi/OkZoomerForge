package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record DisableZoomScrollingPacket(boolean disableScrolling) implements Packet {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(disableScrolling);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        OkZoomerNetwork.disableZoomScrolling = disableScrolling;
        OkZoomerNetwork.checkRestrictions();
        if (disableScrolling) {
            ZoomUtils.LOGGER.info("This server has disabled zoom scrolling");
        }
        OkZoomerNetwork.configureZoomInstance();
    }

    public static DisableZoomScrollingPacket decode(FriendlyByteBuf buf) {
        return new DisableZoomScrollingPacket(buf.readBoolean());
    }
}
