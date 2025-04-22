package eu.dlvm.domotics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.events.EventType;

/**
 * Has two per-day times, at {@link #setOnTime(int, int)} an ON event is sent,
 * at {@link #setOffTime(int, int)} an OFF event is sent.
 *
 * @author dirk
 */
public class TimerOnOff extends Controller {

	static Logger logger = LoggerFactory.getLogger(TimerOnOff.class);

	protected int onTimeHours, onTimeMinutes, offTimeHours, offTimeMinutes;

	// time in ms since midnight
	protected int onTimeMs, offTimeMs;
	protected boolean state;

	static boolean loggedOnce = false;

	// timer usage interface
	public TimerOnOff(String name, String description, DomoticLayout ctx) {
		super(name, description, null, ctx);
		state = false;
		onTimeMs = offTimeMs = 0;
	}

	public void setOnTime(int hoursOfDay, int minutesInHour) {
		onTimeHours = hoursOfDay;
		onTimeMinutes = minutesInHour;
		onTimeMs = TimeUtils.timeInDayMillis(hoursOfDay, minutesInHour);
	}

	public void setOffTime(int hoursOfDay, int minutesInHour) {
		offTimeHours = hoursOfDay;
		offTimeMinutes = minutesInHour;
		offTimeMs = TimeUtils.timeInDayMillis(hoursOfDay, minutesInHour);
	}

	public boolean isOn() {
		return state;
	}

	@Override
	public void onEvent(Block source, EventType event) {
	}

	@Override
	public void loop(long currentTime) {
		long currentTimeInDay = TimeUtils.timeInDayMillis(currentTime);
		boolean state2;
		boolean onTimeBeforeOffTime = (onTimeMs <= offTimeMs);
		if (onTimeBeforeOffTime) {
			state2 = (currentTimeInDay > onTimeMs && currentTimeInDay < offTimeMs);
		} else {
			state2 = !(currentTimeInDay > offTimeMs && currentTimeInDay < onTimeMs);
		}
		if (state2 != state) {
			state = state2;
			logger.info("Timer '" + getName() + "' sends event '" + (state ? "ON" : "OFF") + "' because current:"
					+ TimeUtils.getHoursMinutesInDayAsString(currentTime)
					+ ", on:" + TimeUtils.getHourMinutesTimeAsString(onTimeHours, onTimeMinutes)
					+ ", off:" + TimeUtils.getHourMinutesTimeAsString(offTimeHours, offTimeMinutes));
			notifyListeners(state ? EventType.ON : EventType.OFF);
		}
	}

	@Override
	public String toString() {
		return "Timer on:" + TimeUtils.getHourMinutesTimeAsString(onTimeHours, onTimeMinutes) + " off:"
				+ TimeUtils.getHourMinutesTimeAsString(offTimeHours, offTimeMinutes) + " state=" + state + "]";
	}
}
