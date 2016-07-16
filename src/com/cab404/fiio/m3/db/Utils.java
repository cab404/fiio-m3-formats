package com.cab404.fiio.m3.db;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

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

    public static void writeTag(ByteBuffer to, String tag) {
        if (tag == null || tag.isEmpty()) return;
        if (tag.charAt(0) != tagStartCharacter)
            to.putChar((char) 0xfffe);
        to.put(tag.getBytes(Charset.forName("UTF-16LE")));
        if (tag.indexOf(0) == -1)
            to.putChar((char) 0);
    }

    public static void writeBytes(byte[] bytes) {
        boolean del = true;
        for (int i = 0; i < bytes.length; i++) {
            System.out.printf("%02x", bytes[i]);
//            System.out.print((del = !del) ? " " : "");
        }
    }
}
