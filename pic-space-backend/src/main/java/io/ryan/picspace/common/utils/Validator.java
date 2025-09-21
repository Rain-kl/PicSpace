package io.ryan.picspace.common.utils;

public class Validator {

    /**
     * 验证命名是否合法, 只能包含字母、数字、下划线，且不能以数字开头，长度在3到20之间
     *
     * @param str
     * @return
     */
    public static Boolean validNaming(String str) {
        String regex = "^(?![0-9])[a-zA-Z0-9_]{3,20}$";
        return str != null && str.matches(regex);
    }
}
