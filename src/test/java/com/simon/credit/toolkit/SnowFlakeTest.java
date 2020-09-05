package com.simon.credit.toolkit;

import com.simon.credit.toolkit.lang.SnowFlake;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class SnowFlakeTest {

    public static void main(String[] args) throws Exception{
        BufferedWriter out = new BufferedWriter(new FileWriter("d:/snowflake.log"));

        long begin = System.currentTimeMillis();
        SnowFlake snowFlake = new SnowFlake(2);
        for (int i = 0; i < 10; i++) {
            Long l = snowFlake.nextId();
            String timeStr = Long.toBinaryString(l);
            System.out.println(l + " " + timeStr.length() + " " + timeStr);
            // out.write(l.toString());
            out.write(l.toString());
            out.newLine();
        }

        out.close();
        long end = System.currentTimeMillis();
        System.out.println((end-begin)/1000.0);
//        SnowFlakeOrig snowFlakeOrig = new SnowFlakeOrig(2,3);
//        for (int i = 0; i < 5; i++) {
//            Long l = snowFlakeOrig.nextId();
//            System.out.println(l + " " + Long.toBinaryString(l));
//        }
    }

}