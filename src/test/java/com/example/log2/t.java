package com.example.log2;

import com.example.Run.Excel;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class t {

    public static void main(String[] args)  throws Exception{
//        trans("/Users/brother/Desktop/Log2-0.0.1-SNAPSHOT.jar");


//        Workbook workbook = Excel.CreateHeader("class","runs","sa","sad","sda","sda","sdasd");
        String da[] = new String[]{"1","2","s","s","3","s"};
        Workbook woe = Excel.CreateData("ll",da);



        FileOutputStream fileOutputStream = new FileOutputStream("/Users/brother/Desktop/" + "用户信息表-XLS.xls");
        woe.write(fileOutputStream);
        fileOutputStream.close();


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
