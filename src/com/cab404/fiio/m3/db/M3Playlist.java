package com.cab404.fiio.m3.db;

import com.cab404.fiio.m3.db.data.PlaylistEntry;
import com.cab404.fiio.m3.db.data.Song;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
public class M3Playlist {

    private static final int pl_b_entrySize = 256;
    private static final int pl_b_indexIndex = 0;
    private static final int pl_b_formatIndex = 12;
    private static final int pl_b_addressIndex = 16;
    private static final int pl_b_addressSizeIndex = 96;
    private static final int pl_b_weirdThingsIndex = 98;
    private static final int pl_b_nameIndex = 104;
    private static final int pl_c_nameIndex = 52;
    private static final long WEIRD_THING = 0xC29F686CC29F0000L;


    public static List<PlaylistEntry> readPL(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        List<PlaylistEntry> entries = new ArrayList<>();

        byte[] bytes = new byte[pl_b_entrySize];
        while (is.read(bytes) == bytes.length) {
            PlaylistEntry entry = M3Playlist.parsePlLine(bytes);
            if (entry != null) entries.add(entry);
        }
        is.close();

        return entries;
    }

    public static void generatePlaylistEntry(ByteBuffer writeTo, PlaylistEntry entry){
        if (writeTo.capacity() < 256) throw new RuntimeException("Buffer is too small for playlist entry!");

        Song song = entry.song;
        int index = entry.index;
        int id = entry.playlistId;

        // index
        writeTo.position(pl_b_indexIndex);
        writeTo.put((byte) (entry.presence ? 1 : 2));
        writeTo.put((byte) (0x00));
        writeTo.put((byte) (0x00));
        writeTo.put((byte) (0x00));
        writeTo.put((byte) (index - 2 > 0 ? index - 2 : 0));
        writeTo.put((byte) 0);
        writeTo.put((byte) index);
        writeTo.put((byte) 0);
        writeTo.putInt(id);

        // format
        writeTo.position(pl_b_formatIndex);
        writeTo.put(song.format.getBytes(Charset.forName("ASCII")), 0, 3);


        // file id
        LongBuffer fileIdBuffer = ByteBuffer.wrap(song.file).asLongBuffer();
        int flen = 0;
        while (fileIdBuffer.get(flen) != 0) flen++;
        writeTo.position(pl_b_addressIndex);
        writeTo.putLong(fileIdBuffer.get(flen - 1));
        for (int i = 0; i < flen; i++)
            writeTo.putLong(fileIdBuffer.get(i));

        // file id size
        writeTo.put(pl_b_addressSizeIndex, (byte) (flen - 1));
        writeTo.put(pl_b_addressSizeIndex + 1, (byte) 0x80);

        // some weird things.
        writeTo.putLong(pl_b_weirdThingsIndex, WEIRD_THING);

        // name
        writeTo.position(pl_b_nameIndex);
        Utils.writeTag(writeTo, song.name);
    }

    public static PlaylistEntry parsePlLine(byte[] bytesArray){
        ByteBuffer bytes = ByteBuffer.wrap(bytesArray);
        if ((bytes.getLong(pl_b_weirdThingsIndex) & ~0xFFFFL) != WEIRD_THING) return null;

        CharBuffer le = ByteBuffer.wrap(bytesArray).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();

        PlaylistEntry entry = new PlaylistEntry();
        entry.presence = bytes.get(0) == 1;
        entry.index = bytes.get(6);
        entry.playlistId = bytes.getInt(8);
        entry.song.name = Utils.extractTag(le, pl_c_nameIndex);

        byte[] formatChars = new byte[3];
        bytes.position(pl_b_formatIndex);
        bytes.get(formatChars, 0, 3);
        entry.song.format = new String(formatChars, Charset.forName("ASCII"));

        int size = bytes.get(pl_b_addressSizeIndex);
        bytes.position(pl_b_addressIndex + 8);
        bytes.get(entry.song.file, 0, (size + 1) * 8);

        return entry;
    }

    public static void rewriteHeader(byte[] header, byte playlistSize){
        header[8] = playlistSize;
        header[12] = playlistSize;
    }

    public static void rewritePlaylist(File pl, List<Song> songs) throws IOException {
        byte[] buffer = new byte[256];
        RandomAccessFile rw = new RandomAccessFile(pl, "rw");

        // rewriting header with actual playlist size
        rw.seek(0);
        rw.read(buffer);
        rewriteHeader(buffer, (byte) songs.size());
        rw.seek(0);
        rw.write(buffer);

        // taking out id
        rw.seek(512);
        rw.read(buffer);
        PlaylistEntry header = parsePlLine(buffer);
        if (header == null) throw new RuntimeException("Cannot write to a playlist with no previous entries!");

        // songs
        rw.seek(512);
        ByteBuffer bytes = ByteBuffer.wrap(buffer);
        for (int i = 0; i < songs.size(); i++) {
            header.index = i + 1;
            header.song = songs.get(i);
            header.presence = true;
            generatePlaylistEntry(bytes, header);
            rw.write(buffer);
        }
        // tail!
        rw.write(2);
        rw.close();
    }

}
