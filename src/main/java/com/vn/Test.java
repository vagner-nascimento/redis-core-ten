package com.vn;

import com.vn.util.TryParse;

import java.util.Map;

public class Test {
    public static void main(String[] args) {
        String str = "524863254";
        Long num = TryParse.toLong(str);

        System.out.println("Num: " + num);
    }
}
