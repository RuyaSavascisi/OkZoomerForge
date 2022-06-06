package com.matyrobbrt.okzoomer.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.matyrobbrt.okzoomer.APIImpl;
import com.matyrobbrt.okzoomer.ZoomKeyBinds;
import com.matyrobbrt.okzoomer.api.ZoomInstance;
import com.matyrobbrt.okzoomer.config.ClientConfig;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.config.ConfigEnums;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;
import net.minecraft.client.MouseHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Unique
    private boolean modifyMouse;

    @Unique
    private double finalCursorDeltaX;

    @Unique
    private double finalCursorDeltaY;

    @Inject(
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;invertYMouse:Z",
            opcode = Opcodes.GETFIELD
        ),
        method = "turnPlayer()V",
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void okzoomer$applyZoomChanges(CallbackInfo ci, double d, double e, double k, double l, double f, double g, double h, int m) {
        this.modifyMouse = false;
        if (APIImpl.shouldIterateZoom() || APIImpl.shouldIterateModifiers()) {
            for (ZoomInstance instance : APIImpl.getZoomInstances()) {
                if (instance.getMouseModifier() != null) {
                    boolean zoom = instance.getZoom();
                    if (zoom || instance.isModifierActive()) {
                        instance.getMouseModifier().tick(zoom);
                        double zoomDivisor = zoom ? instance.getZoomDivisor() : 1.0;
                        double transitionDivisor = instance.getTransitionMode().getInternalMultiplier();
                        k = instance.getMouseModifier().applyXModifier(k, h, e, zoomDivisor, transitionDivisor);
                        l = instance.getMouseModifier().applyYModifier(l, h, e, zoomDivisor, transitionDivisor);
                        this.modifyMouse = true;
                    }
                }
            }
        }
        this.finalCursorDeltaX = k;
        this.finalCursorDeltaY = l;
    }

    @ModifyVariable(
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;invertYMouse:Z",
            opcode = Opcodes.GETFIELD
        ),
        method = "turnPlayer()V",
        ordinal = 2
    )
    private double okzoomer$modifyFinalCursorDeltaX(double k) {
        if (!this.modifyMouse) return k;
        return finalCursorDeltaX;
    }

    @ModifyVariable(
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;invertYMouse:Z",
            opcode = Opcodes.GETFIELD
        ),
        method = "turnPlayer()V",
        ordinal = 3
    )
    private double okzoomer$modifyFinalCursorDeltaY(double l) {
        if (!this.modifyMouse) return l;
        return finalCursorDeltaY;
    }

    @Shadow
    private double accumulatedScroll;

    // Handles zoom scrolling
    @Inject(
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;accumulatedScroll:D", ordinal = 7),
            method = "onScroll",
            cancellable = true
    )
    private void okzoomer$handleZoom(long l, double d, double e, CallbackInfo info) {
        if (this.accumulatedScroll != 0.0) {
            if (ClientConfig.ALLOW_SCROLLING.get() && !OkZoomerNetwork.getDisableZoomScrolling()) {
                if (ClientConfig.ZOOM_MODE.get().equals(ConfigEnums.ZoomModes.PERSISTENT)) {
                    if (!ZoomKeyBinds.ZOOM_KEY.isDown()) return;
                }

                if (ZoomUtils.ZOOMER_ZOOM.getZoom()) {
                    ZoomUtils.changeZoomDivisor(this.accumulatedScroll > 0.0);
                    info.cancel();
                }
            }
        }
    }

    // Prevents the spyglass from working if zooming replaces its zoom
    @ModifyExpressionValue(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"),
            method = "turnPlayer"
    )
    private boolean okzoomer$replaceSpyglassMouseMovement(boolean isUsingSpyglass) {
        if (switch (OkZoomerNetwork.getSpyglassDependency()) {
            case REPLACE_ZOOM, BOTH -> true;
            default -> false;
        }) {
            return false;
        } else {
            return isUsingSpyglass;
        }
    }
}
