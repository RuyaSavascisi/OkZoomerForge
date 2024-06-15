package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ForceClassicModePacket(boolean forceClassicMode) implements Packet {
    public static final Type<ForceClassicModePacket> TYPE = Packet.type("force_classic_mode");

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(forceClassicMode);
    }

    @Override
    public void handle(IPayloadContext context) {
        OkZoomerNetwork.disableZoomScrolling = forceClassicMode;
        OkZoomerNetwork.forceClassicMode = forceClassicMode;
        OkZoomerNetwork.configureZoomInstance();
        OkZoomerNetwork.checkRestrictions();
        if (forceClassicMode) {
            ZoomUtils.LOGGER.info("This server has imposed classic mode");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static ForceClassicModePacket decode(FriendlyByteBuf buf) {
        return new ForceClassicModePacket(buf.readBoolean());
    }
}
