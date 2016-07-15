package com.cab404.fiio.m3.db.data;

import com.cab404.fiio.m3.db.M3Library;

/**
 * @author cab404
 */
public class Song {
    public String album, name, author, genre, format, index;
    public byte[] file = new byte[M3Library.lib_b_fileIdSize];
}
