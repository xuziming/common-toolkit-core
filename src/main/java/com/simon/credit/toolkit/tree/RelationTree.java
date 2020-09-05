package com.simon.credit.toolkit.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 关系树
 * @author XUZIMING 2020-08-29
 */
public class RelationTree<T extends RelationNode> {

    /** 根节点 */
    private T root;

    /** 类别节点 */
    private Map<String, T> categoryNodes;

    /** 叶子节点 */
    private Map<String, List<T>> leafNodes;

    public RelationTree(T root) {
        this.root = root;
        this.categoryNodes = new TreeMap<>();
        this.leafNodes = new TreeMap<>();
    }

    public void reverseBuild(List<T> queryLeafNodes) {
        if (queryLeafNodes == null || queryLeafNodes.isEmpty()) {
            return;
        }

        for (T leafNode : queryLeafNodes) {
            // 处理类别节点
            getAndPutIfAbsent(categoryNodes, leafNode.getColor(), buildCategoryNode(root, leafNode));

            // 处理叶子节点
            List<T> subLeafNodes = getAndPutIfAbsent(leafNodes, leafNode.getColor(), new ArrayList<>(10));
            subLeafNodes.add(leafNode);
        }
    }

    private int categoryDataId = 2;

    private T buildCategoryNode(T root, T leafNode) {
        // TODO 补充类别节点构建逻辑代码
        RelationNode relationNode = new RelationNode();
        int cate = categoryDataId++;
        relationNode.setDataId(Integer.toString(cate));
        relationNode.setColor(Integer.toString(cate));
        return (T) relationNode;
    }

    private <K, V> V getAndPutIfAbsent(Map<K, V> map, K key, V defaultIfAbsent) {
        V value = map.get(key);
        if (value != null) {
            return value;
        }

        if (defaultIfAbsent == null) {
            return null;
        }

        map.put(key, defaultIfAbsent);
        return defaultIfAbsent;
    }

    public static void main(String[] args) {
        RelationNode root = new RelationNode();
        root.setDataId("1");
        root.setColor("8");

        RelationTree<RelationNode> tree = new RelationTree<>(root);

        int leafNo = 1;

        for (int i = 1; i <= 6; i++) {
            List<RelationNode> leafNodes = new ArrayList<>(10);
            int categoryColor = i + 10;
            for (int j = 0; j < 20; j++) {
                int dataId = leafNo++;
                RelationNode node = new RelationNode();
                node.setDataId(String.valueOf(dataId));
                node.setColor(String.valueOf(categoryColor));
                leafNodes.add(node);
            }
            tree.reverseBuild(leafNodes);
        }

        System.out.println(tree.root);
        System.out.println(tree.categoryNodes);
        System.out.println(tree.leafNodes);
    }

    private static int tenPower(int powerCount) {
        int result = 1;
        for (int i = 0; i < powerCount; i++) {
            result *= 10;
        }
        return result;
    }

}
