package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.config.ConfigEnums;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ForceSpyglassPacket(ConfigEnums.SpyglassDependency dependency) implements Packet {
    public static final Type<ForceSpyglassPacket> TYPE = Packet.type("force_spyglass");

    public static ForceSpyglassPacket decode(FriendlyByteBuf buf) {
        return new ForceSpyglassPacket(buf.readEnum(ConfigEnums.SpyglassDependency.class));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(dependency);
    }

    @Override
    public void handle(IPayloadContext context) {
        OkZoomerNetwork.spyglassDependency = (dependency == ConfigEnums.SpyglassDependency.OFF ? null : dependency);
        OkZoomerNetwork.checkRestrictions();
        OkZoomerNetwork.configureZoomInstance();
        if (dependency() != ConfigEnums.SpyglassDependency.OFF)
            ZoomUtils.LOGGER.info("This server has the following spyglass restriction: {}", dependency);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
