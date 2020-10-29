package com.haojiangbo.utils;

import org.apache.commons.lang3.StringUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期处理
 */
public class DateUtils {
    /**
     * 时间格式(yyyy-MM-dd)
     */
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 时间格式(yyyy-MM-dd HH:mm:ss)
     */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 时间格式(HH:mm:ss)
     */
    public final static String TIME = "HH:mm:ss";

    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }


    public static Date pasre(String date, String pattern)  {
        try {
            if (!StringUtils.isEmpty(date)) {
                SimpleDateFormat df = new SimpleDateFormat(pattern);
                return df.parse(date);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return null;
    }

    /**
     * 计算距离现在多久，非精确
     *
     * @param date
     * @return
     */
    public static String getTimeBefore(Date date) {
        Date now = new Date();
        long l = now.getTime() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        String r = "";
        if (day > 0) {
            r += day + "天";
        } else if (hour > 0) {
            r += hour + "小时";
        } else if (min > 0) {
            r += min + "分";
        } else if (s > 0) {
            r += s + "秒";
        }
        r += "前";
        return r;
    }

    /**
     * 计算距离现在多久，精确
     *
     * @param date
     * @return
     */
    public static String getTimeBeforeAccurate(Date date) {
        Date now = new Date();
        long l = now.getTime() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        String r = "";
        if (day > 0) {
            r += day + "天";
        }
        if (hour > 0) {
            r += hour + "小时";
        }
        if (min > 0) {
            r += min + "分";
        }
        if (s > 0) {
            r += s + "秒";
        }
        r += "前";
        return r;
    }



    public static Date getCustomTime(int h){
        return getCustomTime(h,0,0,0);
    }


    public static Date getCustomTime(int h,int m){
        return getCustomTime(h,m,0,0);
    }


    public static Date getCustomTime(int h,int m, int s){
        return getCustomTime(h,m,s,0);
    }


    public static Date getCustomTime(int h,int m, int s ,int ss){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE,m);
        calendar.set(Calendar.SECOND,s);
        calendar.set(Calendar.MILLISECOND,ss);
        return calendar.getTime();
    }

    public static Date getCustomTime(int d, int h,int m, int s ,int ss){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, d);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE,m);
        calendar.set(Calendar.SECOND,s);
        calendar.set(Calendar.MILLISECOND,ss);
        return calendar.getTime();
    }

    public static Date getCustomTimebyDay(Date time ,int h,int m, int s ,int ss){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE,m);
        calendar.set(Calendar.SECOND,s);
        calendar.set(Calendar.MILLISECOND,ss);
        return calendar.getTime();
    }


    /**
     * 当前时间只传入时分秒  得到今天的当前时间
     * @param date
     * @return
     */
    public static Date getCustomTimeHms(Date date){
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(date);
        return getCustomTime(startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE),
                startTime.get(Calendar.SECOND),
                startTime.get(Calendar.MILLISECOND)
        )  ;
    }

    /**
     * 当前时间只传入时分秒  得到今天的当前时间
     * @param date
     * @return
     */
    public static Date getCustomTimeHmsAdd1(Date date){
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(date);
        return getCustomTime(
                1,
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE),
                startTime.get(Calendar.SECOND),
                startTime.get(Calendar.MILLISECOND)
        )  ;
    }


    /**
     * 得到两个日期之间的每一天
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<Date> getStartTimeAndEndTimeEveryDay(
            Date startTime, Date endTime
    ){
        List<Date> lDate = new ArrayList<Date>();
        lDate.add(startTime);
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(startTime);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(endTime);
        // 测试此日期是否在指定日期之后
        while (endTime.after(calBegin.getTime()))  {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(calBegin.getTime());
        }
        return lDate;
    }



    /**
     * 计算开始日期到结束日期之间的天数
     * @param startTime
     * @param endTime
     * @return
     * @throws ParseException
     */
    public static int caculateTotalTime(String startTime,String endTime) throws ParseException{
        SimpleDateFormat formatter =   new SimpleDateFormat( "yyyy-MM-dd" );
        Date date1=null;
        Date date = formatter.parse(startTime);
        long ts = date.getTime();
        date1 =  formatter.parse(endTime);
        long ts1 = date1.getTime();
        long ts2=ts1-ts;
        int totalTime = 0;
        totalTime=(int) (ts2/(24*3600*1000)+1);
        return totalTime;
    }

    /**
     * 计算开始日期到结束日期之间的天数
     * @param startTime
     * @param endTime
     * @return
     * @throws ParseException
     */
    public static int caculateTotalTime(long  startTime,long endTime) throws ParseException{
        long ts = startTime;
        long ts1 = endTime;
        long ts2=ts1-ts;
        int totalTime = 0;
        totalTime=(int) (ts2/(24*3600*1000)+1);
        return totalTime;
    }

    /**
     * 获取当月开始时间

     * @return
     */
    public static Date getMonthStartTime(Date date) {
        Calendar calendar = Calendar.getInstance();// 获取当前日期
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取当月的结束时间
     * @return
     */
    public static Date getMonthEndTime(Date date) {
        Calendar calendar = Calendar.getInstance();// 获取当前日期
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));// 获取当前月最后一天
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 传入一个时间，得到今天是星期几
     * @param date
     * @return
     */
    public static String getWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int i = calendar.get(Calendar.DAY_OF_WEEK);
        String result = "星期";
        switch (i) {
            case 1:
                result += "日";
                break;
            case 2:
                result += "一";
                break;
            case 3:
                result += "二";
                break;
            case 4:
                result += "三";
                break;
            case 5:
                result += "四";
                break;
            case 6:
                result += "五";
                break;
            case 7:
                result += "六";
                break;
            default:
                break;
        }
        return result;
    }
}
