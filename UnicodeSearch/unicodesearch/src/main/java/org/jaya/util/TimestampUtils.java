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
	
	public static void main(String[] args){
		System.out.println(getHumanReadableElapsedTimeFromNow(getDateFromISO8601String("2017-06-10T08:30:00.000Z")));
	}

	public static String nowAsString(){
		return getISO8601StringForDate(new Date());
	}
	
	public static String getISO8601StringForDate(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(date);
	}	
	
	public static Date getDateFromISO8601String(String str){
		String[] formats = new String[]{"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'"};
		for(String format:formats){
			DateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));		
			try{
				return dateFormat.parse(str);
			}catch(ParseException ex){
				
			}
		}
		return new Date(0);
	}
	
	public static long diffInSeconds(Date d1, Date d2){
		return (d1.getTime()-d2.getTime())/1000;
	}
	
	public static String getHumanReadableElapsedTimeFromNow(Date date){
		long seconds = diffInSeconds(new Date(), date);
		boolean ago = seconds > 0;
		seconds = Math.abs(seconds);
		if( seconds == 0 )
			return "just now";
		int exp60 = (int)Math.floor(Math.log(seconds)/Math.log(60.0));
		if( seconds <= SECONDS_IN_DAY && exp60 >= 0 && exp60 <= 2 ){
			String suffix = new String[]{"seconds", "minutes", "hours"}[exp60] + ((ago)?" ago":" ahead");
			double val = seconds/Math.pow(60, exp60);
			return String.format("%.0f ", val) + suffix;
		}
		else{
			long days = seconds/SECONDS_IN_DAY;
			String suffix = (days>1)?" days":" day";
			suffix += ((ago)?" ago":" ahead");
			return String.format("%d", days) + suffix;
		}
	}
}
