package com.example.log2;

import java.util.HashMap;
import java.util.Map;

public class t {

    public static void main(String[] args)  throws Exception{
        try {
            Map<String,String> map = new HashMap<>();

            String a = "123";
            String concat = a.concat(".keyword");
            System.out.println(a);
            System.out.println(concat);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
