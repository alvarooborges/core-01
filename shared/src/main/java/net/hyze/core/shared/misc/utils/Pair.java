package net.hyze.core.shared.misc.utils;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"left", "right"})
public class Pair<L, R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

}
