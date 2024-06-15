package com.matyrobbrt.okzoomer.api.overlays;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.api.ZoomOverlay;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

import java.util.Objects;

/**
 * An implementation of the spyglass overlay as a zoom overlay
 */
public class SpyglassZoomOverlay implements ZoomOverlay {
    private static final ResourceLocation OVERLAY_ID = ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, "spyglass_zoom");
    private final ResourceLocation textureId;
    private final Minecraft mc;
    private float scale;
    private boolean active;

    /**
     * Initializes an instance of the spyglass mouse modifier with the specified texture identifier
     * @param textureId The texture identifier for the spyglass overlay
    */
    public SpyglassZoomOverlay(ResourceLocation textureId) {
        this.textureId = textureId;
        this.mc = Minecraft.getInstance();
        this.scale = 0.5F;
        this.active = false;
    }

    @Override
    public ResourceLocation getId() {
        return OVERLAY_ID;
    }

    @Override
    public boolean getActive() {
        return this.active;
    }

    @Override
    public boolean cancelOverlayRendering() {
        return true;
    }

    @Override
    public void renderOverlay(GuiGraphics graphics) {
        int guiWidth = graphics.guiWidth();
        int guiHeight = graphics.guiHeight();
        float dimension = (float) Math.min(guiWidth, guiHeight);
        float scaledDimensions = Math.min((float) guiWidth / dimension, (float) guiHeight / dimension) * scale;
        int width = Mth.floor(dimension * scaledDimensions);
        int height = Mth.floor(dimension * scaledDimensions);
        int x = (guiWidth - width) / 2;
        int y = (guiHeight - height) / 2;
        int yBorder = y + height;
        RenderSystem.enableBlend();
        graphics.blit(textureId, x, y, -90, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.disableBlend();
        graphics.fill(RenderType.guiOverlay(), 0, yBorder, guiWidth, guiHeight, -90, CommonColors.BLACK);
        graphics.fill(RenderType.guiOverlay(), 0, 0, guiWidth, y, -90, CommonColors.BLACK);
        graphics.fill(RenderType.guiOverlay(), 0, y, x, yBorder, -90, CommonColors.BLACK);
        graphics.fill(RenderType.guiOverlay(), x + width, y, guiWidth, yBorder, -90, CommonColors.BLACK);
    }
    
    @Override
    public void tick(boolean active, double divisor, double transitionMultiplier) {
        this.active = active;
    }

    @Override
    public void tickBeforeRender() {
        if (this.mc.options.getCameraType().isFirstPerson()) {
            if (!this.active) {
                this.scale = 0.5F;
            } else {
                float lastFrameDuration = this.mc.getTimer().getGameTimeDeltaPartialTick(true);
                this.scale = Mth.lerp(0.5F * lastFrameDuration, this.scale, 1.125F);
            }
        }
    }
}
