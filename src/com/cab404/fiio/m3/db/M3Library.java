package com.cab404.fiio.m3.db;

import com.cab404.fiio.m3.db.data.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
public class M3Library {
    // 'c' means it is an char size index. 'b' is for byte indexes
    private static final int lib_b_formatIndex = 148;
    private static final int lib_c_nameIndex = 0;
    private static final int lib_c_authorIndex = 28;
    private static final int lib_c_albumIndex = 42;
    private static final int lib_c_genreIndex = 56;
    private static final int lib_c_trackIndex = 70;
    private static final int lib_b_fileIdIndex = 84 * 2;
    public static final int lib_b_fileIdSize = 64;
    private static final int lib_b_entrySize = 256;


    public static Song parseLibLine(byte[] bytesArray){
        ByteBuffer bytes = ByteBuffer.wrap(bytesArray);
        CharBuffer le = ByteBuffer.wrap(bytesArray).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();

        if (le.get(0) != Utils.tagStartCharacter) return null;
        Song song = new Song();

        byte[] formatChars = new byte[3];
        bytes.position(lib_b_formatIndex);
        bytes.get(formatChars, 0, 3);

        song.format = new String(formatChars);
        song.author = Utils.extractTag(le, lib_c_authorIndex);
        song.album = Utils.extractTag(le, lib_c_albumIndex);
        song.genre = Utils.extractTag(le, lib_c_genreIndex);
        song.index = Utils.extractTag(le, lib_c_trackIndex);
        song.name = Utils.extractTag(le, lib_c_nameIndex);

        bytes.position(lib_b_fileIdIndex);
        bytes.get(song.file, 0, lib_b_fileIdSize);

        return song;
    }


    public static List<Song> readDB(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);

        List<Song> songs = new ArrayList<>();
        byte[] bytes = new byte[lib_b_entrySize];
        while (is.read(bytes) == bytes.length) {
            Song song = M3Library.parseLibLine(bytes);
            if (song != null) {
                songs.add(song);
            }
        }
        is.close();

        return songs;
    }
}
