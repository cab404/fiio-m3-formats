package com.cab404.fiio.m3.db;

import com.cab404.fiio.m3.db.data.Song;

import java.nio.CharBuffer;

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

    public static Song parseLibLine(byte[] bytes, CharBuffer le){
        if (le.get(0) != Utils.tagStartCharacter) return null;
        Song song = new Song();
        song.format = new String(bytes, lib_b_formatIndex, 3);
        song.author = Utils.extractTag(le, lib_c_authorIndex);
        song.album = Utils.extractTag(le, lib_c_albumIndex);
        song.genre = Utils.extractTag(le, lib_c_genreIndex);
        song.index = Utils.extractTag(le, lib_c_trackIndex);
        song.name = Utils.extractTag(le, lib_c_nameIndex);

        System.arraycopy(bytes, lib_b_fileIdIndex, song.file, 0, lib_b_fileIdSize);

        return song;
    }
}
