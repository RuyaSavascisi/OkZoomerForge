package com.matyrobbrt.okzoomer.utils;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.api.ZoomInstance;
import com.matyrobbrt.okzoomer.api.modifiers.ZoomDivisorMouseModifier;
import com.matyrobbrt.okzoomer.api.transitions.SmoothTransitionMode;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.matyrobbrt.okzoomer.config.ClientConfig.LOWER_SCROLL_STEPS;
import static com.matyrobbrt.okzoomer.config.ClientConfig.MAXIMUM_ZOOM_DIVISOR;
import static com.matyrobbrt.okzoomer.config.ClientConfig.MINIMUM_ZOOM_DIVISOR;
import static com.matyrobbrt.okzoomer.config.ClientConfig.UPPER_SCROLL_STEPS;
import static com.matyrobbrt.okzoomer.config.ClientConfig.ZOOM_DIVISOR;

// The class that contains most of the logic behind the zoom itself
public class ZoomUtils {
    // The logger, used everywhere to print messages to the console
    public static final Logger LOGGER = LoggerFactory.getLogger("Ok Zoomer");

    public static final ZoomInstance ZOOMER_ZOOM = OkZoomerAPI.INSTANCE.registerZoom(OkZoomerAPI.INSTANCE.createZoomInstance(
            ResourceLocation.parse("ok_zoomer:zoom"),
            4.0F,
            new SmoothTransitionMode(0.75f),
            new ZoomDivisorMouseModifier(),
            null
    ));

    public static final TagKey<Item> ZOOM_DEPENDENCIES_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, "zoom_dependencies"));

    public static int zoomStep = 0;

    // The method used for changing the zoom divisor, used by zoom scrolling and the key binds
    public static void changeZoomDivisor(boolean increase) {
        //If the zoom is disabled, don't allow for zoom scrolling
        if (OkZoomerNetwork.getDisableZoom() || OkZoomerNetwork.getDisableZoomScrolling()) {
            return;
        }

        double zoomDivisor = ZOOM_DIVISOR.get();
        double minimumZoomDivisor = MINIMUM_ZOOM_DIVISOR.get();
        double maximumZoomDivisor = MAXIMUM_ZOOM_DIVISOR.get();
        int upperScrollStep = UPPER_SCROLL_STEPS.get();
        int lowerScrollStep = LOWER_SCROLL_STEPS.get();

        if (OkZoomerNetwork.getForceZoomDivisors()) {
            double packetMinimumZoomDivisor = OkZoomerNetwork.getMinimumZoomDivisor();
            double packetMaximumZoomDivisor = OkZoomerNetwork.getMaximumZoomDivisor();

            if (packetMinimumZoomDivisor < minimumZoomDivisor) {
                minimumZoomDivisor = packetMinimumZoomDivisor;
            }

            if (packetMaximumZoomDivisor > maximumZoomDivisor) {
                maximumZoomDivisor = packetMaximumZoomDivisor;
            }
        }

        if (increase) {
            zoomStep = Math.min(zoomStep + 1, upperScrollStep);
        } else {
            zoomStep = Math.max(zoomStep - 1, -lowerScrollStep);
        }

        if (zoomStep > 0) {
            ZOOMER_ZOOM.setZoomDivisor(zoomDivisor + ((maximumZoomDivisor - zoomDivisor) / upperScrollStep * zoomStep));
        } else if (zoomStep == 0) {
            ZOOMER_ZOOM.setZoomDivisor(zoomDivisor);
        } else {
            ZOOMER_ZOOM.setZoomDivisor(zoomDivisor + ((minimumZoomDivisor - zoomDivisor) / lowerScrollStep * -zoomStep));
        }
    }

    // The method used by both the "Reset Zoom" keybind and the "Reset Zoom With Mouse" tweak
    public static void resetZoomDivisor(boolean userPrompted) {
        if (userPrompted && (OkZoomerNetwork.getDisableZoom() || OkZoomerNetwork.getDisableZoomScrolling())) {
            return;
        }

        ZOOMER_ZOOM.resetZoomDivisor();
        zoomStep = 0;
    }

    public static void keepZoomStepsWithinBounds() {
        int upperScrollStep = UPPER_SCROLL_STEPS.get();
        int lowerScrollStep = LOWER_SCROLL_STEPS.get();

        zoomStep = Mth.clamp(zoomStep, -lowerScrollStep, upperScrollStep);
    }

}
