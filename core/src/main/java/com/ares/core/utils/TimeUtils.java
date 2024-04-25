package com.ares.core.utils;

import java.util.concurrent.TimeUnit;


/**
 * @author wesley
 */
public class TimeUtils {

	private static final long MILLISECONDS_IN_ONE_DAY = TimeUnit.HOURS.toMillis(24);
	private static final long MILLISECONDS_IN_ONE_SECOND = 1000L;

	/**
	 * Return current unix time in milliseconds
	 * @return current unix time in milliseconds
	 */
	public static long getCurrentTimeInMilliseconds() {
		return System.currentTimeMillis();
	}

	/**
	 * Return current unix time in seconds
	 * @return current unix time in seconds
	 */
	public static long getCurrentUnixTime() {
		return System.currentTimeMillis() / MILLISECONDS_IN_ONE_SECOND;
	}

	/**
	 * Gets the current time, in milliseconds, shifted back by one day
	 * @return	a day ago in milliseconds
	 */
	public static long getADayAgoInMilliseconds(){
		return getCurrentTimeInMillisecondsShiftedBackBy( MILLISECONDS_IN_ONE_DAY );
	}

	/**
	 * Gets the current time, in milliseconds, shifted back by the specified offset
	 * @param shiftMilliseconds milliseconds offset to be deducted from the current time
	 * @return the current time minus shiftMilliseconds
	 */
	public static long getCurrentTimeInMillisecondsShiftedBackBy( long shiftMilliseconds )
	{
		return getCurrentTimeInMilliseconds() - shiftMilliseconds;
	}

	/**
	 * Gets the current time, in milliseconds, shifted forward by the specified offset
	 * @param shiftMilliseconds milliseconds offset to be added to the current time
	 * @return the current time plus shiftMilliseconds
	 */

	public static long getCurrentTimeInMillisecondsShiftedForwardBy( long shiftMilliseconds )
	{
		return getCurrentTimeInMilliseconds() + shiftMilliseconds;
	}

}
