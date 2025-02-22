package com.matyrobbrt.okzoomer.config;

import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static final ModConfigSpec SPEC;

    // Features
    public static final ModConfigSpec.EnumValue<ConfigEnums.CinematicCameraOptions> CINEMATIC_CAMERA;
    public static final ModConfigSpec.EnumValue<ConfigEnums.ZoomTransitionOptions> ZOOM_TRANSITION;
    public static final ModConfigSpec.EnumValue<ConfigEnums.ZoomModes> ZOOM_MODE;
    public static final ModConfigSpec.EnumValue<ConfigEnums.ZoomOverlays> ZOOM_OVERLAY;
    public static final ModConfigSpec.EnumValue<ConfigEnums.SpyglassDependency> SPYGLASS_DEPENDENCY;
    public static final ModConfigSpec.BooleanValue REDUCE_SENSITIVITY;
    public static final ModConfigSpec.BooleanValue ALLOW_SCROLLING;
    public static final ModConfigSpec.BooleanValue EXTRA_KEY_BINDS;
    public static final ModConfigSpec.BooleanValue DISABLE_OVERLAY_NO_HUD;

    // Values
    public static final ModConfigSpec.DoubleValue ZOOM_DIVISOR;
    public static final ModConfigSpec.DoubleValue MINIMUM_ZOOM_DIVISOR;
    public static final ModConfigSpec.DoubleValue MAXIMUM_ZOOM_DIVISOR;
    public static final ModConfigSpec.IntValue UPPER_SCROLL_STEPS;
    public static final ModConfigSpec.IntValue LOWER_SCROLL_STEPS;
    public static final ModConfigSpec.DoubleValue SMOOTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue CINEMATIC_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MINIMUM_LINEAR_STEP;
    public static final ModConfigSpec.DoubleValue MAXIMUM_LINEAR_STEP;
    
    // Tweaks
    public static final ModConfigSpec.BooleanValue RESET_ZOOM_WITH_MOUSE;
    public static final ModConfigSpec.BooleanValue USE_SPYGLASS_TEXTURE;
    public static final ModConfigSpec.BooleanValue USE_SPYGLASS_SOUNDS;
    public static final ModConfigSpec.BooleanValue SHOW_RESTRICTION_TOASTS;
    
    static {
        final var builder = new ModConfigSpec.Builder();

        // Features
        {
            builder.push("features");

            CINEMATIC_CAMERA = builder.comment("Defines the cinematic camera while zooming.",
                            "'OFF' disables the cinematic camera",
                            "'VANILLA' uses Vanilla's cinematic camera.",
                            "'MULTIPLIED' is a multiplied variant of 'VANILLA'")
                    .defineEnum("cinematic_camera", ConfigEnums.CinematicCameraOptions.OFF);
            REDUCE_SENSITIVITY = builder.comment("Reduces the mouse sensitivity when zooming.").define("reduce_sensitivity", true);
            ZOOM_TRANSITION = builder.comment("Adds transitions between zooms.",
                    "'OFF' disables transitions.",
                    "'SMOOTH' replicates Vanilla's dynamic FOV.",
                    "'LINEAR' removes the smoothiness.").defineEnum("zoom_transition", ConfigEnums.ZoomTransitionOptions.SMOOTH);
            ZOOM_MODE = builder.comment("The behavior of the zoom key.",
                    "'HOLD' needs the zoom key to be hold.",
                    "'TOGGLE' has the zoom key toggle the zoom.",
                    "'PERSISTENT' makes the zoom permanent.").defineEnum("zoom_mode", ConfigEnums.ZoomModes.HOLD);
            ALLOW_SCROLLING = builder.comment("Allows to increase or decrease zoom by scrolling.")
                    .define("zoom_scrolling", true);
            EXTRA_KEY_BINDS = builder.comment("Adds zoom manipulation keys along with the zoom key.",
                            "Note that this config will NOT prevent the keybinds from being registered, but they will become unusable if false.")
                    .define("extra_key_binds", true);
            ZOOM_OVERLAY = builder.comment("Adds an overlay in the screen during zoom.",
                    "'VIGNETTE' uses a vignette as the overlay.",
                    "'SPYGLASS' uses the spyglass overlay with the vignette texture.",
                    "The vignette texture can be found at: assets/okzoomer/textures/misc/zoom_overlay.png").defineEnum("zoom_overlay", ConfigEnums.ZoomOverlays.OFF);
            SPYGLASS_DEPENDENCY = builder.comment("Determines how the zoom will depend on the spyglass.",
                            "'REQUIRE_ITEM' will make zooming require a spyglass.",
                            "'REPLACE_ZOOM' will replace spyglass's zoom with Ok Zoomer's zoom.",
                            "'BOTH' will apply both options at the same time.",
                            "The 'REQUIRE_ITEM' option is configurable through the okzoomer:zoom_dependencies item tag.")
                    .defineEnum("spyglass_dependency", ConfigEnums.SpyglassDependency.OFF);
            DISABLE_OVERLAY_NO_HUD = builder.comment("If the OkZoomer overlay should be disabled when the HUD is hidden. (F1 mode)")
                            .define("disable_overlay_no_hud", true);

            builder.pop();
        }

        {
            builder.push("values");
            
            ZOOM_DIVISOR = builder.comment("The divisor applied to the FOV when zooming.")
                            .defineInRange("zoom_divisor", 4.0D, 0D, Double.MAX_VALUE);
            
            MINIMUM_ZOOM_DIVISOR = builder.comment("The minimum value that you can scroll down.")
                            .defineInRange("minimum_zoom_divisor", 1.0D, 0D, Double.MAX_VALUE);
            MAXIMUM_ZOOM_DIVISOR = builder.comment("The maximum value that you can scroll down.")
                            .defineInRange("maximum_zoom_divisor", 50.0D, 0D, Double.MAX_VALUE);
            
            UPPER_SCROLL_STEPS = builder.comment("The number of steps between the zoom divisor and the maximum zoom divisor.",
                    "Used by zoom scrolling.").defineInRange("upper_scroll_steps", 20, 0, Integer.MAX_VALUE);
            LOWER_SCROLL_STEPS = builder.comment("The number of steps between the zoom divisor and the minimum zoom divisor.",
                    "Used by zoom scrolling.").defineInRange("lower_scroll_steps", 4, 0, Integer.MAX_VALUE);
            
            SMOOTH_MULTIPLIER = builder.comment("The multiplier used for smooth transitions.")
                            .defineInRange("smooth_multiplier", 0.75D, -1.0D, 1.0D);
            CINEMATIC_MULTIPLIER = builder.comment("The multiplier used for the multiplied cinematic camera.")
                            .defineInRange("cinematic_multiplier", 4.0D, -4.0D, 4.0D);
            
            MINIMUM_LINEAR_STEP = builder.comment("The minimum value that the linear transition step can reach.")
                            .defineInRange("minimum_linear_step", 0.125D, 0D, Double.MAX_VALUE);
            MAXIMUM_LINEAR_STEP = builder.comment("The maximum value that the linear transition step can reach.")
                            .defineInRange("maximum_linear_step", 0.25D, 0D, Double.MAX_VALUE);
            
            builder.pop();
        }

        {
            builder.push("tweaks");
            
            RESET_ZOOM_WITH_MOUSE = builder.comment("Allows resetting the zoom with the middle mouse button.")
                            .define("reset_zoom_with_mouse", true);
            USE_SPYGLASS_TEXTURE = builder.comment("If enabled, the spyglass overlay texture is used instead of Ok Zoomer's overlay texture.")
                            .define("use_spyglass_texture", false);
            USE_SPYGLASS_SOUNDS = builder.comment("If enabled, the zoom will use spyglass sounds on zooming in and out.")
                    .define("use_spyglass_sounds", false);
            SHOW_RESTRICTION_TOASTS = builder.comment("Shows toasts when the server imposes a restriction.")
                            .define("show_restriction_toasts", true);
            
            builder.pop();
        }
        
        SPEC = builder.build();
    }

    public enum ZoomPresets {
        DEFAULT,
        CLASSIC,
        PERSISTENT,
        SPYGLASS
    }

    public static void resetToPreset(ZoomPresets preset) {
        ClientConfig.CINEMATIC_CAMERA.set(preset == ZoomPresets.CLASSIC ? ConfigEnums.CinematicCameraOptions.VANILLA : ConfigEnums.CinematicCameraOptions.OFF);
        ClientConfig.REDUCE_SENSITIVITY.set(preset != ZoomPresets.CLASSIC);
        ClientConfig.ZOOM_TRANSITION.set(preset == ZoomPresets.CLASSIC ? ConfigEnums.ZoomTransitionOptions.OFF : ConfigEnums.ZoomTransitionOptions.SMOOTH);
        ClientConfig.ZOOM_MODE.set(preset == ZoomPresets.PERSISTENT ? ConfigEnums.ZoomModes.PERSISTENT : ConfigEnums.ZoomModes.HOLD);
        ClientConfig.ALLOW_SCROLLING.set(preset == ZoomPresets.CLASSIC || preset == ZoomPresets.SPYGLASS);
        ClientConfig.EXTRA_KEY_BINDS.set(preset != ZoomPresets.CLASSIC);
        ClientConfig.ZOOM_OVERLAY.set(preset == ZoomPresets.SPYGLASS ? ConfigEnums.ZoomOverlays.SPYGLASS : ConfigEnums.ZoomOverlays.OFF);
        ClientConfig.SPYGLASS_DEPENDENCY.set(preset == ZoomPresets.SPYGLASS ? ConfigEnums.SpyglassDependency.BOTH : ConfigEnums.SpyglassDependency.OFF);

        ClientConfig.RESET_ZOOM_WITH_MOUSE.set(preset != ZoomPresets.CLASSIC);
        ClientConfig.USE_SPYGLASS_TEXTURE.set(preset == ZoomPresets.SPYGLASS);
        ClientConfig.USE_SPYGLASS_SOUNDS.set(preset == ZoomPresets.SPYGLASS);
        ClientConfig.SHOW_RESTRICTION_TOASTS.set(true);

        ClientConfig.ZOOM_DIVISOR.set(switch (preset) {
            case PERSISTENT -> 1.0D;
            case SPYGLASS -> 10.0D;
            default -> 4.0D;
        });
        ClientConfig.MINIMUM_ZOOM_DIVISOR.set(1D);
        ClientConfig.MAXIMUM_LINEAR_STEP.set(50D);
        ClientConfig.UPPER_SCROLL_STEPS.set(preset == ZoomPresets.SPYGLASS ? 16 : 20);
        ClientConfig.LOWER_SCROLL_STEPS.set(preset == ZoomPresets.SPYGLASS ? 8 : 4);
        ClientConfig.SMOOTH_MULTIPLIER.set(preset == ZoomPresets.SPYGLASS ? 0.5D : 0.75D);
        ClientConfig.CINEMATIC_MULTIPLIER.set(4D);
        ClientConfig.MINIMUM_LINEAR_STEP.set(0.125D);
        ClientConfig.MAXIMUM_LINEAR_STEP.set(0.25D);

        SPEC.save();
    }

    @SubscribeEvent
    static void configChanged(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() != ModConfig.Type.CLIENT)
            return;
        ZoomUtils.LOGGER.info("THe OkZoomer client config has been changed!");
        OkZoomerNetwork.configureZoomInstance();
    }
}
