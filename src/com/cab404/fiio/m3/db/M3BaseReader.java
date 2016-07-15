package com.cab404.fiio.m3.db;

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

    private static void readDB(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);

        List<Song> songs = new ArrayList<>();
        byte[] bytes = new byte[lib_b_entrySize];
        int flen = 0;
        CharBuffer le = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();
        while (is.read(bytes) == bytes.length) {
            Song song = M3Library.parseLibLine(bytes, le);
            if (song != null) {
                songs.add(song);
                System.out.println(song.name + " - " + song.album);
                flen++;
            }
        }
        System.out.println(flen);
        is.close();

        Collections.shuffle(songs);
        for (int i = 0; i < 10; i++) {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            M3Playlist.generatePlaylistEntry(buffer, i + 1, songs.get(i));
            Utils.writeBytes(buffer.array());
        }
    }


    private static void readPL(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);

        byte[] bytes = new byte[lib_b_entrySize];
        int flen = 0;
        CharBuffer le = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();
        while (is.read(bytes) == bytes.length) {
            String tag = Utils.extractTag(le, 52);
            if (tag == null && flen != 0) continue;
            if (tag != null) {
                System.out.println(tag);
                flen++;
            }
            Utils.writeBytes(bytes);
            System.out.println();
        }
        System.out.println(flen);
        is.close();
    }

}
