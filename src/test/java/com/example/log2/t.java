package com.example.log2;

import com.example.Run.ESproperties;
import com.example.Utils.TimeUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class t {
    static {

    }

    public static void main(String[] args)  throws Exception{
//        trans("/Users/brother/Desktop/Log2-0.0.1-SNAPSHOT.jar");

        Calendar starTime = Calendar.getInstance();
        starTime.setTime(TimeUtils.ParseDate("2022-04-02 00:00:00"));
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(TimeUtils.ParseDate("2023-06-02 00:00:00"));

        int startYear =  starTime.get(Calendar.YEAR);
        int endYear =  endTime.get(Calendar.YEAR);
        int startMonth = starTime.get(Calendar.MONTH);
        int endMonth = endTime.get(Calendar.MONTH);

        Date parselong = TimeUtils.ParseTimestamp("2022-09-10 00:00:00");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(parselong);
        System.out.println(timestamp);

    }

    public static void trans(String file) throws Exception{
        Socket socket = new Socket("10.0.0.183",2000);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        int i = 0;
        OutputStream outputStream = socket.getOutputStream();
        while ((i = inputStream.read(bytes)) != -1){
            outputStream.write(bytes,0,i);
        }
        socket.shutdownOutput();
    }


}
