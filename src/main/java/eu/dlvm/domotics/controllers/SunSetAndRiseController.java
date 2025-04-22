package eu.dlvm.domotics.controllers;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.events.EventType;

/**
 * Checks via {@link ISunSetAnRiseProvider} the sunrise and sunset times, and uses
 * these for sending events {@link eu.dlvm.domotics.events.EventType#UP} and
 * {@link eu.dlvm.domotics.events.EventType#DOWN} respectively.
 * <p>
 * {@link #getShimmerMinutes()} minutes is subtracted or added to account for
 * shimmer.
 * 
 * @author dirk
 */
public class SunSetAndRiseController extends Controller {

	private static Logger log = LoggerFactory.getLogger(SunSetAndRiseController.class);

	/**
	 * To avoid sending requests to OpenWeahterMap too often, wait following time in
	 * between. Otherwise OpenWeahterMap might throttle us.
	 */
	public static long TIME_BETWEEN_TIMEPROVIDER_CONTACTS_MS = 5 * 60 * 1000;

	public enum States {
		INIT, UP_SENT, DOWN_SENT;
	}

	private int shimmerMinutes;
	private int sunsetHours, sunsetMinutes, sunriseHours, sunriseMinutes;

	private States state;

	private boolean timesUpdatedForToday, wrongTimesErrorReported;
	private Calendar today;

	private long lastContactTimeProviderMs;
	private ISunSetAnRiseProvider openWeatherMap;
	private Future<SunSetAndRise> asyncCheckWeather;

	public SunSetAndRiseController(String name, String description, int shimmerTimeMin, ISunSetAnRiseProvider owm,
			DomoticLayout ctx) {
		super(name, description, null, ctx);
		this.shimmerMinutes = shimmerTimeMin;
		this.state = States.INIT;
		this.openWeatherMap = owm;
	}

	public int getShimmerMinutes() {
		return shimmerMinutes;
	}

	public int getSunsetHour() {
		return sunsetHours;
	}

	public int getSunsetMinute() {
		return sunsetMinutes;
	}

	public int getSunriseHour() {
		return sunriseHours;
	}

	public int getSunriseMinute() {
		return sunriseMinutes;
	}

	public boolean isSunIsSet() {
		return state == States.DOWN_SENT;
	}

	public boolean isSunIsRisen() {
		return state == States.UP_SENT;
	}

	public String getSunsetTimeAsString() {
		return String.format("%02d", getSunsetHour()) + ':' + String.format("%02d", getSunsetMinute());
	}

	public String getSunriseTimeAsString() {
		return String.format("%02d", getSunriseHour()) + ':' + String.format("%02d", getSunriseMinute());
	}

	public ISunSetAnRiseProvider getOpenWeatherMap() {
		return openWeatherMap;
	}

	public static String formatHM(int hours, int minutes) {
		return String.format("%02d", hours) + ':' + String.format("%02d", minutes);
	}

	void checktTimesUpdatedForToday(long currentTime) {
		if (timesUpdatedForToday || (today == null)) {
			// check if still today: if not, false; above test on today is to
			// force to initialize today
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(currentTime);
			if (today == null || (now.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH))) {
				today = now;
				timesUpdatedForToday = false;
			}
		}
	}

	/** Testing only */
	boolean isTimesUpdatedForToday() {
		return timesUpdatedForToday;
	}

	@Override
	public void loop(long currentTime) {
		checktTimesUpdatedForToday(currentTime);
		if (!timesUpdatedForToday) {
			updateSunTimesIfAvailble(currentTime);
			if (!timesUpdatedForToday)
				return;
		}

		int[] hourMinute = TimeUtils.hourMinute(currentTime);
		int minutesCurrentDay = hourMinute[0] * 60 + hourMinute[1];
		int minutesSunsetPlusShimmer = sunsetHours * 60 + sunsetMinutes + shimmerMinutes;
		int minutesSunriseMinusShimmer = sunriseHours * 60 + sunriseMinutes - shimmerMinutes;

		// assumes minutesSunrise < minutesSunset, checked elsewhere in this class
		boolean sendUp, sendDown;
		sendUp = sendDown = false;
		switch (state) {
			case INIT:
				sendDown = (minutesCurrentDay < minutesSunriseMinusShimmer)
						|| (minutesCurrentDay >= minutesSunsetPlusShimmer);
				sendUp = !sendDown;
				break;
			case DOWN_SENT:
				// Can be early morning or late evening; don't do anything if late evening
				// because already down
				sendUp = minutesCurrentDay >= minutesSunriseMinusShimmer && minutesCurrentDay < minutesSunsetPlusShimmer ;
				break;
			case UP_SENT:
				sendDown = minutesCurrentDay >= minutesSunsetPlusShimmer;
				break;
		}
		if (sendUp || sendDown)
			// first log, then do, otherwise strange sequence of stuff in log
			log.info(
					"SunSetAndRise '{}' sends event '{}'. Note: SunRise={} SunSet={} Shimmer={} mins. (Current time is {}).",
					getName(), sendUp ? "Up" : "Down", formatHM(sunriseHours, sunriseMinutes),
					formatHM(sunsetHours, sunsetMinutes), shimmerMinutes,
					formatHM(hourMinute[0], hourMinute[1]));
		if (sendUp) {
			notifyListeners(EventType.UP);
			state = States.UP_SENT;
		} else if (sendDown) {
			notifyListeners(EventType.DOWN);
			state = States.DOWN_SENT;
		}

	}

	private void updateSunTimesIfAvailble(long currentTime) {
		if (asyncCheckWeather != null) {
			// Already checking, check if there is a result
			if (asyncCheckWeather.isDone()) {
				try {
					SunSetAndRise info = asyncCheckWeather.get();
					asyncCheckWeather = null;
					if (info != null) {
						if (info.sunsetHours * 60 + info.sunsetMinutes < info.sunriseHours * 60
								+ info.sunriseMinutes) {
							if (!wrongTimesErrorReported)
								log.error(
										"Sunrise time ({}) is coming after sunset time ({}) within a day??? Doing nothing.",
										formatHM(info.sunriseHours, info.sunriseMinutes),
										formatHM(info.sunsetHours, info.sunsetMinutes));
							wrongTimesErrorReported = true;
							return;
						}
						sunsetHours = info.sunsetHours;
						sunsetMinutes = info.sunsetMinutes;
						sunriseHours = info.sunriseHours;
						sunriseMinutes = info.sunriseMinutes;
						log.info(
								"Checked todays' sunrise {} and sunset {} times. Note: these include {} minutes shimmer time. I'll check again tomorrow.",
								formatHM(sunriseHours, sunriseMinutes), formatHM(sunsetHours, sunsetMinutes),
								shimmerMinutes);
						timesUpdatedForToday = true;
						wrongTimesErrorReported = false;
					} else {
						log.warn("Did not get times from internet provider. Will try again in "
								+ TIME_BETWEEN_TIMEPROVIDER_CONTACTS_MS / 1000 / 60 + " minutes.");
					}
				} catch (InterruptedException | ExecutionException e) {
					log.warn("Getting weather report failed.", e);
				}
			} else {
				log.debug("loop() asyncCheckWeather task not finished yet...");
			}
		} else if (lastContactTimeProviderMs + TIME_BETWEEN_TIMEPROVIDER_CONTACTS_MS <= currentTime) {
			// Not checking, start one if grace period expired
			lastContactTimeProviderMs = currentTime;
			Callable<SunSetAndRise> worker = new SunSetAndRiseController.WheatherInfoCallable();
			asyncCheckWeather = Executors.newSingleThreadExecutor().submit(worker);
		}
	}

	public class WheatherInfoCallable implements Callable<SunSetAndRise> {
		@Override
		public SunSetAndRise call() throws Exception {
			SunSetAndRise info = openWeatherMap.getSunSetAndRise();
			log.debug("WheatherInfoCallable.call() info=" + info);
			return info;
		}
	}

	@Override
	public void onEvent(Block source, EventType event) {
	}
}
