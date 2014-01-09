package com.github.wartman4404.glview.time;

import java.io.Serializable;

public class PassiveStopwatch implements Serializable, TimeSource {
	private static final long serialVersionUID = 1L;
	private long startedTime;
	private boolean running;
	private long elapsedTime;
	public PassiveStopwatch(long initStarted, long initElapsed, boolean initRunning) {
		this.startedTime = initStarted;
		this.running = initRunning;
		this.elapsedTime = initElapsed;
	}
	public PassiveStopwatch() {
		this(0, 0, false);
	}
	public long elapsed(long currentTime) {
		return elapsedTime + (running ? (currentTime - startedTime) : 0);
	}
	public boolean start(long currentTime) {
		if (!running) {
			running = true;
			startedTime = currentTime;
			return true;
		}
		return false;
	}
	public boolean stop(long currentTime) {
		if (running) {
			running = false;
			elapsedTime += currentTime - startedTime;
			return true;
		}
		return false;
	}
	public void reset(long currentTime) {
		elapsedTime = 0;
		startedTime = currentTime;
	}
	public boolean isRunning() {
		return running;
	}
	public void restore(PassiveStopwatch oldstate) {
     this.startedTime = oldstate.startedTime;
     this.running = oldstate.running;
     this.elapsedTime = oldstate.elapsedTime;
   }
}
