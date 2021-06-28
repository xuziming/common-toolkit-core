package com.simon.credit.toolkit.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    /**
     * 简单层序遍历
     */
    public static final void simpleLevelOrder(BinaryTreeNode root) {
        BinaryTreeNode node = root;

        LinkedList<BinaryTreeNode> list = new LinkedList<>();
        list.add(node);

        while (!list.isEmpty()) {
            node = list.poll();
            System.out.print(node.getNumber() + "\t");
            if (node.getLeft()  != null) list.offer(node.getLeft());
            if (node.getRight() != null) list.offer(node.getRight());
        }
        System.out.println();
    }

    /**
     * 层序遍历
     */
    public static final List<List<BinaryTreeNode>> levelOrder(BinaryTreeNode root) {
        List<List<BinaryTreeNode>> levelNodes = new ArrayList<>();
        // 判空处理
        if (root == null) {
            return levelNodes;
        }

        // 这里存放树的节点
        List<BinaryTreeNode> nodes = new ArrayList<>();
        // 先把root节点加入节点集合
        nodes.add(root);

        // 如果节点集合有节点需要遍历
        do {
            // 设置遍历集合大小
            int size = nodes.size();
            // 某一层节点的数据
            List<BinaryTreeNode> oneLevelNodes = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                // 取出第一集合元素，按照加入集合顺序打印
                BinaryTreeNode node = nodes.remove(0);

                // 把节点（类似于根节点）信息加入信息集合
                oneLevelNodes.add(node);

                if (node.getLeft()  != null) nodes.add(node.getLeft());
                if (node.getRight() != null) nodes.add(node.getRight());
            }

            // 本次数据加入总的数据集合中
            levelNodes.add(oneLevelNodes);
        } while (!nodes.isEmpty());

        return levelNodes;
    }

    public static final List<List<BinaryTreeNode>> zigzagLevelOrder(BinaryTreeNode root, int level) {
        return zigzagLevelOrder(root, level, new ArrayList<>());
    }

    /**
     * 蛇形遍历
     * @param root
     * @param level
     * @param levelNodes
     * @return 一个元素(元素类型为二叉树节点List列表)代表一层
     */
    private static final <T extends BinaryTreeNode> List<List<T>> zigzagLevelOrder(T root, int level, List<List<T>> levelNodes) {
        if (root == null) {
            return levelNodes;
        }

        if (level >= levelNodes.size()) {// 遍历下一层时，加入一个空的集合
            levelNodes.add(new ArrayList<>());
        }

        // 对2取模，判断奇偶层
        if (level % 2 == 0) {//第奇数层（从1开始计算层高），从头部开始加
            levelNodes.get(level).add(root);
        } else { // 第偶数层，从尾部开始加
            levelNodes.get(level).add(0, root);
        }

        zigzagLevelOrder((T) root.getLeft() , level + 1, levelNodes);// 下一层左子节点
        zigzagLevelOrder((T) root.getRight(), level + 1, levelNodes);// 下一层右子节点

        return levelNodes;
    }

}