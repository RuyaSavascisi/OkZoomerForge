package com.matyrobbrt.okzoomer.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.matyrobbrt.okzoomer.APIImpl;
import com.matyrobbrt.okzoomer.api.ZoomInstance;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;invertYMouse()Lnet/minecraft/client/OptionInstance;"
        ),
        method = "turnPlayer(D)V",
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void okzoomer$applyZoomChanges(double movementTime, CallbackInfo ci, @Local(ordinal = 1) LocalDoubleRef i, @Local(ordinal = 2) LocalDoubleRef j, @Local(ordinal = 5) double f) {
        if (APIImpl.shouldIterateZoom() || APIImpl.shouldIterateModifiers()) {
            for (ZoomInstance instance : APIImpl.getZoomInstances()) {
                if (instance.getMouseModifier() != null) {
                    boolean zoom = instance.getZoom();
                    if (zoom || instance.isModifierActive()) {
                        instance.getMouseModifier().tick(zoom);
                        double zoomDivisor = zoom ? instance.getZoomDivisor() : 1.0;
                        double transitionDivisor = instance.getTransitionMode().getInternalMultiplier();
                        i.set(instance.getMouseModifier().applyXModifier(i.get(), f, movementTime, zoomDivisor, transitionDivisor));
                        j.set(instance.getMouseModifier().applyYModifier(j.get(), f, movementTime, zoomDivisor, transitionDivisor));
                    }
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
