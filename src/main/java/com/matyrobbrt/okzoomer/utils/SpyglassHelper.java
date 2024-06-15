package com.matyrobbrt.okzoomer.utils;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A utility class whose sole purpose is to hold the spyglass tag
 */
public class SpyglassHelper {
    /**
     * The spyglass tag, which is used internally in order to unhardcode behavior specific to vanilla spyglasses
     */
    public static final TagKey<Item> SPYGLASSES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, "spyglasses"));
}
