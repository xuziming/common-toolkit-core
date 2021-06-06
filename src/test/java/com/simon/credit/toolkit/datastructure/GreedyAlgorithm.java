package com.simon.credit.toolkit.datastructure;

import java.util.*;

/**
 * 贪心算法-电台覆盖问题
 * @author xuziming 2021-06-04
 */
public class GreedyAlgorithm {

    public static void main(String[] args) {
        // 创建广播电台, 放入到Map
        Map<String, Set<String>> broadcasts = new HashMap<>();

        // 将各个电台放入到broadcasts
        Set<String> k1 = new HashSet<>();
        k1.add("北京");
        k1.add("上海");
        k1.add("天津");

        Set<String> k2 = new HashSet<>();
        k2.add("广州");
        k2.add("北京");
        k2.add("深圳");

        Set<String> k3 = new HashSet<>();
        k3.add("成都");
        k3.add("上海");
        k3.add("杭州");

        Set<String> k4 = new HashSet<>();
        k4.add("上海");
        k4.add("天津");

        Set<String> k5 = new HashSet<>();
        k5.add("杭州");
        k5.add("大连");

        // 加入到map
        broadcasts.put("K1", k1);
        broadcasts.put("K2", k2);
        broadcasts.put("K3", k3);
        broadcasts.put("K4", k4);
        broadcasts.put("K5", k5);

        // allAreas存放所有的地区
        Set<String> allUncoveringAreas = new HashSet<>();
        for (Set<String> value : broadcasts.values()) {
            allUncoveringAreas.addAll(value);
        }

        // 创建ArrayList, 存放选择的电台集合
        List<String> selects = new ArrayList<>();

        // 定义给maxKey ， 保存在一次遍历过程中，能够覆盖最大未覆盖的地区对应的电台的key
        // 如果maxKey不为null , 则会加入到selects
        String maxKey = null;
        while (allUncoveringAreas.size() != 0) {// 如果allUncoveringAreas不为0, 则表示还没有覆盖到所有的地区
            // 每进行一次while,需要
            maxKey = null;

            // 遍历broadcasts
            for (Map.Entry<String, Set<String>> entry : broadcasts.entrySet()) {
				// 存放遍历过程中的电台覆盖的地区和当前还没有覆盖的地区的交集
				Set<String> uncoveringSet = parseUncoveringSet(allUncoveringAreas, entry.getValue());
				if (uncoveringSet.size() == 0) {
					continue;
				}

				// 如果当前这个集合包含的未覆盖地区的数量, 比maxKey指向的集合地区还多, 则需要重置maxKey
				// uncoveringSet.size() > broadcasts.get(maxKey).size())体现出贪心算法特点: 每次都选择最优的
				if (maxKey == null || uncoveringSet.size() > broadcasts.get(maxKey).size()) {
					maxKey = entry.getKey();
				}
            }

            // maxKey != null, 就应该将maxKey加入selects
            if (maxKey != null) {
                selects.add(maxKey);
                // 将maxKey指向的广播电台覆盖的地区，从allUncoveringAreas去掉
                allUncoveringAreas.removeAll(broadcasts.get(maxKey));
            }
        }

        System.out.println("得到的选择结果是" + selects);//[K1,K2,K3,K5]
    }

    /**
     * 解析还没覆盖地区的集合
     * @param allAreas 所有区域
     * @param areas    一次遍历过程中能够覆盖的地区
     * @return
     */
    private static final Set<String> parseUncoveringSet(Set<String> allAreas, Set<String> areas) {
        // 定义一个临时的集合， 在遍历的过程中，存放遍历过程中的电台覆盖的地区和当前还没有覆盖的地区的交集
        Set<String> tempSet = new HashSet<>(areas);

        // 求出tempSet和allAreas集合的交集, 交集会赋给tempSet
        tempSet.retainAll(allAreas);

        return tempSet;
    }

}