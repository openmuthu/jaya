package org.jaya.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimestampUtils {
	
	public static final int SECONDS_IN_MINUTE = 60;
	public static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
	public static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;

	public static String nowAsString(){
		return getISO8601StringForDate(new Date());
	}
	
	public static String getISO8601StringForDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(date);
	}	
	
	public static Date getDateFromISO8601String(String str){
		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			return dateFormat.parse(str);
		}catch(ParseException ex){
			
		}
		return new Date(0);
	}
	
	public static long diffInSeconds(Date d1, Date d2){
		return (d1.getTime()-d2.getTime())/1000;
	}
}
