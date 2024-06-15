package com.matyrobbrt.okzoomer.network.packet;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface Packet extends CustomPacketPayload {
    void encode(FriendlyByteBuf buf);

    default void handle(IPayloadContext context) {
        if (FMLLoader.getDist().isClient()) {
            handleClient(context);
        }
    }

    default void handleClient(IPayloadContext context) {}

    static <T extends Packet> Type<T> type(String name) {
        return new Type<>(ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, name));
    }
}
