package com.matyrobbrt.okzoomer.api.transitions;

import com.matyrobbrt.okzoomer.api.OkZoomerAPI;
import com.matyrobbrt.okzoomer.api.TransitionMode;
import net.minecraft.resources.ResourceLocation;

/**
 * An implementation of a simple zoom as a transition mode
 */
public class InstantTransitionMode implements TransitionMode {
    private static final ResourceLocation TRANSITION_ID = ResourceLocation.fromNamespaceAndPath(OkZoomerAPI.MOD_ID, "no_transition");
    private boolean active;
    private double divisor;

    /**
     * Initializes an instance of the instant transition mode
    */
    public InstantTransitionMode() {
        this.active = false;
        this.divisor = 1.0;
    }
    
    @Override
    public ResourceLocation getId() {
        return TRANSITION_ID;
    }

    @Override
    public boolean getActive() {
        return this.active;
    }

    @Override
    public double applyZoom(double fov, float tickDelta) {
        return fov / this.divisor;
    }

    @Override
    public void tick(boolean active, double divisor) {
        this.active = active;
        this.divisor = divisor;
    }

    @Override
    public double getInternalMultiplier() {
        return 1.0 / this.divisor;
    }
}
