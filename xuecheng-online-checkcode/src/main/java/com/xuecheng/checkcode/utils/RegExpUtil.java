package com.xuecheng.checkcode.utils;

import java.util.regex.Pattern;

public class RegExpUtil {

    /**
     * 验证手机号码   手机或者固话 (注意:只要满足11位，判定校验成功)
     *
     * @param phone 匹配电话
     * @return true 验证成功返回true，验证失败返回false
     */
    public static boolean checkPhone(String phone) {
        String regexGH = "^(\\(\\d{3,4}\\)|\\d{3,4}-|\\s)?\\d{8}$";//固话
        String regexSJ = "^(86)*0*\\d{11}";//手机号
        return Pattern.matches(regexGH, phone) || Pattern.matches(regexSJ, phone);
    }

    /**
     * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
     *
     * @param mobile 移动、联通、电信运营商的号码段
     *               <p>移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
     *               、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）</p>
     *               <p>联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）</p>
     *               <p>电信的号段：133、153、180（未启用）、189</p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkMobile(String mobile) {
        String regex = "(\\+\\d+)?1[34578]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }


    /**
     * 邮箱验证
     *
     * @param email email地址，格式：zhangsan@zuidaima.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkEmail(String email) {
        String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        return Pattern.matches(regex, email);
    }

}
