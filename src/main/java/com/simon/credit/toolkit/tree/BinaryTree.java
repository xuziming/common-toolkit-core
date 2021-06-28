package com.simon.credit.toolkit.tree;

import java.util.List;

/**
 * 二叉树
 *
 * @author xuziming 2021-06-08
 */
public class BinaryTree {

    private BinaryTreeNode root;

    public BinaryTree() {}

    public BinaryTree(BinaryTreeNode root) {
        this.root = root;
    }

    public void setRoot(BinaryTreeNode root) {
        this.root = root;
    }

    /**
     * 前序遍历
     */
    public void preOrder() {
        if (root != null) {
            BinaryTreeOperation.preOrder(root);
        } else {
            System.out.println("二叉树为空，无法遍历");
        }
    }

    /**
     * 中序遍历
     */
    public void infixOrder() {
        if (root != null) {
            BinaryTreeOperation.infixOrder(root);
        } else {
            System.out.println("二叉树为空，无法遍历");
        }
    }

    /**
     * 后序遍历
     */
    public void postOrder() {
        if (root != null) {
            BinaryTreeOperation.postOrder(root);
        } else {
            System.out.println("二叉树为空，无法遍历");
        }
    }

    /**
     * 层序遍历
     */
    public void levelOrder() {
        if (root != null) {
            List<List<BinaryTreeNode>> levelNodes = BinaryTreeOperation.levelOrder(root);
            printLevelNodes(levelNodes);
        } else {
            System.out.println("二叉树为空，无法遍历");
        }
    }

    /**
     * 啮齿形层序遍历(蛇形遍历)
     */
    public void zigzagLevelOrder() {
        if (root != null) {
            List<List<BinaryTreeNode>> levelNodes = BinaryTreeOperation.zigzagLevelOrder(root, 0);
            printLevelNodes(levelNodes);
        } else {
            System.out.println("二叉树为空，无法遍历");
        }
    }

    /**
     * 打印层序遍历结果
     * @param levelNodes
     */
    private void printLevelNodes(List<List<BinaryTreeNode>> levelNodes) {
        for (List<BinaryTreeNode> oneLevelNodes : levelNodes) {
            for (BinaryTreeNode oneLevelNode : oneLevelNodes) {
                System.out.print(oneLevelNode.getNumber() + "\t");
            }
        }
        System.out.println();
    }

    /**
     * 前序遍历查找
     */
    public BinaryTreeNode preOrderSearch(int searchNumber) {
        if (root != null) {
            return BinaryTreeOperation.preOrderSearch(root, searchNumber);
        } else {
            return null;
        }
    }

    /**
     * 中序遍历查找
     */
    public BinaryTreeNode infixOrderSearch(int searchNumber) {
        if (root != null) {
            return BinaryTreeOperation.infixOrderSearch(root, searchNumber);
        } else {
            return null;
        }
    }

    /**
     * 后序遍历查找
     */
    public BinaryTreeNode postOrderSearch(int searchNumber) {
        if (root != null) {
            return BinaryTreeOperation.postOrderSearch(root, searchNumber);
        } else {
            return null;
        }
    }

    /**
     * 暴力删除树节点
     * 如果删除的节点是叶子节点，则删除该节点
     * 如果删除的节点是非叶子节点，则删除该子树
     */
    public void violenceDeleteNode(int deleteNumber) {
        if (root != null) {
            if (Integer.valueOf(deleteNumber).equals(root.getNumber())) {
                root = null;
            } else {
                // 递归删除
                BinaryTreeOperation.violenceDeleteNode(root, deleteNumber);
            }
        } else {
            System.out.println("树为空，不能删除");
        }
    }

}