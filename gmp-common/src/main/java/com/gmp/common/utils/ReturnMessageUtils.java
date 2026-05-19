package com.gmp.common.utils;

import com.gmp.common.enums.GmpResponseCodeMsg;
import com.gmp.common.enums.ResponseCodeMsg;

public class ReturnMessageUtils {


    public static String getMessage(ResponseCodeMsg codeMsg, String[] params) {
        String message = codeMsg.getMessage();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replace("{" + i + "}", params[i]);
            }
            return message;
        }
        return message;
    }

    public static String getMessage(GmpResponseCodeMsg codeMsg, String[] params) {
        String message = codeMsg.getMessage();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                message = message.replace("{" + i + "}", params[i]);
            }
            return message;
        }
        return message;
    }


}
