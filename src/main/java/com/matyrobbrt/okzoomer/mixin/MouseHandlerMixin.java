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
    private boolean okzoomer$modifyMouse;

    @Unique
    private double okzoomer$finalCursorDeltaX;

    @Unique
    private double okzoomer$finalCursorDeltaY;

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;invertYMouse()Lnet/minecraft/client/OptionInstance;"
        ),
        method = "turnPlayer()V",
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void okzoomer$applyZoomChanges(CallbackInfo ci, double d, double e, double k, double l, double f, double g, double h, int m) {
        this.okzoomer$modifyMouse = false;
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
                        this.okzoomer$modifyMouse = true;
                    }
                }
            }
        }
        this.okzoomer$finalCursorDeltaX = k;
        this.okzoomer$finalCursorDeltaY = l;
    }

    @ModifyVariable(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;invertYMouse()Lnet/minecraft/client/OptionInstance;"
        ),
        method = "turnPlayer()V",
        ordinal = 2
    )
    private double okzoomer$modifyFinalCursorDeltaX(double k) {
        if (!this.okzoomer$modifyMouse) return k;
        return okzoomer$finalCursorDeltaX;
    }

    @ModifyVariable(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;invertYMouse()Lnet/minecraft/client/OptionInstance;"
        ),
        method = "turnPlayer()V",
        ordinal = 3
    )
    private double okzoomer$modifyFinalCursorDeltaY(double l) {
        if (!this.okzoomer$modifyMouse) return l;
        return okzoomer$finalCursorDeltaY;
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
