package com.example.log2;

import org.springframework.data.repository.cdi.Eager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class t {

    public static void main(String[] args)  throws Exception{
        try {
            String[] opear = new String[]{"=",">","<",">=","<="};
            boolean contains = Arrays.asList(opear).contains("=");
            System.out.println(contains);


        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
