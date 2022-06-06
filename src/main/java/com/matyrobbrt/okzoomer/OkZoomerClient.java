package com.matyrobbrt.okzoomer;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.api.ZoomInstance;
import com.matyrobbrt.okzoomer.api.ZoomOverlay;
import com.matyrobbrt.okzoomer.config.ClientConfig;
import com.matyrobbrt.okzoomer.events.ManageKeyBindsEvent;
import com.matyrobbrt.okzoomer.events.ManageZoomEvent;
import com.matyrobbrt.okzoomer.config.ConfigEnums;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.SpyglassHelper;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = OkZoomerAPI.MOD_ID, value = Dist.CLIENT)
public class OkZoomerClient {

    private static boolean shouldCancelOverlay;

    @SubscribeEvent
    static void clientSetup(final FMLClientSetupEvent event) {
        OverlayRegistry.registerOverlayTop("LibZoomer", (gui, poseStack, partialTick, width, height) -> {
            shouldCancelOverlay = false;
            for (ZoomInstance instance : APIImpl.getZoomInstances()) {
                ZoomOverlay overlay = instance.getZoomOverlay();
                if (overlay != null) {
                    overlay.tickBeforeRender();
                    if (overlay.getActive()) {
                        shouldCancelOverlay = overlay.cancelOverlayRendering() || true;
                        overlay.renderOverlay();
                    }
                }
            }
        });
        MinecraftForge.EVENT_BUS.addListener(OkZoomerClient::clientTick);
        MinecraftForge.EVENT_BUS.addListener(OkZoomerClient::onMouseInput);

        ItemProperties.registerGeneric(new ResourceLocation(OkZoomerAPI.MOD_ID, "scoping"),
                (ClampedItemPropertyFunction) (stack, clientWorld, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && entity.getUseItem().is(SpyglassHelper.SPYGLASSES) ? 1.0F : 0.0F);

        ClientRegistry.registerKeyBinding(ZoomKeyBinds.ZOOM_KEY);
        if (ZoomKeyBinds.areExtraKeyBindsEnabled()) {
            ClientRegistry.registerKeyBinding(ZoomKeyBinds.DECREASE_ZOOM_KEY);
            ClientRegistry.registerKeyBinding(ZoomKeyBinds.INCREASE_ZOOM_KEY);
            ClientRegistry.registerKeyBinding(ZoomKeyBinds.RESET_ZOOM_KEY);
        }

        OkZoomerNetwork.configureZoomInstance();
    }

    static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.ClientTickEvent.Phase.END && Minecraft.getInstance().level == null)
            return;
        OverlayRegistry.enableOverlay(ForgeIngameGui.SPYGLASS_ELEMENT, !shouldCancelOverlay);

        if (switch (OkZoomerNetwork.getSpyglassDependency()) {
            case REPLACE_ZOOM, BOTH -> true;
            default -> false;
        }) {
            OverlayRegistry.enableOverlay(ForgeIngameGui.SPYGLASS_ELEMENT, false);
        }

        ManageKeyBindsEvent.onTickEnd();
        ManageZoomEvent.endTick(Minecraft.getInstance());
    }

    static void onMouseInput(final InputEvent.MouseInputEvent event) {
        if (ClientConfig.ALLOW_SCROLLING.get() && !OkZoomerNetwork.getDisableZoomScrolling()) {
            if (ClientConfig.ZOOM_MODE.get().equals(ConfigEnums.ZoomModes.PERSISTENT) && !ZoomKeyBinds.ZOOM_KEY.isDown()) {
                return;
            }

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && ZoomKeyBinds.ZOOM_KEY.isDown() && ClientConfig.RESET_ZOOM_WITH_MOUSE.get()) {
                ZoomUtils.resetZoomDivisor(true);
            }
        }
    }
}
