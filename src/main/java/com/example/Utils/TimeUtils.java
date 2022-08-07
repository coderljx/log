package com.example.Utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    private static String Time;

    private static String Validate(String date){
        if (date.length() == 10 || date.length() == 19){
            if (date.contains(":") && date.length() == 19){
                Time = "yyyy-MM-dd HH:mm:ss";
            }else {
                Time = "yyyy-MM-dd";
            }
            return  Time;
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
        Date parse = new Date(Parselong(date));
        return parse;
    }

    /**
     * 转换成times，写入mysql
     * @return
     */
    public static Timestamp ParseTimestamp(String date) throws ParseException {
        Date date1 = ParseDate(date);
        return new Timestamp(date1.getTime());
    }




}
