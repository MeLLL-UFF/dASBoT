/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import java.util.Calendar;

/**
 * Class used to estimate time between processes.
 *
 * @author Victor Guimarães
 */
public class Time {

    /**
     * Get the current time and format it as a {@link String}.
     *
     * @return the current time as {@link String}.
     */
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
    }

    /**
     * Get the current time and format it as a {@link String} and also load the
     * time as a long inside a {@link Box<Long>}. The time is formated by
     * summing everything as miliseconds.
     *
     * @param time the {@link Box<Long>} with the time in it.
     * @return the current time as {@link String}.
     */
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

    /**
     * Calculate the difference between to times.
     *
     * @param begin the begin.
     * @param end the end.
     * @return a formatted {@link String} with the time difference.
     */
    public static String getDiference(Box<Long> begin, Box<Long> end) {
        return getDiference(begin.getContent(), end.getContent());
    }

    /**
     * Calculate the difference between to times.
     *
     * @param begin the begin.
     * @param end the end.
     * @return a formatted {@link String} with the time difference.
     */
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
    
    public static String getFormatedTime(Long time) {
        long dif = Math.round(time / 1000.0);
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
    
    public static long getLongTime(String time) {
        //12h45min37s
        int h = 0, m = 0, s = 0;
        String t = time;
        int index = t.indexOf("h");
        if (index > 0) {
            h = Integer.parseInt(t.substring(0, index));
            t = t.substring(index + 1);
        }
        
        index = t.indexOf("min");
        if (index > 0) {
            m = Integer.parseInt(t.substring(0, index));
            t = t.substring(index + 3);
        }
        
        index = t.indexOf("s");
        if (index > 0) {
            s = Integer.parseInt(t.substring(0, index));
            t = t.substring(index + 1);
        }
        
        long resp = 0;
        resp += s * 1000;
        resp += m * 60 * 1000;
        resp += h * 60 * 60 * 1000;
        
        return resp;
    }
    
}
