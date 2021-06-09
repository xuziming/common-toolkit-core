package com.simon.credit.toolkit.tree;

/**
 * 顺序存储二叉树遍历
 * @author xuziming 2021-06-08
 */
public class ArrayBinaryTreeOperation {

    /**
     * 前序遍历（根 -> 左子树 -> 右子树）
     * @param array 顺序存储数组
     * @param n 数组的下标（从0开始）
     */
    public static final void preOrder(int[] array, int n) {
        if (array == null || array.length == 0) {
            System.out.println("数组为空，不能执行二叉树的前序遍历");
            return;
        }

        System.out.println(array[n]);
        // 递归向左子树进行前序遍历
        if (2 * n + 1 < array.length) {
            preOrder(array, 2 * n + 1);
        }
        // 递归向右子树进行前序遍历
        if (2 * n + 2 < array.length) {
            preOrder(array, 2 * n + 2);
        }
    }

    /**
     * 中序遍历（左子树 -> 根 -> 右子树）
     * @param array 顺序存储数组
     * @param n 数组的下标（从0开始）
     */
    public static final void infixOrder(int[] array, int n) {
        if (array == null || array.length == 0) {
            System.out.println("数组为空，不能执行二叉树的中序遍历");
            return;
        }

        // 递归向左子树进行中序遍历
        if (2 * n + 1 < array.length) {
            infixOrder(array, 2 * n + 1);
        }
        System.out.println(array[n]);
        // 递归向右子树进行中序遍历
        if (2 * n + 2 < array.length) {
            infixOrder(array, 2 * n + 2);
        }
    }

    /**
     * 后序遍历（左子树 -> 右子树 -> 根）
     * @param array 顺序存储数组
     * @param n 数组的下标（从0开始）
     */
    public static final void postOrder(int[] array, int n) {
        if (array == null || array.length == 0) {
            System.out.println("数组为空，不能执行二叉树的后序遍历");
            return;
        }

        // 递归向左子树进行后序遍历
        if (2 * n + 1 < array.length) {
            postOrder(array, 2 * n + 1);
        }
        // 递归向右子树进行后序遍历
        if (2 * n + 2 < array.length) {
            postOrder(array, 2 * n + 2);
        }
        System.out.println(array[n]);
    }

}