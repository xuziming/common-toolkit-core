package com.simon.credit.toolkit.lang.wrapper;

public class IntegerWrapper extends Number implements Comparable<Integer> {
	private static final long serialVersionUID = 7910229238664410458L;

	final static char[] DigitTens = { 
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', 
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', 
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2', 
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3', 
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4', 
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5', 
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6', 
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7', 
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8', 
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9', 
	};

	final static char[] DigitOnes = { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
	};

	/**
	 * All possible chars for representing a number as a String
	 */
	final static char[] digits = { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 
		'u', 'v', 'w', 'x', 'y', 'z' 
	};

	final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

	private Integer delegate;

	public IntegerWrapper(Integer delegate) {
		this.delegate = delegate;
	}

	public static void getChars(int i, int index, char[] buf) {
		int q, r;
		int charPos = index;
		char sign = 0;

		if (i < 0) {
			sign = '-';
			i = -i;
		}

		// Generate two digits per iteration
		while (i >= 65536) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			buf[--charPos] = DigitOnes[r];
			buf[--charPos] = DigitTens[r];
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i <= 65536, i);
		for (;;) {
			q = (i * 52429) >>> (16 + 3);
			r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
			buf[--charPos] = digits[r];
			i = q;
			if (i == 0) {
				break;
			}
		}
		if (sign != 0) {
			buf[--charPos] = sign;
		}
	}

	// Requires positive x
	public static int stringSize(int x) {
		for (int i = 0;; i++) {
			if (x <= sizeTable[i]) {
				return i + 1;
			}
		}
	}

	@Override
	public int compareTo(Integer anotherInteger) {
		return compare(delegate.intValue(), anotherInteger.intValue());
	}

	public static int compare(int x, int y) {
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