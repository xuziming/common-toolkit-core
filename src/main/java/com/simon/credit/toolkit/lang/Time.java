package com.simon.credit.toolkit.lang;

import java.util.concurrent.TimeUnit;

/**
 * 时间类
 * @author XUZIMING 2019-11-18
 */
public class Time {
	/** 一秒 */public static final Time   ONE_SECONDS = Time.of( 1, TimeUnit.SECONDS);
	/** 两秒 */public static final Time   TWO_SECONDS = Time.of( 2, TimeUnit.SECONDS);
	/** 三秒 */public static final Time THREE_SECONDS = Time.of( 3, TimeUnit.SECONDS);
	/** 四秒 */public static final Time  FOUR_SECONDS = Time.of( 4, TimeUnit.SECONDS);
	/** 五秒 */public static final Time  FIVE_SECONDS = Time.of( 5, TimeUnit.SECONDS);
	/** 六秒 */public static final Time   SIX_SECONDS = Time.of( 6, TimeUnit.SECONDS);
	/** 七秒 */public static final Time SEVEN_SECONDS = Time.of( 7, TimeUnit.SECONDS);
	/** 八秒 */public static final Time EIGHT_SECONDS = Time.of( 8, TimeUnit.SECONDS);
	/** 九秒 */public static final Time  NINE_SECONDS = Time.of( 9, TimeUnit.SECONDS);
	/** 十秒 */public static final Time   TEN_SECONDS = Time.of(10, TimeUnit.SECONDS);

	/** 持续时间 */
	private long duration;

	/** 时间单位 */
	private TimeUnit timeUnit;

	public Time(long duration, TimeUnit timeUnit) {
		this.duration = duration;
		this.timeUnit = timeUnit;
	}

	public static Time of(long duration, TimeUnit timeUnit) {
		return new Time(duration, timeUnit);
	}

	public long getDuration() {
		return duration;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

}
