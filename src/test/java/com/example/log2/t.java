package com.example.log2;

public class t {

    public static void main(String[] args)  throws Exception{
        try {
            String a = "2022-08-07 00:00:00#2022-08-08 23:59:59";
            String substring = a.substring(0, a.indexOf("#"));
            String[] split = a.split("#");
            System.out.println(substring);
            for (String s : split) {
                System.out.println(s);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
