package com.simon.credit.toolkit.lang.wrapper;

public class LongWrapper extends Number implements Comparable<Long> {
	private static final long serialVersionUID = -1758205070089085006L;

	private Long delegate;

	public LongWrapper(Long delegate) {
		this.delegate = delegate;
	}

	// Requires positive x
	public static int stringSize(long x) {
		long p = 10;
		for (int i = 1; i < 19; i++) {
			if (x < p) {
				return i;
			}
			p = 10 * p;
		}
		return 19;
	}

	public static void getChars(long i, int index, char[] buf) {
		long q;
		int r;
		int charPos = index;
		char sign = 0;

		if (i < 0) {
			sign = '-';
			i = -i;
		}

		// Get 2 digits/iteration using longs until quotient fits into an int
		while (i > Integer.MAX_VALUE) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
			i = q;
			buf[--charPos] = IntegerWrapper.DigitOnes[r];
			buf[--charPos] = IntegerWrapper.DigitTens[r];
		}

		// Get 2 digits/iteration using ints
		int q2;
		int i2 = (int) i;
		while (i2 >= 65536) {
			q2 = i2 / 100;
			// really: r = i2 - (q * 100);
			r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
			i2 = q2;
			buf[--charPos] = IntegerWrapper.DigitOnes[r];
			buf[--charPos] = IntegerWrapper.DigitTens[r];
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i2 <= 65536, i2);
		for (;;) {
			q2 = (i2 * 52429) >>> (16 + 3);
			r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
			buf[--charPos] = IntegerWrapper.digits[r];
			i2 = q2;
			if (i2 == 0)
				break;
		}
		if (sign != 0) {
			buf[--charPos] = sign;
		}
	}

	public int compareTo(Long anotherLong) {
		return compare(delegate.longValue(), anotherLong.longValue());
	}

	public static int compare(long x, long y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	@Override
	public int intValue() {
		return delegate.intValue();
	}

	@Override
	public long longValue() {
		return delegate.longValue();
	}

	@Override
	public float floatValue() {
		return delegate.floatValue();
	}

	@Override
	public double doubleValue() {
		return delegate.doubleValue();
	}

}