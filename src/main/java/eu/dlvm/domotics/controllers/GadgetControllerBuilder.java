package eu.dlvm.domotics.controllers;

import eu.dlvm.domotics.base.IBlockRegistrar;

public class GadgetControllerBuilder {
	private String name;
	private long startTimeMs, durationMs = -1L;
	private int onTime, offTime = -1;
	private boolean activateOnStartTime, repeat, daily;
	private IBlockRegistrar ctx;

	public GadgetControllerBuilder(final String name, final boolean daily, final IBlockRegistrar ctx) {
		this.name = name;
		this.daily = daily;
		this.ctx = ctx;
	}

	public GadgetControllerBuilder repeat() {
		return repeat(true);
	}

	public GadgetControllerBuilder repeat(boolean value) {
		repeat = value;
		return this;
	}

	public GadgetControllerBuilder activateOnStart() {
		return activateOnStart(true);
	}

	public GadgetControllerBuilder activateOnStart(boolean value) {
		activateOnStartTime = value;
		return this;
	}

	public GadgetControllerBuilder setOnOffTime(int onTime, int offTime) {
		this.onTime = onTime;
		this.offTime = offTime;
		return this;
	}

	public GadgetControllerBuilder setStartAndDuration(long startTimeMs, long durationMs) {
		this.startTimeMs = startTimeMs;
		this.durationMs = durationMs;
		return this;
	}

	public GadgetController build() {
		// TODO check juiste dingen gezet
		if (daily) {
			return new GadgetController(name, activateOnStartTime, repeat, onTime, offTime, ctx);
		} else {
			return new GadgetController(name, startTimeMs, durationMs, activateOnStartTime, repeat, ctx);
		}
	}
}