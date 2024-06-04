package com.ares.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 *  @author wesley.zhong
 *
 */
public class DateUtils {
	
	private static String dateFormatStr1 = "yyyy-MM-dd HH:mm:ss";
	private static String dateFormatStr2 = "yyyy-MM-dd";
	private static String dateFormatStr3 = "yyyyMMddHHmmss";
	
//	private static SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//	private static SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
	
	public static long getTimestampFromStr(String dateStr) {
		SimpleDateFormat df1 = new SimpleDateFormat(dateFormatStr1);
		try{
			Date d = df1.parse(dateStr);
			return d.getTime();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return 0L;
	}
	
	public static boolean compareDayWithToday(long timestamp) {
		SimpleDateFormat df2 = new SimpleDateFormat(dateFormatStr2);
		boolean ifSame = false;
		try{
			Date targetDate = new Date(timestamp);
			String targetDateStr = df2.format(targetDate);
			String currentDateStr = df2.format(new Date());
			if(targetDateStr.equals(currentDateStr)) {
				ifSame = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ifSame;
	}
	
	public static String GetCurrentTime()
	{
		SimpleDateFormat df1 = new SimpleDateFormat(dateFormatStr1);
		return df1.format(new Date());
	}
	public static String GetYMD(Date  date){
		SimpleDateFormat df1 = new SimpleDateFormat(dateFormatStr2);
		return df1.format(date);		
	}
	
	public static String GetDateString(Date date){
		SimpleDateFormat df1 = new SimpleDateFormat(dateFormatStr1);
		return df1.format(date);
	}
	
	public static Date  dateAddDays(Date date, int days){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
	
	public static String GetCurrentTimeFormat3()
	{
		SimpleDateFormat df1 = new SimpleDateFormat(dateFormatStr3);
		return df1.format(new Date());
	}
	
	
	public static void main(String[] args) {
//		String test = "2014-01-10 12:30:01--2014-02-10 12:30:02";
//		String[] result = test.split("--");
//		for(String e:result) {
//			System.out.println(e);
//		}
		
//		String testDate = "2014-3-18 12:38:00";
//		long timestamp =DateUtils.getTimestampFromStr(testDate);
//		System.out.println(timestamp);
		
//		SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
//		String test = formatter.format(new Date());
	}
}
