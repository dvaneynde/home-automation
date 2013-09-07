package eu.dlvm.domotica.utils;

import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenWeatherMap {

	static Logger log = Logger.getLogger(OpenWeatherMap.class);

	String requestUrl = "http://api.openweathermap.org/data/2.5/weather?q=Leuven,be";

	public class Info {
		public long sunrise_sec;
		public long sunset_sec;
	}

	public Info getWeatherReport() {
		Info info = null;
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
			log.debug(response1.getStatusLine());
			HttpEntity entity = response1.getEntity();
			weather = EntityUtils.toString(entity);
			log.debug("http response=" + weather);
			EntityUtils.consume(entity);
			return weather;
		} catch (Exception e) {
			log.warn("Contacting openweathermap.org, no result, got exception:"
					+ e.getMessage());
		} finally {
			httpGet.releaseConnection();
		}
		return weather;
	}

	private Info parseWeatherReport(String jsonWeather) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = mapper.readValue(jsonWeather, JsonNode.class);
		} catch (Exception e) {
			log.warn("Parsing openweathermap.org response failed, got exception: "
					+ e.getMessage());
			return null;
		}

		Info info = new Info();
		info.sunset_sec = rootNode.get("sys").get("sunset").asLong();
		info.sunrise_sec = rootNode.get("sys").get("sunrise").asLong();

		return info;
	}

	public static void main(String[] args) {
		OpenWeatherMap owm = new OpenWeatherMap();
		OpenWeatherMap.Info info = owm.getWeatherReport();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(info.sunset_sec*1000L);
		System.out.println("Sunrise="+info.sunrise_sec+", calendar sunrise="+c.get(Calendar.HOUR_OF_DAY)+"h"+c.get(Calendar.MINUTE)+"m");
	}
}
