package com.example.Utils;

public enum Coco {

    ServerError("服务器错误", -101),
    ParamsError("请求参数错误", -104),
    ParamsNumError("请求参数缺少", -105),
    ParamsNullError("请求参数不可为空", -106),
    ParamsTypeError("请求参数类型错误", -108),
    LogTypeError("日志类型错误", -109),
    IndexNameNotFound("当前索引库不存在",-110),


    ok("Success", 200);


    public String message;
    public Integer code;


    Coco(String success, int i) {
        this.message = success;
        this.code = i;
    }



}
