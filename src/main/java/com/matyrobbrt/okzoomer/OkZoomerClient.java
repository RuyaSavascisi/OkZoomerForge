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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = OkZoomerAPI.MOD_ID, value = Dist.CLIENT)
public class OkZoomerClient {

    private static boolean shouldCancelOverlay;
    private static final ResourceLocation OVERLAY_NAME = new ResourceLocation(OkZoomerAPI.MOD_ID, "okzoomer");

    @SubscribeEvent
    static void registerOverlay(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(OVERLAY_NAME.getPath(), (gui, poseStack, partialTick, width, height) -> {
            shouldCancelOverlay = false;
            for (ZoomInstance instance : APIImpl.getZoomInstances()) {
                ZoomOverlay overlay = instance.getZoomOverlay();
                if (overlay != null) {
                    overlay.tickBeforeRender();
                    if (overlay.getActive()) {
                        // noinspection PointlessBooleanExpression,ConstantConditions
                        shouldCancelOverlay = overlay.cancelOverlayRendering() || true;
                        overlay.renderOverlay();
                    }
                }
            }
        });
    }

    @SubscribeEvent
    static void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(OkZoomerClient::clientTick);
        MinecraftForge.EVENT_BUS.addListener(OkZoomerClient::onMouseInput);
        MinecraftForge.EVENT_BUS.addListener(OkZoomerClient::renderOverlay);
        MinecraftForge.EVENT_BUS.addListener(OkZoomerClient::onMouseScroll);

        ItemProperties.registerGeneric(new ResourceLocation(OkZoomerAPI.MOD_ID, "scoping"),
                (ClampedItemPropertyFunction) (stack, clientWorld, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && entity.getUseItem().is(SpyglassHelper.SPYGLASSES) ? 1.0F : 0.0F);

        OkZoomerNetwork.configureZoomInstance();
    }

    @SubscribeEvent
    static void registerKeys(final RegisterKeyMappingsEvent event) {
        event.register(ZoomKeyBinds.ZOOM_KEY);

        // Extra keybinds
        event.register(ZoomKeyBinds.DECREASE_ZOOM_KEY);
        event.register(ZoomKeyBinds.INCREASE_ZOOM_KEY);
        event.register(ZoomKeyBinds.RESET_ZOOM_KEY);
    }

    static void renderOverlay(final RenderGuiOverlayEvent.Pre event) {
        if (Minecraft.getInstance().level == null) return;
        if (event.getOverlay().id().equals(VanillaGuiOverlay.SPYGLASS.id())) {
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

    static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.ClientTickEvent.Phase.END && Minecraft.getInstance().level == null)
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

            if (event.getScrollDelta() != 0 && ZoomUtils.ZOOMER_ZOOM.getZoom()) {
                ZoomUtils.changeZoomDivisor(event.getScrollDelta() > 0);
                event.setCanceled(true);
            }
        }
    }

    private static final Component TOAST_TITLE = Component.translatable("toast.okzoomer.title");

    public static void sendToast(Component description) {
        if (ClientConfig.SHOW_RESTRICTION_TOASTS.get()) {
            Minecraft.getInstance().getToasts().addToast(SystemToast.multiline(Minecraft.getInstance(), SystemToast.SystemToastIds.TUTORIAL_HINT, TOAST_TITLE, description));
        }
    }
}
