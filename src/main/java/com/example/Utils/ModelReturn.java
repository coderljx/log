package com.example.Utils;

import com.example.Pojo.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ModelReturn {
    private final Logger mylog = LoggerFactory.getLogger(ModelReturn.class);

    public static Model Time() throws Exception {
        String CurrentTime = TimeUtils.ParseDate(new Date());
        String[] times = new String[7];
        for (int i = 0; i < times.length; i++) {
            Calendar c = Calendar.getInstance();
            if (i == 0) times[i] = ParseString(CurrentTime, CurrentTime);

            if (i == 1) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                Date time = c.getTime();
                times[i] = ParseString(CurrentTime, time);
            }
            if (i == 2) {
                c.add(Calendar.DAY_OF_MONTH, -2);
                Date time = c.getTime();
                times[i] = ParseString(CurrentTime, time);
            }
            if (i == 3) {
                c.add(Calendar.DAY_OF_WEEK, -7);
                Date time = c.getTime();
                times[i] = ParseString(CurrentTime, time);
            }
            if (i == 4) {
                c.add(Calendar.DAY_OF_WEEK, -14);
                Date time = c.getTime();
                times[i] = ParseString(CurrentTime, time);
            }
            if (i == 5) {
                c.add(Calendar.MONTH, -1);
                Date time = c.getTime();
                times[i] = ParseString(CurrentTime, time);
            }
            if (i == 6) {
                c.add(Calendar.MONTH, -3);
                Date time = c.getTime();
                times[i] = ParseString(CurrentTime, time);
            }
        }
        List<Model.label> labelList = new ArrayList<>();

        Model model = new Model();
        model.setField("recorddate");
        model.setLabel("range");
        model.setOperator("时间");
        model.setType("radio");
        model.setDatatype("date");
        model.setCanInput("no");
        String[] values = new String[]{"一天内", "两天内", "三天内", "一周内", "两周内", "一月内", "三月内"};
        int i = 0;
        for (String value : values) {
            Model.label label = new Model.label();
            label.setLabel(value);
            label.setValue(times[i]);
            labelList.add(label);
            i++;
        }
        model.setOtions(labelList);
        return model;
    }

    /**
     * 设置固定值，
     *
     * @param end 必须为已经转换后的时间，不然会报错
     * @return
     */
    private static String ParseString(String current, String end) {
        String[] s1 = current.split(" ");
        String[] s2 = end.split(" ");
        String value = s2[0] + "#" + s1[0];
        return value;
    }

    private static String ParseString(String current, Date date) throws Exception {
        String s1 = TimeUtils.ParseDate(date);
        return ParseString(current, s1);
    }


}
