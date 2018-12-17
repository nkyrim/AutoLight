package com.nkyrim.autolight.flashlight;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import java.lang.ref.WeakReference;

/**
 * Utility class for easy access to the device flashlight
 */
public final class Flashlight {
	private WeakReference<Context> context;
	private Camera camera;
	private boolean open;

	private Flashlight(Context context) {
		this.context = new WeakReference<>(context);
	}

	public static Flashlight create(Context context) {
		// check if device has flash
		PackageManager pc = context.getPackageManager();
		if(!pc.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			return null;
		}

		return new Flashlight(context);
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void open() throws FlashlightException {
		if(open) return;

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			try {
				// Use deprecated "android.hardware.Camera"
				camera = Camera.open();
				Camera.Parameters params = camera.getParameters();
				params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				camera.setParameters(params);
			} catch (RuntimeException exc) {
				throw new FlashlightException("Error opening the flashlight", exc);
			}
		} else {
			// Use Flashlight API
			try {
				if(context.get() != null) {
					CameraManager cm = (CameraManager) context.get().getSystemService(Context.CAMERA_SERVICE);
					cm.setTorchMode("0", true);
				}
			} catch (CameraAccessException exc) {
				throw new FlashlightException("Error opening the flashlight.\n" + exc.getMessage(), exc);
			}
			open = true;
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void close() throws FlashlightException {
		if(!open) return;

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			// Use deprecated "android.hardware.Camera"
			try {
				if(camera != null) {
					Camera.Parameters params = camera.getParameters();
					params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					camera.setParameters(params);
					camera.release();
					camera = null;
				}
				open = false;
			} catch (RuntimeException exc) {
				throw new FlashlightException("Error closing the flashlight", exc);
			}
		} else {
			// Use Flashlight API
			try {
				if(context.get() != null) {
					CameraManager cm = (CameraManager) context.get().getSystemService(Context.CAMERA_SERVICE);
					cm.setTorchMode("0", false);
				}
				open = false;
			} catch (CameraAccessException exc) {
				throw new FlashlightException("Error closing the flashlight.\n" + exc.getMessage(), exc);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
}
