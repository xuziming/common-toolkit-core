package com.simon.credit.toolkit.lang;

import java.util.concurrent.TimeUnit;

/**
 * 线程工具类
 * @author XUZIMING 2019-11-18
 */
public class ThreadToolkits {

	public static final void sleep(long timeout, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static final void sleep(Time time) {
		sleep(time.getDuration(), time.getTimeUnit());
	}

	public static void main(String[] args) {
		System.out.println("started.");
//		ThreadToolkits.sleep(Time.ONE_SECONDS);
//		ThreadToolkits.sleep(Time.TWO_SECONDS);
//		ThreadToolkits.sleep(Time.THREE_SECONDS);
//		ThreadToolkits.sleep(Time.FOUR_SECONDS);
		ThreadToolkits.sleep(Time.FIVE_SECONDS);
//		ThreadToolkits.sleep(Time.SIX_SECONDS);
//		ThreadToolkits.sleep(Time.SEVEN_SECONDS);
//		ThreadToolkits.sleep(Time.EIGHT_SECONDS);
//		ThreadToolkits.sleep(Time.NINE_SECONDS);
//		ThreadToolkits.sleep(Time.TEN_SECONDS);
		System.out.println("finished.");
	}

}
