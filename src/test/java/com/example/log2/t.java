package com.example.log2;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class t {
    static {
        System.out.println("sas");
    }

    public static void main(String[] args)  throws Exception{
//        trans("/Users/brother/Desktop/Log2-0.0.1-SNAPSHOT.jar");

        String a = "sad,sdasdas";
        String[] c = a.split(",");


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
