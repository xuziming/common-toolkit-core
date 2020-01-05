package com.simon.credit.toolkit.lang;

/**
 * 不可变Triple
 * @author XUZIMING 2019-01-05
 */
public final class MyImmutableTriple<L, M, R> extends MyTriple<L, M, R> {
	private static final long serialVersionUID = -6235661336100407683L;

	public final L left;
	public final M middle;
	public final R right;

	public static <L, M, R> MyImmutableTriple<L, M, R> of(final L left, final M middle, final R right) {
		return new MyImmutableTriple<L, M, R>(left, middle, right);
	}

	public MyImmutableTriple(final L left, final M middle, final R right) {
		super();
		this.left = left;
		this.middle = middle;
		this.right = right;
	}

	@Override
	public L getLeft() {
		return left;
	}

	@Override
	public M getMiddle() {
		return middle;
	}

	@Override
	public R getRight() {
		return right;
	}

}