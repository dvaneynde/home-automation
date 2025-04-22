package eu.dlvm.domotics.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.events.EventType;

/**
 * Sends a specified Event every day at a given Time.
 * TODO extend to multiple such events a day, just one for now.
 *
 * @author dirk
 */
public class DailyEvent extends Controller {

	static Logger logger = LoggerFactory.getLogger(DailyEvent.class);

	protected int eventTimeHours, eventTimeMinutes;

	// time in ms since midnight
	protected int eventTimeMs;
	protected EventType event;
	protected boolean isInSpecifiedHourAndMinute;

	static boolean loggedOnce = false;

	// timer usage interface
	public DailyEvent(String name, String description, DomoticLayout ctx) {
		super(name, description, null, ctx);
		isInSpecifiedHourAndMinute = false;
		eventTimeMs = 0;
	}

	public void setTimeAndEvent(int hoursOfDay, int minutesInHour, EventType event) {
		eventTimeHours = hoursOfDay;
		eventTimeMinutes = minutesInHour;
		eventTimeMs = TimeUtils.timeInDayMillis(hoursOfDay, minutesInHour);
		this.event = event;
	}

	@Override
	public void onEvent(Block source, EventType event) {
		logger.warn("I don't support events, ignored. I'm '{}', source was '{}'', event was '{}''.", name,
				source.getName(), event.toString());
	}

	@Override
	public void loop(long currentTime) {
		long currentTimeInDay = TimeUtils.timeInDayMillis(currentTime);

		if (isInSpecifiedHourAndMinute) {
			isInSpecifiedHourAndMinute = (currentTimeInDay < eventTimeMs + 60 * 1000);
		} else {
			if (currentTimeInDay >= eventTimeMs) {
				if (currentTimeInDay < eventTimeMs + 60 * 1000) {
					isInSpecifiedHourAndMinute = true;
					logger.info("TimedEvent '" + getName() + "' sends event '" + event + "'");
					notifyListeners(event);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "[TimedEvent name:" + getName() + " at:"
				+ TimeUtils.getHourMinutesTimeAsString(eventTimeHours, eventTimeMinutes) + " event:"
				+ event + "]";
	}
}
