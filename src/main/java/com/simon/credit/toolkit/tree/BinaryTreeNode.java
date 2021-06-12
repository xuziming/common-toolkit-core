package com.simon.credit.toolkit.tree;

/**
 * 二叉树节点
 * @author xuziming 2021-06-08
 */
public class BinaryTreeNode {

    private int number;
    private String name;
    private BinaryTreeNode left;
    private BinaryTreeNode right;

    public BinaryTreeNode() {}

    public BinaryTreeNode(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BinaryTreeNode getLeft() {
        return left;
    }

    public void setLeft(BinaryTreeNode left) {
        this.left = left;
    }

    public BinaryTreeNode getRight() {
        return right;
    }

    public void setRight(BinaryTreeNode right) {
        this.right = right;
    }

    public boolean isSame(int targetNumber) {
        return Integer.valueOf(targetNumber).equals(this.number);
    }

    @Override
    public String toString() {
        return "BinaryTreeNode{" + "number=" + number + ", name='" + name + '\'' + '}';
    }

}