package com.simon.credit.toolkit.lang.tuple;

import java.io.Serializable;
import java.util.Objects;

import com.simon.credit.toolkit.lang.MyStringBuilder;

public abstract class MyTriple<L, M, R> implements Serializable {
	private static final long serialVersionUID = 8628847557297588228L;

	public static <L, M, R> MyTriple<L, M, R> of(final L left, final M middle, final R right) {
		return new MyImmutableTriple<L, M, R>(left, middle, right);
	}

	public abstract L getLeft();

	public abstract M getMiddle();

	public abstract R getRight();

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof MyTriple<?, ?, ?>) {
			final MyTriple<?, ?, ?> other = (MyTriple<?, ?, ?>) obj;
			return Objects.equals(getLeft()  , other.getLeft()) && 
				   Objects.equals(getMiddle(), other.getMiddle()) && 
				   Objects.equals(getRight() , other.getRight());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (getLeft()   == null ? 0 : getLeft().hashCode()) ^ 
			   (getMiddle() == null ? 0 : getMiddle().hashCode()) ^ 
			   (getRight()  == null ? 0 : getRight().hashCode());
	}

	@Override
	public String toString() {
		return new MyStringBuilder().append('(')
									.append(getLeft())
									.append(',')
									.append(getMiddle())
									.append(',')
									.append(getRight())
									.append(')')
									.toString();
	}

	public String toString(final String format) {
		return String.format(format, getLeft(), getMiddle(), getRight());
	}

}