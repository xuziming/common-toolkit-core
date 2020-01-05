package com.simon.credit.toolkit.lang.tuple;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.simon.credit.toolkit.lang.MyStringBuilder;

public abstract class MyPair<L, R> implements Serializable {
	private static final long serialVersionUID = 3492729747136174358L;

	public static <L, R> MyPair<L, R> of(final L left, final R right) {
		return new MyImmutablePair<L, R>(left, right);
	}

	public abstract L getLeft();

	public abstract R getRight();

	@Override
	public boolean equals(final Object other) {
		if (other == this) {
			return true;
		}
		if (other instanceof Map.Entry<?, ?>) {
			final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) other;
			return Objects.equals(getLeft(), entry.getKey()) && Objects.equals(getRight(), entry.getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		// see Map.Entry API specification
		return (getLeft() == null ? 0 : getLeft().hashCode()) ^ (getRight() == null ? 0 : getRight().hashCode());
	}

	@Override
	public String toString() {
		return new MyStringBuilder().append('(')
									.append(getLeft())
									.append(',')
									.append(getRight())
									.append(')')
									.toString();
	}

	public String toString(final String format) {
		return String.format(format, getLeft(), getRight());
	}

}