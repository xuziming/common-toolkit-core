package com.simon.credit.toolkit.tree;

/**
 * 二叉树遍历
 * @author xuziming 2021-06-08
 */
public class BinaryTreeOperation {

    /**
     * 前序遍历（根 -> 左子树 -> 右子树）
     * @param node 树结点（可能是根、非叶子结点或者叶子节点）
     */
    public static final void preOrder(BinaryTreeNode node) {
        System.out.println(node);
        // 递归向左子树进行前序遍历
        if (node.getLeft() != null) {
            preOrder(node.getLeft());
        }
        // 递归向右子树进行前序遍历
        if (node.getRight() != null) {
            preOrder(node.getRight());
        }
    }

    /**
     * 中序遍历（左子树 -> 根 -> 右子树）
     * @param node 树结点（可能是根、非叶子结点或者叶子节点）
     */
    public static final void infixOrder(BinaryTreeNode node) {
        // 递归向左子树进行中序遍历
        if (node.getLeft() != null) {
            infixOrder(node.getLeft());
        }
        System.out.println(node);
        // 递归向右子树进行中序遍历
        if (node.getRight() != null) {
            infixOrder(node.getRight());
        }
    }

    /**
     * 后序遍历（左子树 -> 右子树 -> 根）
     * @param node 树结点（可能是根、非叶子结点或者叶子节点）
     */
    public static final void postOrder(BinaryTreeNode node) {
        // 递归向左子树进行后序遍历
        if (node.getLeft() != null) {
            postOrder(node.getLeft());
        }
        // 递归向右子树进行后序遍历
        if (node.getRight() != null) {
            postOrder(node.getRight());
        }
        System.out.println(node);
    }

    /**
     * 前序遍历查找（根 -> 左子树 -> 右子树）
     * @param node 树结点（可能是根、非叶子结点或者叶子节点）
     * @param searchNumber 待查找的节点的number值
     */
    public static final BinaryTreeNode preOrderSearch(BinaryTreeNode node, int searchNumber) {
        System.out.println("前序遍历查找");
        // 比较当前节点是不是目标节点
        if (node.isSame(searchNumber)) {
            return node;
        }

        BinaryTreeNode target = null;

        // 递归向左子树进行前序遍历查找
        if (node.getLeft() != null) {
            target = preOrderSearch(node.getLeft(), searchNumber);
        }
        if (target != null) {
            return target;
        }

        // 递归向右子树进行前序遍历查找
        if (node.getRight() != null) {
            target = preOrderSearch(node.getRight(), searchNumber);
        }
        return target;
    }

    /**
     * 中序遍历查找（左子树 -> 根 -> 右子树）
     * @param node 树结点（可能是根、非叶子结点或者叶子节点）
     * @param searchNumber 待查找的节点的number值
     */
    public static final BinaryTreeNode infixOrderSearch(BinaryTreeNode node, int searchNumber) {
        BinaryTreeNode target = null;

        // 递归向左子树进行中序遍历查找
        if (node.getLeft() != null) {
            target = infixOrderSearch(node.getLeft(), searchNumber);
        }
        if (target != null) {
            return target;
        }

        System.out.println("中序遍历查找");
        // 比较当前节点是不是目标节点
        if (node.isSame(searchNumber)) {
            return node;
        }

        // 递归向右子树进行中序遍历查找
        if (node.getRight() != null) {
            target = infixOrderSearch(node.getRight(), searchNumber);
        }
        return target;
    }

    /**
     * 后序遍历查找（左子树 -> 右子树 -> 根）
     * @param node 树结点（可能是根、非叶子结点或者叶子节点）
     * @param searchNumber 待查找的节点的number值
     */
    public static final BinaryTreeNode postOrderSearch(BinaryTreeNode node, int searchNumber) {
        BinaryTreeNode target = null;

        // 递归向左子树进行后序遍历查找
        if (node.getLeft() != null) {
            target = postOrderSearch(node.getLeft(), searchNumber);
        }
        if (target != null) {
            return target;
        }

        // 递归向右子树进行后序遍历查找
        if (node.getRight() != null) {
            target = postOrderSearch(node.getRight(), searchNumber);
        }
        if (target != null) {
            return target;
        }

        System.out.println("后序遍历查找");
        // 比较当前节点是不是目标节点
        if (node.isSame(searchNumber)) {
            return node;
        }

        return target;
    }

    /**
     * 暴力删除树节点
     * 如果删除的节点是叶子节点，则删除该节点
     * 如果删除的节点是非叶子节点，则删除该子树
     *
     * @param node 树结点（可能是根、非叶子结点或者叶子节点）
     * @param deleteNumber 待删除的节点的number值
     */
    public static final void violenceDeleteNode(BinaryTreeNode node, int deleteNumber) {
        BinaryTreeNode left = node.getLeft();// 左子树
        if (left != null && left.isSame(deleteNumber)) {
            node.setLeft(null);// 删除当前结点的左子树
            return;
        }

        BinaryTreeNode right = node.getRight();// 右子树
        if (right != null && right.isSame(deleteNumber)) {
            node.setRight(null);// 删除当前结点的右子树
            return;
        }

        if (left != null) {
            // 递归左子树，查询并删除目标节点
            violenceDeleteNode(left, deleteNumber);
        }
        if (right != null) {
            // 递归右子树，查询并删除目标节点
            violenceDeleteNode(right, deleteNumber);
        }
    }

}