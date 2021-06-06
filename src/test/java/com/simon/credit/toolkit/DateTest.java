package com.simon.credit.toolkit;

import com.simon.credit.toolkit.common.CommonToolkits;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTest {

    public static void main(String[] args) {
        String workStart = "09:00";
        String workEnd   = "18:00";
        String restStart = "12:30";
        String restEnd   = "14:00";

        Date workStartTime = CommonToolkits.parseDate(workStart, "HH:mm");
        Date workEndTime   = CommonToolkits.parseDate(workEnd  , "HH:mm");
        Date restStartTime = CommonToolkits.parseDate(restStart, "HH:mm");
        Date restEndTime   = CommonToolkits.parseDate(restEnd  , "HH:mm");

        System.out.println(CommonToolkits.formatDate(workStartTime));
        System.out.println(CommonToolkits.formatDate(workEndTime));
        System.out.println(CommonToolkits.formatDate(restStartTime));
        System.out.println(CommonToolkits.formatDate(restEndTime));

//        long duration = restStartTime.getTime() - workStartTime.getTime() + workEndTime.getTime() - restEndTime.getTime();
//        System.out.println(duration / 3600000);

        double diff = CommonToolkits.timeDiff(workStartTime, restStartTime, TimeUnit.DAYS);
        System.out.println(diff);
    }

}