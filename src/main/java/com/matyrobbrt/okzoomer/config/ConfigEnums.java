package com.matyrobbrt.okzoomer.config;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;

@MethodsReturnNonnullByDefault
public class ConfigEnums {
	public enum CinematicCameraOptions implements StringRepresentable {
		OFF,
		VANILLA,
		MULTIPLIED;

		@Override
		public String getSerializedName() {
			return this.toString();
		}
	}

	public enum ZoomTransitionOptions implements StringRepresentable {
		OFF,
		SMOOTH,
		LINEAR;

		@Override
		public String getSerializedName() {
			return this.toString();
		}
	}

	public enum ZoomModes implements StringRepresentable {
		HOLD,
		TOGGLE,
		PERSISTENT;

		@Override
		public String getSerializedName() {
			return this.toString();
		}
	}

	public enum ZoomOverlays implements StringRepresentable {
		OFF,
		VIGNETTE,
		SPYGLASS;

		@Override
		public String getSerializedName() {
			return this.toString();
		}
	}

	public enum SpyglassDependency implements StringRepresentable {
		OFF,
		REQUIRE_ITEM,
		REPLACE_ZOOM,
		BOTH;

		@Override
		public String getSerializedName() {
			return this.toString();
		}
	}
}
