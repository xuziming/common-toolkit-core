package com.simon.credit.toolkit.tree;

/**
 * AVL树
 */
public class AVLTree<T extends Comparable<T>> {

    /**
     * 根结点
     */
    private AVLTreeNode<T> root;

    /**
     * AVL树的节点(内部类)
     *
     * @param <T>
     */
    class AVLTreeNode<T extends Comparable<T>> {
        T value;             // 结点值
        int height;          // 树高度
        AVLTreeNode<T> left;// 左结点
        AVLTreeNode<T> right;// 右结点

        public AVLTreeNode(T value, AVLTreeNode<T> left, AVLTreeNode<T> right) {
            this.value = value;
            this.left = left;
            this.right = right;
            this.height = 0;
        }
    }

    public AVLTree() {
        root = null;
    }

    /**
     * 获取树(或子树)的高度
     */
    private int high(AVLTreeNode<T> tree) {
        if (tree != null) {
            return tree.height;
        }
        return 0;
    }

    /**
     * 获取整棵树的高度
     *
     * @return
     */
    public int high() {
        return high(root);
    }

    /**
     * 比较两个值的大小
     */
    private int max(int a, int b) {
        return a > b ? a : b;
    }

    /**
     * 前序遍历"AVL树"
     */
    private void preOrder(AVLTreeNode<T> tree) {
        if (tree != null) {
            System.out.print(tree.value + " ");
            preOrder(tree.left);
            preOrder(tree.right);
        }
    }

    public void preOrder() {
        preOrder(root);
    }

    /**
     * 中序遍历"AVL树"
     */
    private void inOrder(AVLTreeNode<T> tree) {
        if (tree != null) {
            inOrder(tree.left);
            System.out.print(tree.value + " ");
            inOrder(tree.right);
        }
    }

    public void inOrder() {
        inOrder(root);
    }

    /**
     * 后序遍历"AVL树"
     */
    private void postOrder(AVLTreeNode<T> tree) {
        if (tree != null) {
            postOrder(tree.left);
            postOrder(tree.right);
            System.out.print(tree.value + " ");
        }
    }

    public void postOrder() {
        postOrder(root);
    }

    /**
     * (递归实现)查找"AVL树x"中键值为key的节点
     */
    private AVLTreeNode<T> search(AVLTreeNode<T> x, T key) {
        if (x == null) {
            return x;
        }

        int cmp = key.compareTo(x.value);

        if (cmp < 0) {
            return search(x.left, key);
        } else if (cmp > 0) {
            return search(x.right, key);
        } else {
            return x;
        }
    }

    public AVLTreeNode<T> search(T key) {
        return search(root, key);
    }

    /**
     * (非递归实现)查找"AVL树x"中键值为key的节点
     */
    private AVLTreeNode<T> iterativeSearch(AVLTreeNode<T> x, T key) {
        while (x != null) {
            int cmp = key.compareTo(x.value);

            if (cmp < 0) {
                x = x.left;
            } else if (cmp > 0) {
                x = x.right;
            } else {
                return x;
            }
        }

        return x;
    }

    public AVLTreeNode<T> iterativeSearch(T key) {
        return iterativeSearch(root, key);
    }

    /**
     * 查找最小结点：返回tree为根结点的AVL树的最小结点。
     */
    private AVLTreeNode<T> minimum(AVLTreeNode<T> tree) {
        if (tree == null) {
            return null;
        }

        while (tree.left != null) {
            tree = tree.left;
        }
        return tree;
    }

    public T minimum() {
        AVLTreeNode<T> p = minimum(root);
        if (p != null) {
            return p.value;
        }

        return null;
    }

    /**
     * 查找最大结点：返回tree为根结点的AVL树的最大结点。
     */
    private AVLTreeNode<T> maximum(AVLTreeNode<T> tree) {
        if (tree == null) {
            return null;
        }

        while (tree.right != null) {
            tree = tree.right;
        }
        return tree;
    }

    public T maximum() {
        AVLTreeNode<T> p = maximum(root);
        if (p != null) {
            return p.value;
        }

        return null;
    }

    /**
     * LL：左左对应的情况(左单旋转：只有一次左旋)
     * <p>
     * 返回值：旋转后的根节点
     */
    private AVLTreeNode<T> doRightRotation(AVLTreeNode<T> oldRoot) {
        // (1)老根的左子节点选为新根
        AVLTreeNode<T> newRoot = oldRoot.left;
        // (2)记录新根需要裁切的右子节点
        AVLTreeNode<T> cutNode = newRoot.right;
        // (3)新根的右子节点指向老根
        newRoot.right = oldRoot;
        // (4)老根的左子节点指向新根需要裁切的右子节点
        oldRoot.left = cutNode;

        oldRoot.height = max(high(oldRoot.left), high(oldRoot.right)) + 1;
        newRoot.height = max(high(newRoot.left), oldRoot.height) + 1;

        return newRoot;
    }

    private AVLTreeNode<T> rightRotation(AVLTreeNode<T> node, T value) {
        // 新插入的值落在左子树的右子树上，需先对左子树做一次左旋
        if (value.compareTo(node.left.value) >= 0) {
            node.left = doLeftRotation(node.left);
        }

        // 然后再进行树的右旋
        return doRightRotation(node);
    }

    /**
     * RR：右右对应的情况(右单旋转：只有一次右旋)
     * <p>
     * 返回值：旋转后的根节点
     */
    private AVLTreeNode<T> doLeftRotation(AVLTreeNode<T> oldRoot) {
        // (1)老根的右子节点选为新根
        AVLTreeNode<T> newRoot = oldRoot.right;
        // (2)记录新根需要裁切的左子节点
        AVLTreeNode<T> cutNode = newRoot.left;
        // (3)新根的左子节点指向老根
        newRoot.left = oldRoot;
        // (4)老根的右子节点指向新根需要裁切的左子节点
        oldRoot.right = cutNode;

        oldRoot.height = max(high(oldRoot.left), high(oldRoot.right)) + 1;
        newRoot.height = max(high(newRoot.right), oldRoot.height) + 1;

        return newRoot;
    }

    private AVLTreeNode<T> leftRotation(AVLTreeNode<T> node, T value) {
        // 新插入的值落在右子树的左子树上，需先对左子树做一次右旋
        if (value.compareTo(node.right.value) <= 0) {
            node.right = doRightRotation(node.right);
        }

        // 然后再进行树的左旋
        return doLeftRotation(node);
    }

    /**
     * LR：左右对应的情况(左双旋转：第一次左子树右旋，第二次当前树左旋)
     * <p>
     * 返回值：旋转后的根节点
     */
    private AVLTreeNode<T> leftRightRotation(AVLTreeNode<T> oldRoot) {
        oldRoot.left = doLeftRotation(oldRoot.left);
        return doRightRotation(oldRoot);
    }

    /**
     * RL：右左对应的情况(右双旋转：第一次右子树左旋，第二次当前树右旋)
     * <p>
     * 返回值：旋转后的根节点
     */
    private AVLTreeNode<T> rightLeftRotation(AVLTreeNode<T> oldRoot) {
        oldRoot.right = doRightRotation(oldRoot.right);
        return doLeftRotation(oldRoot);
    }

    /**
     * 将结点插入到AVL树中，并返回根节点
     * <p>
     * 参数说明：
     * tree AVL树的根结点
     * key 插入的结点的键值
     * 返回值：
     * 根节点
     */
    private AVLTreeNode<T> insert(AVLTreeNode<T> node, T value) {
        if (node == null) {
            // 新建节点
            node = new AVLTreeNode<T>(value, null, null);
        } else {
            int cmp = value.compareTo(node.value);

            if (cmp < 0) {// 插入到"tree的左子树"的情况
                node.left = insert(node.left, value);

                // 插入节点后，若AVL树失去平衡，则进行树的旋转
                if (high(node.left) - high(node.right) > 1) {
                    node = rightRotation(node, value);// 右旋
                }
            } else if (cmp > 0) {// 插入到"tree的右子树"的情况
                node.right = insert(node.right, value);

                // 插入节点后，若AVL树失去平衡，则进行树的旋转
                if (high(node.right) - high(node.left) > 1) {
                    node = leftRotation(node, value);// 左旋
                }
            } else {// cmp==0
                System.out.println("添加失败：不允许添加相同的节点！");
            }
        }

        node.height = max(high(node.left), high(node.right)) + 1;

        return node;
    }

    public void insert(T key) {
        root = insert(root, key);
    }

    /**
     * 删除结点(z)，返回根节点
     * <p>
     * 参数说明：
     * tree AVL树的根结点
     * removeNode 待删除的结点
     * 返回值：
     * 根节点
     */
    private AVLTreeNode<T> remove(AVLTreeNode<T> node, AVLTreeNode<T> removeNode) {
        // 根为空 或者 没有要删除的节点，直接返回null。
        if (node == null || removeNode == null) {
            return null;
        }

        int cmp = removeNode.value.compareTo(node.value);
        if (cmp < 0) {        // 待删除的节点在"tree的左子树"中
            node.left = remove(node.left, removeNode);
            // 删除节点后，若AVL树失去平衡，则进行相应的调节。
            if (high(node.right) - high(node.left) > 1) {
                AVLTreeNode<T> r = node.right;
                if (high(r.left) > high(r.right)) {
                    node = rightLeftRotation(node);
                } else {
                    node = doLeftRotation(node);
                }
            }
        } else if (cmp > 0) {    // 待删除的节点在"tree的右子树"中
            node.right = remove(node.right, removeNode);
            // 删除节点后，若AVL树失去平衡，则进行相应的调节。
            if (high(node.left) - high(node.right) > 1) {
                AVLTreeNode<T> l = node.left;
                if (high(l.right) > high(l.left)) {
                    node = leftRightRotation(node);
                } else {
                    node = doRightRotation(node);
                }
            }
        } else {    // tree是对应要删除的节点。
            // tree的左右孩子都非空
            if ((node.left != null) && (node.right != null)) {
                if (high(node.left) > high(node.right)) {
                    // 如果tree的左子树比右子树高；
                    // 则(01)找出tree的左子树中的最大节点
                    //   (02)将该最大节点的值赋值给tree。
                    //   (03)删除该最大节点。
                    // 这类似于用"tree的左子树中最大节点"做"tree"的替身；
                    // 采用这种方式的好处是：删除"tree的左子树中最大节点"之后，AVL树仍然是平衡的。
                    AVLTreeNode<T> max = maximum(node.left);
                    node.value = max.value;
                    node.left = remove(node.left, max);
                } else {
                    // 如果tree的左子树不比右子树高(即它们相等，或右子树比左子树高1)
                    // 则(01)找出tree的右子树中的最小节点
                    //   (02)将该最小节点的值赋值给tree。
                    //   (03)删除该最小节点。
                    // 这类似于用"tree的右子树中最小节点"做"tree"的替身；
                    // 采用这种方式的好处是：删除"tree的右子树中最小节点"之后，AVL树仍然是平衡的。
                    AVLTreeNode<T> min = maximum(node.right);
                    node.value = min.value;
                    node.right = remove(node.right, min);
                }
            } else {
                AVLTreeNode<T> tmp = node;
                node = (node.left != null) ? node.left : node.right;
                tmp = null;
            }
        }

        return node;
    }

    public void remove(T value) {
        AVLTreeNode<T> target;

        if ((target = search(root, value)) != null) {
            root = remove(root, target);
        }
    }

    /**
     * 销毁AVL树
     */
    private void destroy(AVLTreeNode<T> node) {
        if (node == null) {
            return;
        }

        if (node.left != null) {
            destroy(node.left);
        }
        if (node.right != null) {
            destroy(node.right);
        }

        node = null;
    }

    public void destroy() {
        destroy(root);
    }

    /**
     * 打印"二叉查找树"
     * <p>
     * key        -- 节点的键值
     * direction  --  0，表示该节点是根节点;
     * -1，表示该节点是它的父结点的左孩子;
     * 1，表示该节点是它的父结点的右孩子。
     */
    private void print(AVLTreeNode<T> tree, T value, int direction) {
        if (tree != null) {
            if (direction == 0) {// tree是根节点
                System.out.printf("%2d is root\n", tree.value, value);
            } else {// tree是分支节点
                System.out.printf("%2d is %2d's %6s child\n", tree.value, value, direction == 1 ? "right" : "left");
            }

            print(tree.left, tree.value, -1);
            print(tree.right, tree.value, 1);
        }
    }

    public void print() {
        if (root != null) {
            print(root, root.value, 0);
        }
    }

    public static void main(String[] args) {
//        int arr[] = {4, 3, 6, 5, 7, 8};
        int arr[] = {50, 20, 60, 10, 30, 40};

        AVLTree<Integer> tree = new AVLTree<>();

        System.out.printf("== 依次添加: ");
        for (int i = 0; i < arr.length; i++) {
//             System.out.printf("%d ", arr[i]);
            tree.insert(arr[i]);
            tree.preOrder();
            System.out.println();
        }

//        System.out.printf("\n== 前序遍历: ");
//        tree.preOrder();
//
//        System.out.printf("\n== 中序遍历: ");
//        tree.inOrder();
//
//        System.out.printf("\n== 后序遍历: ");
//        tree.postOrder();
//        System.out.printf("\n");
//
//        System.out.printf("== 高度: %d\n", tree.high());
//        System.out.printf("== 最小值: %d\n", tree.minimum());
//        System.out.printf("== 最大值: %d\n", tree.maximum());
//        System.out.printf("== 树的详细信息: \n");
//        tree.print();
//
//        int removeKey = 8;
//        System.out.printf("\n== 删除根节点: %d", removeKey);
//        tree.remove(removeKey);
//
//        System.out.printf("\n== 高度: %d", tree.high());
//        System.out.printf("\n== 中序遍历: ");
//        tree.inOrder();
//        System.out.printf("\n== 树的详细信息: \n");
//        tree.print();
//
//        // 销毁二叉树
//        tree.destroy();
    }

}