package com.cab404.fiio.m3.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

/**
 * @author cab404
 */
public class M3BaseReader {
    public static void main(String[] args) throws Exception {
        readDB(new File("data/MUSIC.LIB"));
        readPL(new File("data/USERPL2.PL"));
    }

// 'c' means it is an char size index. 'b' is for byte indexes
    public static final int lib_b_entrySize = 256;
    public static final int lib_b_formatIndex = 148;
    public static final int lib_c_nameIndex = 0;
    public static final int lib_c_authorIndex = 28;
    public static final int lib_c_albumIndex = 42;
    public static final int lib_c_genreIndex = 56;
    public static final int lib_c_trackIndex = 70;
    public static final int lib_b_fileIdIndex = 84 * 2;
    public static final int lib_b_fileIdSize = 64;
    public static final char tagStartCharacter = 0xfeff;

    private static void readDB(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);

        byte[] bytes = new byte[lib_b_entrySize];
        int flen = 0;
        CharBuffer le = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();
        while (is.read(bytes) == bytes.length) {
            Song song = parseLibLine(bytes, le);
            if (song != null) {
                System.out.println(song.name + " - " + song.album);
                writeBytes(song.file);
                writeBytes(bytes);
                flen++;
            }
        }
        System.out.println(flen);
        is.close();
    }

    public static String extractTag(CharBuffer le, int start){
        int end = start;
        if (le.get(end) == tagStartCharacter)
            while (le.get(end) != 0)
                end++;
        else
            return null;
        return le.subSequence(start, end).toString();
    }

    public static class Song {
        public String album, name, author, genre, format, index;
        public byte[] file = new byte[lib_b_fileIdSize];
    }

    public static Song parseLibLine(byte[] bytes, CharBuffer le){
        if (le.get(0) != tagStartCharacter) return null;
        Song song = new Song();
        song.format = new String(bytes, lib_b_formatIndex, 3);
        song.author = extractTag(le, lib_c_authorIndex);
        song.album = extractTag(le, lib_c_albumIndex);
        song.genre = extractTag(le, lib_c_genreIndex);
        song.index = extractTag(le, lib_c_trackIndex);
        song.name = extractTag(le, lib_c_nameIndex);

        System.arraycopy(bytes, lib_b_fileIdIndex, song.file, 0, lib_b_fileIdSize);

        return song;
    }

    private static void writeBytes(byte[] bytes) {
        boolean del = true;
        for (int i = 0; i < bytes.length; i++) {
            System.out.printf("%02x", bytes[i]);
            System.out.print((del = !del) ? " " : "");
        }
        System.out.println();
    }

    private static void readPL(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);

        byte[] bytes = new byte[lib_b_entrySize];
        int flen = 0;
        CharBuffer le = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();
        while (is.read(bytes) == bytes.length) {
            String tag = extractTag(le, 52);
            if (tag == null) continue;
            System.out.println(tag);
            writeBytes(bytes);
            flen++;
        }
        System.out.println(flen);
        is.close();
    }

}
