package com.cab404.fiio.m3.db;

import java.nio.CharBuffer;

/**
 * @author cab404
 */
public class Utils {
    public static final char tagStartCharacter = 0xfeff;

    public static String extractTag(CharBuffer le, int start){
        int end = start;
        if (le.get(end) == tagStartCharacter)
            while (le.get(end) != 0)
                end++;
        else
            return null;
        return le.subSequence(start, end).toString();
    }

    public static void writeBytes(byte[] bytes) {
        boolean del = true;
        for (int i = 0; i < bytes.length; i++) {
            System.out.printf("%02x", bytes[i]);
//            System.out.print((del = !del) ? " " : "");
        }
    }
}
