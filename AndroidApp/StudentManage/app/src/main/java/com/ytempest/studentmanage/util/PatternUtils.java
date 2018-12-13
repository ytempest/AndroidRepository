package com.ytempest.studentmanage.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ytempest
 *         Description：
 */
public class PatternUtils {


    /**
     * 手机号码的正则匹配
     */
    private static Pattern PHONE_PATTERN = Pattern.compile("^1[34578]\\d{9}$");

    public static boolean isPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

}
