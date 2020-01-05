package com.simon.credit.toolkit.lang;

/**
 * 不可变Pair
 * @author XUZIMING 2019-01-05
 */
public final class MyImmutablePair<L, R> extends MyPair<L, R> {
	private static final long serialVersionUID = -4346102748535893437L;

	public final L left;
    public final R right;

    public static <L, R> MyImmutablePair<L, R> of(final L left, final R right) {
        return new MyImmutablePair<L, R>(left, right);
    }

    public MyImmutablePair(final L left, final R right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return left;
    }

    @Override
    public R getRight() {
        return right;
    }

}