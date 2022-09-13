package com.example.Utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class TimeUtils {

    private static String Validate(String date){
        if (date.length() == 10 || date.length() == 19){
            String time;
            if (date.contains(":") && date.length() == 19){
                time = "yyyy-MM-dd HH:mm:ss";
            }else {
                time = "yyyy-MM-dd";
            }
            return time;
        }
        return null;
    }

    public static long Parselong(String date) throws ParseException {
        if (Validate(date) == null)  throw new ParseException("类型错误",0);

        String Time = Validate(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Time);
        Date parse = simpleDateFormat.parse(date);
        return parse.getTime();
    }

    public static long Parselong(Date date){
        return date.getTime();
    }

    public static Date ParseDate(String date) throws ParseException {
        if (Validate(date) == null)   throw new ParseException("类型错误",0);

        String Time = Validate(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Time);
        return new Date(Parselong(date));
    }

    /**
     * 转换成times，写入mysql
     * @return
     */
    public static Timestamp ParseTimestamp(String date) throws ParseException {
        Date date1 = ParseDate(date);
        return new Timestamp(date1.getTime());
    }

    /**
     * 对date 进行格式化，按照 yyyy-MM-dd HH:mm:ss 格式显示
     * @param date
     */
    public static String ParseDate(Date date) {
        String time = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(time);
        return simpleDateFormat.format(date);
    }

    /**
     *
     * @param date
     * @param type  1 : yyyy-MM-dd HH:mm:ss
     *              2 : yyyy-MM-dd
     *              3 : yyyy-MM
     * @return
     * @throws Exception
     */
    public static String ParseDate(Date date,int type) throws ParseException{
        String time = "yyyy-MM-dd HH:mm:ss";
        String times = "yyyy-MM-dd";
        String times2 = "yyyy-MM";
        String koko = "";
        if (type == 1) koko = time;

        if (type == 2) koko = times;

        if (type == 3) koko = times2;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(koko);
        return simpleDateFormat.format(date);
    }


    public static Instant ParseInstant(String date) throws ParseException {
        long parselong = Parselong(date);
        return Instant.ofEpochMilli(parselong);
    }


}
