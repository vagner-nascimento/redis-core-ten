package com.vn;

import com.vn.util.TryParse;
import io.lettuce.core.protocol.CommandType;

import java.util.Map;

public class Test {
    public static void main(String[] args) {
        CommandType t = CommandType.valueOf("NADA");

        System.out.println(t);
    }
}
