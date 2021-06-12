package com.simon.credit.toolkit.tree;

/**
 * 二叉树测试
 *
 * @author xuziming 2021-06-08
 */
public class BinaryTreeTest {

    public static void main(String[] args) {
        // 先创建一颗二叉树
        BinaryTree tree = new BinaryTree();

        // 创建需要的节点
        BinaryTreeNode root  = new BinaryTreeNode(1, "宋江");
        BinaryTreeNode node2 = new BinaryTreeNode(2, "吴用");
        BinaryTreeNode node3 = new BinaryTreeNode(3, "卢俊义");
        BinaryTreeNode node4 = new BinaryTreeNode(4, "林冲");
        BinaryTreeNode node5 = new BinaryTreeNode(5, "关胜");

        // 手动创建该二叉树，后面使用递归方式创建二叉树
        root.setLeft(node2);
        root.setRight(node3);
        node3.setRight(node4);
        node3.setLeft(node5);

        // 挂载根节点
        tree.setRoot(root);

        /***************************************************************************/
        /***************************************************************************/

        // 测试前序遍历
        System.out.println("=== 前序遍历: ");
        tree.preOrder();

        // 测试中序遍历
        System.out.println("=== 中序遍历: ");
        tree.infixOrder();

        // 测试后序遍历
        System.out.println("=== 后序遍历: ");
        tree.postOrder();

        /***************************************************************************/
        /***************************************************************************/

        int searchNumber = 5;

        // 测试前序遍历查找
        System.out.println("=== 前序遍历查找: ");
        BinaryTreeNode targetByPreOrderSearch = tree.preOrderSearch(searchNumber);
        if (targetByPreOrderSearch != null) {
            System.out.println("找到了，信息为：" + targetByPreOrderSearch);
        } else {
            System.out.println("前序遍历没有找到number为" + searchNumber + "的目标节点");
        }

        // 测试中序遍历查找
        System.out.println("=== 中序遍历查找: ");
        BinaryTreeNode targetByInfixOrderSearch = tree.infixOrderSearch(searchNumber);
        if (targetByInfixOrderSearch != null) {
            System.out.println("找到了，信息为：" + targetByInfixOrderSearch);
        } else {
            System.out.println("中序遍历没有找到number为" + searchNumber + "的目标节点");
        }

        // 测试后序遍历查找
        System.out.println("=== 后序遍历查找: ");
        BinaryTreeNode targetByPostOrderSearch = tree.postOrderSearch(searchNumber);
        if (targetByPostOrderSearch != null) {
            System.out.println("找到了，信息为：" + targetByPostOrderSearch);
        } else {
            System.out.println("后序遍历没有找到number为" + searchNumber + "的目标节点");
        }

        /***************************************************************************/
        /***************************************************************************/
        int deleteNumber = 5;

        // 测试暴力删除
        System.out.println("=== 删除前--前序遍历: ");
        tree.preOrder();
        tree.violenceDeleteNode(deleteNumber);
        System.out.println("=== 删除后--前序遍历: ");
        tree.preOrder();
    }

}