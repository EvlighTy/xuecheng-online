package com.xuecheng.checkcode.utils;

import java.util.Random;

public class RandomCodeUtil {

    //生成随机六位数手机验证码
    public static String getSixFigureCode(){
        Random r = new Random();
        StringBuilder smsCode = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            smsCode.append(r.nextInt(10));
        }
        return smsCode.toString();
    }

}
