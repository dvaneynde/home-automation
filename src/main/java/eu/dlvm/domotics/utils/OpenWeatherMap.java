package eu.dlvm.domotics.utils;

import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dlvm.domotics.controllers.ISunSetAnRiseProvider;
import eu.dlvm.domotics.controllers.SunSetAndRise;

/**
 * See Readme_OpenWeatherMap.md for more info on response.
 */
public class OpenWeatherMap implements ISunSetAnRiseProvider {

	static Logger log = LoggerFactory.getLogger(OpenWeatherMap.class);

	static final String APP_ID = "9432e6e90eb0c5c30b4f4c19ba396d37";// System.getenv("OPEN_WEATHER_MAP_APP_ID");
	String requestUrl = "http://api.openweathermap.org/data/2.5/weather?q=Leuven,be&appid=" + APP_ID;

	public SunSetAndRise getSunSetAndRise() {
		SunSetAndRise info = null;
		String report = getJsonWeatherReport();
		if (report != null) {
			info = parseWeatherReport(report);
		}
		return info;
	}

	private String getJsonWeatherReport() {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(requestUrl);
		String weather = null;
		try {
			HttpResponse response1 = httpclient.execute(httpGet);
			log.debug("response statusline", response1.getStatusLine());
			HttpEntity entity = response1.getEntity();
			weather = EntityUtils.toString(entity);
			log.debug("http response=" + weather);
			EntityUtils.consume(entity);
			return weather;
		} catch (Exception e) {
			log.warn("Contacting openweathermap.org, no result, got exception:" + e.getMessage(), e);
		} finally {
			httpGet.releaseConnection();
		}
		return weather;
	}

	private SunSetAndRise parseWeatherReport(String jsonWeather) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = mapper.readValue(jsonWeather, JsonNode.class);
		} catch (Exception e) {
			log.warn("Parsing openweathermap.org response failed, got exception: " + e.getMessage());
			return null;
		}

		SunSetAndRise info = null;
		JsonNode sys;
		sys = rootNode.get("sys");
		if (sys != null) {
			JsonNode sunset = sys.get("sunset");
			JsonNode sunrise = sys.get("sunrise");
			if (sunset != null && sunrise != null) {
				info = new SunSetAndRise(sunrise.asLong() * 1000L, sunset.asLong() * 1000L);
			}
		}
		return info;
	}

	public static void main(String[] args) {
		// BasicConfigurator.configure();
		/*
		 * OpenWeatherMap owm = new OpenWeatherMap(); IOpenWeatherMap.Info info = owm.getWeatherReport();
		 * Calendar c = Calendar.getInstance(); c.setTimeInMillis(info.sunset_sec * 1000L);
		 * System.out.println("Sunrise=" + info.sunrise_sec + ", calendar sunrise=" +
		 * c.get(Calendar.HOUR_OF_DAY) + "h" + c.get(Calendar.MINUTE) + "m");
		 */
		// 1663305469
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(1663305469 * 1000L);
		System.out.println("time:\n" + c);
	}
}
