package com.matyrobbrt.okzoomer;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.api.ZoomInstance;
import com.matyrobbrt.okzoomer.api.ZoomOverlay;
import com.matyrobbrt.okzoomer.config.ClientConfig;
import com.matyrobbrt.okzoomer.config.ConfigEnums;
import com.matyrobbrt.okzoomer.events.ManageKeyBindsEvent;
import com.matyrobbrt.okzoomer.events.ManageZoomEvent;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.SpyglassHelper;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = OkZoomerAPI.MOD_ID, dist = Dist.CLIENT)
public class OkZoomerClient {

    public OkZoomerClient(IEventBus bus) {
        bus.addListener(OkZoomerClient::registerOverlay);
        bus.addListener(OkZoomerClient::registerKeys);
        bus.addListener(OkZoomerClient::clientSetup);

        NeoForge.EVENT_BUS.addListener(OkZoomerClient::clientTick);
        NeoForge.EVENT_BUS.addListener(OkZoomerClient::onMouseInput);
        NeoForge.EVENT_BUS.addListener(OkZoomerClient::renderOverlay);
        NeoForge.EVENT_BUS.addListener(OkZoomerClient::onMouseScroll);
    }

    private static boolean shouldCancelOverlay;
    private static final ResourceLocation OVERLAY_NAME = ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, "okzoomer");

    private static void registerOverlay(final RegisterGuiLayersEvent event) {
        event.registerAboveAll(OVERLAY_NAME, (gui, delta) -> {
            shouldCancelOverlay = false;
            for (ZoomInstance instance : APIImpl.getZoomInstances()) {
                ZoomOverlay overlay = instance.getZoomOverlay();
                if (overlay != null) {
                    overlay.tickBeforeRender();
                    if (overlay.getActive()) {
                        // noinspection PointlessBooleanExpression,ConstantConditions
                        shouldCancelOverlay = overlay.cancelOverlayRendering() || true;
                        overlay.renderOverlay(gui);
                    }
                }
            }
        });
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        ItemProperties.registerGeneric(ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, "scoping"),
                (ClampedItemPropertyFunction) (stack, clientWorld, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && entity.getUseItem().is(SpyglassHelper.SPYGLASSES) ? 1.0F : 0.0F);

        OkZoomerNetwork.configureZoomInstance();
    }

    private static void registerKeys(final RegisterKeyMappingsEvent event) {
        event.register(ZoomKeyBinds.ZOOM_KEY);

        // Extra keybinds
        event.register(ZoomKeyBinds.DECREASE_ZOOM_KEY);
        event.register(ZoomKeyBinds.INCREASE_ZOOM_KEY);
        event.register(ZoomKeyBinds.RESET_ZOOM_KEY);
    }

    static void renderOverlay(final RenderGuiLayerEvent.Pre event) {
        if (Minecraft.getInstance().level == null) return;
        if (event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) {
            boolean enable = !shouldCancelOverlay;
            if (switch (OkZoomerNetwork.getSpyglassDependency()) {
                case REPLACE_ZOOM, BOTH -> true;
                default -> false;
            }) {
                enable = false;
            }

            if (!enable)
                event.setCanceled(true);
        }
    }

    static void clientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null)
            return;
        ManageKeyBindsEvent.onTickEnd();
        ManageZoomEvent.endTick(Minecraft.getInstance());
    }

    static void onMouseInput(final InputEvent.MouseButton.Pre event) {
        if (ClientConfig.ALLOW_SCROLLING.get() && !OkZoomerNetwork.getDisableZoomScrolling()) {
            if (ClientConfig.ZOOM_MODE.get().equals(ConfigEnums.ZoomModes.PERSISTENT) && !ZoomKeyBinds.ZOOM_KEY.isDown()) {
                return;
            }

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && ZoomKeyBinds.ZOOM_KEY.isDown() && ClientConfig.RESET_ZOOM_WITH_MOUSE.get()) {
                ZoomUtils.resetZoomDivisor(true);
            }
        }
    }

    static void onMouseScroll(final InputEvent.MouseScrollingEvent event) {
        if (ClientConfig.ALLOW_SCROLLING.get() && !OkZoomerNetwork.getDisableZoomScrolling()) {
            if (ClientConfig.ZOOM_MODE.get().equals(ConfigEnums.ZoomModes.PERSISTENT) && !ZoomKeyBinds.ZOOM_KEY.isDown()) {
                return;
            }

            if (event.getScrollDeltaY() != 0 && ZoomUtils.ZOOMER_ZOOM.getZoom()) {
                ZoomUtils.changeZoomDivisor(event.getScrollDeltaY() > 0);
                event.setCanceled(true);
            }
        }
    }

    private static final Component TOAST_TITLE = Component.translatable("toast.okzoomer.title");

    public static void sendToast(Component description) {
        if (ClientConfig.SHOW_RESTRICTION_TOASTS.get()) {
            Minecraft.getInstance().getToasts().addToast(SystemToast.multiline(Minecraft.getInstance(), new SystemToast.SystemToastId(), TOAST_TITLE, description));
        }
    }
}
