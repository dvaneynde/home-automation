package eu.dlvm.domotics.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;

import org.junit.Test;

import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.base.IDomoticLoop;

public class TestSunSetAndRise {

	public SunSetAndRise[] createInfosSimple(Calendar calBase) {
		SunSetAndRise[] infos = new SunSetAndRise[1];
		infos[0] = createInfo(calBase, 7, 54, 17, 38);
		return infos;
	}

	public SunSetAndRise[] createInfosImpossibleTimes(Calendar calBase) {
		SunSetAndRise[] infos = new SunSetAndRise[1];
		infos[0] = createInfo(calBase, 12, 0, 10, 0);
		return infos;
	}

	public SunSetAndRise[] createInfosTwoDaysAndFailedRequests(Calendar calBase) {
		SunSetAndRise[] infos = new SunSetAndRise[5];
		infos[0] = null;
		infos[1] = null;
		infos[2] = createInfo(calBase, 7, 54, 17, 38);
		infos[3] = null;
		infos[4] = createInfo(calBase, 7, 56, 17, 35);
		return infos;
	}

	private SunSetAndRise createInfo(Calendar calBase, int hoursSunrise, int minsSunrise, int hoursSunset,
			int minsSunset) {
		Calendar calSunrise = (Calendar) calBase.clone();
		calSunrise.set(Calendar.HOUR_OF_DAY, hoursSunrise);
		calSunrise.set(Calendar.MINUTE, minsSunrise);
		Calendar calSunset = (Calendar) calBase.clone();
		calSunset.set(Calendar.HOUR_OF_DAY, hoursSunset);
		calSunset.set(Calendar.MINUTE, minsSunset);
		return new SunSetAndRise(calSunrise, calSunset);
	}

	public class OwmTest implements ISunSetAnRiseProvider {
		SunSetAndRise[] infos;
		int callseqnr;

		public OwmTest(SunSetAndRise[] infos) {
			this.infos = infos;
			callseqnr = 0;
		}

		public SunSetAndRise getSunSetAndRise() {
			if (callseqnr >= infos.length)
				fail("getWeatherReport should not have been called anymore.");
			return infos[callseqnr++];
		}
	}

	@Test
	public void testInitAtMidday() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, 11, 16, 12, 0);

		// Initial state.
		TestSunSetAndRise.OwmTest owmt = new TestSunSetAndRise.OwmTest(createInfosSimple(calendar));
		SunSetAndRiseController t = new SunSetAndRiseController("TestSunSetAndRise", "timer day and night", 30, owmt, new DomoticLayout());
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(0, owmt.callseqnr);

		long time = calendar.getTimeInMillis();
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals(30, t.getShimmerMinutes());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);
	}
	
	@Test
	public void testImpossibleTimes() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, 11, 20, 11, 0);

		// Initial state.
		TestSunSetAndRise.OwmTest owmt = new TestSunSetAndRise.OwmTest(createInfosImpossibleTimes(calendar));
		SunSetAndRiseController t = new SunSetAndRiseController("TestSunSetAndRiseController", "timer day and night", 30, owmt, new DomoticLayout());
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals(30, t.getShimmerMinutes());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(0, owmt.callseqnr);

		long time = calendar.getTimeInMillis();
		loopTwice(t, time);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);

		// TODO should check error logging, should have only 1 per day: https://www.baeldung.com/junit-asserting-logs
		time = calendar.getTimeInMillis()+1000;
		loopTwice(t, time);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);
	}
	
	@Test
	public void testInitInEvening() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, 11, 16, 20, 0);

		// Initial state.
		TestSunSetAndRise.OwmTest owmt = new TestSunSetAndRise.OwmTest(createInfosSimple(calendar));
		SunSetAndRiseController t = new SunSetAndRiseController("TestSunSetAndRiseController", "timer day and night", 30, owmt, new DomoticLayout());
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(0, owmt.callseqnr);

		long time = calendar.getTimeInMillis();
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertTrue(t.isSunIsSet());
		assertEquals(30, t.getShimmerMinutes());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);
	}
	
	@Test
	public void testTwoDaysWithInitInMorning() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, 11, 16, 3, 0);
		long basetime = calendar.getTimeInMillis();

		// Initial state. OpenWeatherMap does not return anything yet.
		TestSunSetAndRise.OwmTest owmt = new TestSunSetAndRise.OwmTest(createInfosTwoDaysAndFailedRequests(calendar));
		SunSetAndRiseController t = new SunSetAndRiseController("TestSunSetAndRiseController", "timer day and night", 30, owmt, new DomoticLayout());
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(0, owmt.callseqnr);

		// First OpenWeatherMap request. OpenWeatherMap does not return anything yet.
		long time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 0);
		loopTwice(t, time);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);

		// Within 5 minutes wait time of internet request response time, so there should
		// be no new call to OpenWeatherMap (see callseqnr)
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 4);
		loopTwice(t, time);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(1, owmt.callseqnr);

		// Second request. OpenWeatherMap does not return anything yet.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 5);
		loopTwice(t, time);
		assertFalse(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsSet());
		assertEquals("00:00", t.getSunriseTimeAsString());
		assertEquals("00:00", t.getSunsetTimeAsString());
		assertEquals(2, owmt.callseqnr);

		// Third request. Should have response now from openweathermap. Since dark and
		// INIT we get a DOWN event.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 4, 10);
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertTrue(t.isSunIsSet());
		assertEquals(30, t.getShimmerMinutes());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(3, owmt.callseqnr);

		// Sun not yet risen and before shimmer.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 7, 23);
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertTrue(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		// no increase in callseqnr, since only once a day
		assertEquals(3, owmt.callseqnr);

		// Sun not yet risen but within shimmer time.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 7, 24);
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		// no increase in callseqnr, since only once a day
		assertEquals(3, owmt.callseqnr);

		// Sun is set but shimmer time not passed, so no change.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 18, 7);
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsRisen());
		assertFalse(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(3, owmt.callseqnr);

		// Sun is set and shimmer time has passed, so Down event.
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 18, 8);
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertFalse(t.isSunIsRisen());
		assertTrue(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(3, owmt.callseqnr);

		// Simulate next day, and internet provider gives no result yet so should be
		// same time as previous one
		Calendar c2 = Calendar.getInstance();
		c2.set(2022, 11, 17, 3, 0);
		basetime = c2.getTimeInMillis();
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 3, 0);
		loopTwice(t, time);
		assertFalse(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsSet());
		assertEquals("07:54", t.getSunriseTimeAsString());
		assertEquals("17:38", t.getSunsetTimeAsString());
		assertEquals(4, owmt.callseqnr);

		// now we request succeeds so we should have different time
		time = TimeUtils.getTimeMsSameDayAtHourMinute(basetime, 3, 5);
		loopTwice(t, time);
		assertTrue(t.isTimesUpdatedForToday());
		assertTrue(t.isSunIsSet());
		assertEquals("07:56", t.getSunriseTimeAsString());
		assertEquals("17:35", t.getSunsetTimeAsString());
		assertEquals(5, owmt.callseqnr);
	}

	// Because of async executor in {@see SunSetAndRiseController} need to loop twice so it
	// finishes.
	private static void loopTwice(IDomoticLoop t, long time) {
		t.loop(time);
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t.loop(time + 50);
	}
}
