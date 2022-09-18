package eu.dlvm.domotics.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.utils.IOpenWeatherMap;
import eu.dlvm.domotics.utils.OpenWeatherMap;

public class TestSunSetAndRise {

	public class OwmTest implements IOpenWeatherMap {
		Info[] infos;
		int callseqnr;

		public OwmTest(Calendar calBase) {
			infos = new Info[5];
			infos[0] = null;
			infos[1] = null;
			infos[2] = createInfo(calBase, 7, 54, 17, 38);
			infos[3] = null;
			infos[4] = createInfo(calBase, 7, 56, 17, 35);
			callseqnr = 0;
		}

		private IOpenWeatherMap.Info createInfo(Calendar calBase, int hoursSunrise, int minsSunrise, int hoursSunset,
				int minsSunset) {
			Calendar calSunrise = (Calendar) calBase.clone();
			calSunrise.set(Calendar.HOUR_OF_DAY, hoursSunrise);
			calSunrise.set(Calendar.MINUTE, minsSunrise);
			Calendar calSunset = (Calendar) calBase.clone();
			calSunset.set(Calendar.HOUR_OF_DAY, hoursSunset);
			calSunset.set(Calendar.MINUTE, minsSunset);
			return new Info(calSunrise, calSunset);
		}

		public Info getWeatherReport() {
			if (callseqnr >= infos.length)
				fail("getWeatherReport should not have been called anymore.");
			return infos[callseqnr++];
		}
	}

	@Test
	public void testCheckChangedDay() {
		long day0 = new GregorianCalendar(2014, 0, 1, 0, 1, 0).getTime().getTime();
		long day1 = new GregorianCalendar(2014, 0, 2, 0, 0, 10).getTime().getTime();

		SunSetAndRise t = new SunSetAndRise("test", "test", 30, new OpenWeatherMap(), new DomoticLayout());
		t.checktTimesUpdatedForToday(day0);
		assertFalse(t.isTimesUpdatedForToday());

		day0 += 100;
		t.checktTimesUpdatedForToday(day0);
		assertFalse(t.isTimesUpdatedForToday());

		t.setTimesUpdatedForToday(true);
		day0 += 100;
		t.checktTimesUpdatedForToday(day0);
		assertTrue(t.isTimesUpdatedForToday());

		t.checktTimesUpdatedForToday(day1);
		assertFalse(t.isTimesUpdatedForToday());
		day1 += 1 * 3600 * 10000; // 1 hour later
		t.checktTimesUpdatedForToday(day1);
		assertFalse(t.isTimesUpdatedForToday());

		t.setTimesUpdatedForToday(true);
		day1 += 1 * 3600 * 10000; // 1 hour later
		t.checktTimesUpdatedForToday(day1);
		assertTrue(t.isTimesUpdatedForToday());
	}

	// TODO why 2 loops?
	// TODO calender instead of basetime?
	// TODO init in morning, day and evening
	// TODO shimmer time correctly applied?
	@Test
	public void testAllOk() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, 11, 16, 3, 0);
		// TODO no basetime?
		long basetime = calendar.getTimeInMillis();

		// Initial state. OpenWeatherMap does not return anything yet.
		TestSunSetAndRise.OwmTest owmt = new TestSunSetAndRise.OwmTest(calendar);
		SunSetAndRise t = new SunSetAndRise("timerDayNigth", "timer day and night", 30, owmt, new DomoticLayout());
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(0, owmt.callseqnr);

		// First OpenWeatherMap request. OpenWeatherMap does not return anything yet.
		long time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 0);
		t.loop(time);
		sleepwell();
		t.loop(time + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);

		// Within 5 minutes wait time of internet request response time (TODO what is
		// this again?)
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 4);
		t.loop(time);
		sleepwell();
		t.loop(time + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);

		// Second request. OpenWeatherMap does not return anything yet.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 5);
		t.loop(time);
		sleepwell();
		t.loop(time + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(2, owmt.callseqnr);

		// Third request. Should have response now from openweathermap. Since dark and
		// INIT we get a DOWN event.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 10);
		t.loop(time);
		sleepwell();
		t.loop(time + 50);
		assertTrue(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertTrue(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(3, owmt.callseqnr);

		// Fourth request. Sun must be risen now.
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 10, 0));
		sleepwell();
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 10, 0) + 50);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(3, owmt.callseqnr);

		// Fifth request in same day, so times do not change.
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 18, 9));
		sleepwell();
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 18, 9) + 50);
		assertTrue(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertTrue(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(3, owmt.callseqnr);

		// simulate next day, and internet provider gives no result yet so should be same time as previous one
		Calendar c2= Calendar.getInstance();
		c2.set(2022, 11, 17, 3, 0);
		basetime = c2.getTimeInMillis();
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 0, 0));
		sleepwell();
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 0, 0) + 50);
		assertFalse(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(4, owmt.callseqnr);

		// now we request succeeds so we should have different time
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 0, 5));
		sleepwell();
		t.loop(TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 0, 5) + 50);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsSet());
		assertEquals("07:56", t.getSunriseTimeAsString());
		assertEquals("17:35", t.getSunsetTimeAsString());
		assertEquals(5, owmt.callseqnr);
	}

	// Nodig omdat async code anders niet afgehandeld is
	public static void sleepwell() {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
