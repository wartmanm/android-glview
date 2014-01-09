package com.github.wartman4404.glview.time;


public class LinearTimeSource implements TimeSource {
	private long startTime;
	public LinearTimeSource(long startTime) {
		this.startTime = startTime;
	}
	public long elapsed(long currentTime) {
		return currentTime - startTime;
	}

}