package eu.dlvm.domotics.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import eu.dlvm.domotics.controllers.SunSetAndRise;

public class TestOpenWeatherMap {

	// This test is used to check if the OpenWeatherMap API key is set in the environment variable or system property.
	// It should be run before running the actual tests that depend on the OpenWeatherMap API.
	// The key should be set in the environment variable or system property as follows:
	// export OPEN_WEATHER_MAP_API_KEY=your_api_key
	// or
	// mvn -Denv.OPEN_WEATHER_MAP_API_KEY=your_api_key test
	@Test
    public void testOwmApiKeyEnvironmentVariable() {
		String apiKey = OpenWeatherMap.getOpenWeatherMapApiKey();
        assertNotNull("Environment variable or system property OPEN_WEATHER_MAP_API_KEY should not be null", apiKey);
        System.out.println("OPEN_WEATHER_MAP_API_KEY=" + apiKey);
    }

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
