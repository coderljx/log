package com.example.log2;

import com.example.Run.ESproperties;
import com.example.Utils.TimeUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
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

        Instant instant = Instant.ofEpochMilli(TimeUtils.Parselong("2000-01-01 00:00:00"));
        System.out.println(instant);

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
