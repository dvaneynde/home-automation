package eu.dlvm.domotics.utils;

import java.util.Calendar;

public interface IOpenWeatherMap {
    public class Info {

        private Calendar sunriseCal, sunsetCal;

        public Info(Calendar sunrise, Calendar sunset) {
            sunsetHours = sunset.get(Calendar.HOUR_OF_DAY);
            sunsetMinutes = sunset.get(Calendar.MINUTE);
            sunriseHours = sunrise.get(Calendar.HOUR_OF_DAY);
            sunriseMinutes = sunrise.get(Calendar.MINUTE);
        }

        public Info(long sunriseMs, long sunsetMs) {
            sunriseCal = Calendar.getInstance();
            sunriseCal.setTimeInMillis(sunriseMs);
            sunsetCal = Calendar.getInstance();
            sunsetCal.setTimeInMillis(sunsetMs);
            sunsetHours = sunsetCal.get(Calendar.HOUR_OF_DAY);
            sunsetMinutes = sunsetCal.get(Calendar.MINUTE);
            sunriseHours = sunriseCal.get(Calendar.HOUR_OF_DAY);
            sunriseMinutes = sunriseCal.get(Calendar.MINUTE);
        }

        public Calendar getSunrise() {
            return sunriseCal;
        }
        public Calendar getSunset() {
            return sunsetCal;
        }

        public final int sunsetHours, sunsetMinutes, sunriseHours, sunriseMinutes;
    }

    public Info getWeatherReport();
}