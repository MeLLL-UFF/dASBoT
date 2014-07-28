/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.util;

import java.util.Calendar;

/**
 *
 * @author Victor
 */
public class Time {
    public static String getTime() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        return String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
        //System.out.println("");
    }

    public static String getTime(Box<Long> time) {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        long resp = 0;
        resp += millis;
        resp += second * 1000;
        resp += minute * 60 * 1000;
        resp += hour * 60 * 60 * 1000;
        resp += day * 24 * 60 * 60 * 1000;
        resp += month * 30 * 24 * 60 * 60 * 1000;
        resp += year * 365 * 30 * 24 * 60 * 60 * 1000;

        time.setContent(resp);

        return String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
    }
    
    public static String getDiference(Box<Long> begin, Box<Long> end) {
        return getDiference(begin.getContent(), end.getContent());
    }
    
    public static String getDiference(Long begin, Long end) {
        long dif = Math.round((end - begin) / 1000.0);
        long hour, min, sec;
        if (dif < 60) {
            sec = dif;
            return sec + "s";
        } else if (dif < 60 * 60) {
            sec = dif % 60;
            min = dif / 60;
            return min + "min" + sec + "s";
        } else {
            hour = dif / 3600;
            dif = dif % (3600);
            min = dif / 60;
            sec = dif % 60;
            return hour + "h" + min + "min" + sec + "s";
        }
    }
}
