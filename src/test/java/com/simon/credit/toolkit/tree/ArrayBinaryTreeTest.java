package com.simon.credit.toolkit.tree;

/**
 * 顺序存储二叉树测试
 * @author xuziming 2021-06-08
 */
public class ArrayBinaryTreeTest {

    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4, 5, 6, 7};
        ArrayBinaryTree tree = new ArrayBinaryTree(array);
        tree.preOrder();// 1,2,4,5,3,6,7
    }

}