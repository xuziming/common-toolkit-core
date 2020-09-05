package com.simon.credit.toolkit.lang;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 新snowflake算法
 * ([0] -[41bit时间差]-[8bit 机器标识])*10000+random(9999)
 *  （最后10进制4位 方便数据分析做随机样本抽取）
 * 性能：普通mac i5处理器，每秒10万并发
 */
public class SnowFlake {

    /** 起始的时间戳 */
    private final static long START_STMP = 1480166465631L;

    /** 每一部分占用的位数(机器标识占用的位数) */
    private final static long MACHINE_BIT = 8;

    /** 每一部分的最大值 */
    public final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);

    /** 每一部分向左的位移 */
    private final static long TIMESTMP_LEFT = MACHINE_BIT;

    private long machineId;     //机器标识
    private long sequence =  0L;//序列号
    private long lastStmp = -1L;//上一次时间戳
 // private long random   =  0L;//序列号

    private Set<Integer> randomsInMill = new HashSet<Integer>();

    public SnowFlake(long machineId) {
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.machineId = machineId;
    }

    /** 产生下一个ID */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        // 时间回拨检测
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        sequence = 0L;

        // 取9999内随机数
        int random = geUniqRandom(currStmp, lastStmp);

        lastStmp = currStmp;

        // 时间戳部分 | 机器标识部分
        long id = (currStmp - START_STMP) << TIMESTMP_LEFT | machineId ;

        // add random number
        id = id*10000 + random;
        return id;
    }

    /** 生成1ms内不重复的随机数 */
    private int geUniqRandom(long currStmp,long lastStmp){
        int random = getRandom();

        if (currStmp == lastStmp) {
            // 同一毫秒内，随机数发生碰撞检测
            int roop = 5;
            while(--roop > 0) {
                // 如果发生碰撞，重复生成随机数
                if (randomsInMill.contains(random)) {
                    random = getRandom();
                } else {// 没有发生碰撞
                    randomsInMill.add(random);
                    return random;
                }
            }

            if (roop <= 0) {// 尝试5次仍然发生碰撞
                throw new RuntimeException("random collision.  Refusing to generate id");
            }

            //this case never happen
            return random;
        } else {// 进入下一个毫秒
            randomsInMill.clear();
            randomsInMill.add(random);
            return random;
        }
    }

    private int getRandom() {
        Random randomObj=new java.util.Random();
        return randomObj.nextInt(9999);
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

}