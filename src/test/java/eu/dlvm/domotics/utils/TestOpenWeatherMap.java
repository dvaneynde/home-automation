package eu.dlvm.domotics.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import eu.dlvm.domotics.controllers.SunSetAndRise;

public class TestOpenWeatherMap {

	//@Ignore
	@Test
	public void testOpenWeatherMap() {
		OpenWeatherMap owm = new OpenWeatherMap();
		SunSetAndRise info = owm.getSunSetAndRise();
		assertNotNull(info);
		assertNotNull(info.getSunrise());
		assertNotNull(info.getSunset());
		assertTrue(info.sunriseHours*60+info.sunriseMinutes < info.sunsetHours*60+info.sunsetMinutes);
	}
}
