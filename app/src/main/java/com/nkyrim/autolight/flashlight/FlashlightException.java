package com.nkyrim.autolight.flashlight;

/**
 * Exception thrown during flashlight usage
 */
public class FlashlightException extends Exception {
	public FlashlightException(String message, Throwable exc) {
		super(message, exc);
	}
}