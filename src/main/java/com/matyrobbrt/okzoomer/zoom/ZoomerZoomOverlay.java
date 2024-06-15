package com.matyrobbrt.okzoomer.zoom;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.api.ZoomOverlay;
import com.matyrobbrt.okzoomer.config.ClientConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.matyrobbrt.okzoomer.config.ConfigEnums.ZoomTransitionOptions;

import java.util.Objects;

// Implements the zoom overlay
public class ZoomerZoomOverlay implements ZoomOverlay {
    private static final ResourceLocation OVERLAY_ID = ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, "zoom_overlay");
    private ResourceLocation textureId;
    private boolean active;
    private boolean zoomActive;
    private double divisor;
    private final Minecraft client;

    public float zoomOverlayAlpha = 0.0F;
    public float lastZoomOverlayAlpha = 0.0F;

    public ZoomerZoomOverlay(ResourceLocation textureId) {
        this.textureId = textureId;
        this.active = false;
        this.client = Minecraft.getInstance();
    }

    @Override
    public ResourceLocation getId() {
        return OVERLAY_ID;
    }

    @Override
    public boolean getActive() {
        if (client.options.hideGui && ClientConfig.DISABLE_OVERLAY_NO_HUD.get()) {
            return false;
        }
        return this.active;
    }

    @Override
    public void renderOverlay(GuiGraphics graphics) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        float lerpedOverlayAlpha = Mth.lerp(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true), this.lastZoomOverlayAlpha, this.zoomOverlayAlpha);
        RenderSystem.setShaderColor(lerpedOverlayAlpha, lerpedOverlayAlpha, lerpedOverlayAlpha, 1.0F);
        graphics.blit(this.textureId, 0, 0, -90, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    @Override
    public void tick(boolean active, double divisor, double transitionMultiplier) {
        this.divisor = divisor;
        this.zoomActive = active;
        if ((!active && zoomOverlayAlpha == 0.0f) || active) {
            this.active = active;
        }

        float zoomMultiplier = this.zoomActive ? 1.0F : 0.0F;

        lastZoomOverlayAlpha = zoomOverlayAlpha;

        if (ClientConfig.ZOOM_TRANSITION.get().equals(ZoomTransitionOptions.SMOOTH)) {
            zoomOverlayAlpha += (zoomMultiplier - zoomOverlayAlpha) * ClientConfig.SMOOTH_MULTIPLIER.get();
        } else if (ClientConfig.ZOOM_TRANSITION.get().equals(ZoomTransitionOptions.LINEAR)) {
            double linearStep = Mth.clamp(1.0F / this.divisor, ClientConfig.MINIMUM_LINEAR_STEP.get(), ClientConfig.MAXIMUM_LINEAR_STEP.get());

            zoomOverlayAlpha = Mth.approach(zoomOverlayAlpha, zoomMultiplier, (float) linearStep);
        }
    }
}
