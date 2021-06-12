package com.simon.credit.toolkit.tree;

/**
 * 顺序存储二叉树（大堆、小堆以及堆排序的基础）
 * @author xuziming 2021-06-08
 */
public class ArrayBinaryTree {

    private int[] array;

    public ArrayBinaryTree() {}

    public ArrayBinaryTree(int[] array) {
        this.array = array;
    }

    public void setArray(int[] array) {
        this.array = array;
    }

    /**
     * 前序遍历
     */
    public void preOrder() {
        ArrayBinaryTreeOperation.preOrder(array, 0);
    }

    /**
     * 中序遍历
     */
    public void infixOrder() {
        ArrayBinaryTreeOperation.infixOrder(array, 0);
    }

    /**
     * 后序遍历
     */
    public void postOrder() {
        ArrayBinaryTreeOperation.postOrder(array, 0);
    }

}