package com.cab404.fiio.m3.db;

import com.cab404.fiio.m3.db.data.PlaylistEntry;
import com.cab404.fiio.m3.db.data.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cab404
 */
public class M3BaseReader {
    public static void main(String[] args) throws Exception {
        readDB(new File("data/MUSIC.LIB"));
        System.out.println("pl2");
        readPL(new File("data/02/USERPL2.PL"));
        System.out.println("pl3");
        readPL(new File("data/02/USERPL3.PL"));
    }

    private static final int lib_b_entrySize = 256;

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




    private static List<PlaylistEntry> readPL(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        List<PlaylistEntry> entries = new ArrayList<>();

        byte[] bytes = new byte[lib_b_entrySize];
        while (is.read(bytes) == bytes.length) {
            PlaylistEntry entry = M3Playlist.parsePlLine(bytes);
            if (entry == null) entries.add(entry);
        }
        is.close();

        return entries;
    }

}
