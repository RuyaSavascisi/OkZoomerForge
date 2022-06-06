package com.matyrobbrt.okzoomer.events;

import com.matyrobbrt.okzoomer.ZoomKeyBinds;
import com.matyrobbrt.okzoomer.network.OkZoomerNetwork;
import com.matyrobbrt.okzoomer.utils.ZoomUtils;

public class ManageKeyBindsEvent {
    public static void onTickEnd() {
        if (ZoomKeyBinds.areExtraKeyBindsEnabled() && OkZoomerNetwork.getDisableZoomScrolling()) {
            if (ZoomKeyBinds.DECREASE_ZOOM_KEY.isDown() && !ZoomKeyBinds.INCREASE_ZOOM_KEY.isDown()) {
                ZoomUtils.changeZoomDivisor(false);
            }

            if (ZoomKeyBinds.INCREASE_ZOOM_KEY.isDown() && !ZoomKeyBinds.DECREASE_ZOOM_KEY.isDown()) {
                ZoomUtils.changeZoomDivisor(true);
            }

            if (ZoomKeyBinds.RESET_ZOOM_KEY.isDown()) {
                ZoomUtils.resetZoomDivisor(true);
            }
        }
    }
}
