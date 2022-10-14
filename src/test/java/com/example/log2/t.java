package com.example.log2;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class t {
    static {

    }

    public static void main(String[] args)  throws Exception{
//        trans("/Users/brother/Desktop/Log2-0.0.1-SNAPSHOT.jar");


//        Workbook workbook = Excel.CreateHeader("", "sa", "sa", "sa");
//        Excel.CreateData("sadasd","2022-01-12 10:21:22","adasdasjdlkjasjdlasjdjasljdlsajldsad");
//
//
//        workbook.write(new FileOutputStream("/Users/brother/Desktop/1.xls"));


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
