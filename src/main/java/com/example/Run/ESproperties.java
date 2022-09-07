package com.example.Run;

import com.example.Utils.TimeUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@Component
public class ESproperties  {
    private final Logger mylog = LoggerFactory.getLogger(ESproperties.class);

    /**
     * 查询时使用，根据传入的时间解析出对应的时间，解析成对应的索引库名称
     * @param oldName 2022-07-10 00:00:00
     * @return 返回时间的年月日格式  202207
     */
    public String IndexName(String oldName) {
        String[] NewName = oldName.split(" ");
        String res = NewName[0].replaceAll("-","");
        return res.substring(0,6);
    }

    /**
     * 写入数据时使用，根据当前时间生成索引名称
     *
     * @param PreIndex 索引名称的前缀，例如：log，sj
     * @return log202209
     */
    public String currenTime(String PreIndex) throws ParseException  {
        String time = TimeUtils.ParseDate(new Date(), 3);
        String res = time.replaceAll("-", "");
        return PreIndex + res;
    }

    /**
     * 将转换后的日期跟 索引前缀进行拼接
     * @param list
     * @return
     */
    public String[] parseIndexName(List<String> list,String prie) {
        if (list.size() == 0) return new String[0];

        List<String> newIndex = new ArrayList<>();
        for (String s : list) {
            newIndex.add(prie + s);
        }
        return newIndex.toArray(new String[]{});
    }

    /**
     * 计算两个时间相差的月份, 返回他们直接间隔的每一个月，用以查询es索引库
     * @param time1 2022-01-01 00:00:00
     * @param time2 2022-02-01 00:00:00
     * @return 202201,202202
     */
    public List<String> suxMonth (String time1,String time2) throws ParseException  {
        Calendar starTime = Calendar.getInstance();
        starTime.setTime(TimeUtils.ParseDate(time1));
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(TimeUtils.ParseDate(time2));

        int startYear =  starTime.get(Calendar.YEAR);
        int endYear =  endTime.get(Calendar.YEAR);
        List<String> mount = new ArrayList<>();
        // 如果查询开始时间和结束时间是同一个年份
        if (startYear == endYear) {
            int startMonth = starTime.get(Calendar.MONTH);
            int endMonth = endTime.get(Calendar.MONTH);
            // 如果是同一个月份
            if (startMonth == endMonth) {
                mount.add(IndexNameFromYearAndMonth(startYear,startMonth));
                return mount;
            }
            mount.add(IndexNameFromYearAndMonth(startYear,startMonth));
            for (int i = 0; i < endMonth - startMonth; i ++) {
                starTime.add(Calendar.MONTH , 1);
                int startMonthAdd = starTime.get(Calendar.MONTH);
                if (startMonthAdd != endMonth)  mount.add(IndexNameFromYearAndMonth(startYear,startMonthAdd));
            }
            mount.add(IndexNameFromYearAndMonth(endYear,endMonth));
        }else {
            mount.addAll(suxYear(starTime, endTime));
        }
        return mount;
    }

    /**
     * 如果查询时间是跨越年份的
     * @param starTime
     * @param endTime
     */
    private List<String> suxYear(Calendar starTime,Calendar endTime) throws ParseException {
        int startYear =  starTime.get(Calendar.YEAR);
        int endYear =  endTime.get(Calendar.YEAR);
        int startMonth = starTime.get(Calendar.MONTH);
        int endMonth = endTime.get(Calendar.MONTH);
        List<String> mount = new ArrayList<>();
        for (int i = 1; i <= endYear - startYear; i++) {
            int startMonthEnd = 11 - startMonth;
            for (int h = 0; h < startMonthEnd + 1; h++) {
                mount.add(IndexNameFromYearAndMonth(startYear,startMonth + h ));
            }
            starTime.add(Calendar.YEAR,1);
            int startYearAdd = starTime.get(Calendar.YEAR);
            String startYearAddStartMount = startYearAdd + "-01-01 00:00:00";
            if (startYearAdd != endYear) {
                Calendar starTimeCurr = Calendar.getInstance();
                starTimeCurr.clear();
                starTimeCurr.set(Calendar.YEAR,startYearAdd);
                starTimeCurr.roll(Calendar.DAY_OF_YEAR,-1);
                String startYearAddEndMount = TimeUtils.ParseDate(starTimeCurr.getTime());
                List<String> list = this.suxMonth(startYearAddStartMount, startYearAddEndMount);
                mount.addAll(list);
            }else {
                List<String> list = this.suxMonth(startYearAddStartMount, TimeUtils.ParseDate(endTime.getTime()));
                mount.addAll(list);
            }

        }
        return mount;
    }

    /**
     * 根据年月生成对应的索引名称
     * @param year  2022
     * @param month 1
     * @return  202201
     */
    public String IndexNameFromYearAndMonth(int year,int month){
        month += 1;
        String NewMonth = "";
        if (month < 10) {
            NewMonth = "0" + month;
        }else {
            NewMonth = "" + month;
        }
        return year + NewMonth;
    }



}
