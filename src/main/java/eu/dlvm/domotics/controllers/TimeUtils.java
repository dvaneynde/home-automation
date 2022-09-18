package eu.dlvm.domotics.controllers;

import java.util.Calendar;

public class TimeUtils {

    public static String getHoursMinutesInDayAsString(long time) {
    	int[] times = TimeUtils.hourMinute(time);
    	return String.format("%02d:%02d", times[0], times[1]);
    }

    public static String getHourMinutesTimeAsString(int onTimeHours, int onTimeMinutes) {
    	return String.format("%02d:%02d", onTimeHours, onTimeMinutes);
    }

    public static long getTimeMsSameDayAtHourMinute(long basetime, int hour, int minute) {
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(basetime);
    	c.set(Calendar.HOUR_OF_DAY, hour);
    	c.set(Calendar.MINUTE, minute);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);
    	return c.getTimeInMillis();
    }

    /**
     * @param hours
     *            in the day
     * @param minutes
     *            in the hour
     * @return time in ms since midnight
     */
    public static int timeInDayMillis(int hours, int minutes) {
    	return ((hours * 60) + minutes) * 60 * 1000;
    }

    public static int[] hourMinute(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
    	return new int[] { cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)};
    }

    /**
     * @param basetime
     * @return aantal ms. sinds begin van de dag
     */
    public static int timeInDayMillis(long basetime) {
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(basetime);
    	if (!TimerOnOff.loggedOnce) {
    		TimerOnOff.loggedOnce = true;
    		TimerOnOff.logger.info("timeInDayMillis: timezone=" + c.getTimeZone().getDisplayName());
    	}
    	int timeInDay = timeInDayMillis(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    	return timeInDay;
    }
    
}
