package com.simon.credit.toolkit.lang;

import java.util.concurrent.TimeUnit;

public class ThreadToolkits {

	public static final void sleep(long timeout, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
