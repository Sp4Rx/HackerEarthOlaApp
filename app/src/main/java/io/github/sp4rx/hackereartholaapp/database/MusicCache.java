package io.github.sp4rx.hackereartholaapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import io.github.sp4rx.hackereartholaapp.BuildConfig;
import io.github.sp4rx.hackereartholaapp.pojo.MusicListPojo;

import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_ARTISTS;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_COVER_IMAGE;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_ID;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_IS_FAVOURITE;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_IS_PLAYING;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_PLAYED_COUNT;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_SONG;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.COLUMN_URL;
import static io.github.sp4rx.hackereartholaapp.database.DbHelper.TABLE_SONGS;

/**
 * Created by suvajit.<br>
 * Database class to handle SQLite operation on {@link DbHelper#TABLE_SONGS} table
 */

public class MusicCache {
    /**
     * No. of items which will be shown on each page of the music list
     */
    private static final int ITEMS_PER_PAGE = 5;

    /**
     * Saves the list of songs
     *
     * @param context        Context
     * @param musicListPojos {@link ArrayList}<{@link MusicListPojo}>
     * @return true/false based on success/failure
     */
    public synchronized static boolean saveSongs(Context context, ArrayList<MusicListPojo> musicListPojos) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getWritableDatabase();

        for (int i = 0; i < musicListPojos.size(); i++) {
            MusicListPojo musicListPojo = musicListPojos.get(i);

            //Check if song name already exists to prevent duplicate songs
            //As in a folder file name must be unique
            Cursor cursor = sqLiteDatabase.query(TABLE_SONGS, null, COLUMN_SONG + " = '" + musicListPojo.getSong() + "'", null, null, null, null);
            if (cursor.getCount() <= 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_SONG, musicListPojo.getSong());
                contentValues.put(COLUMN_URL, musicListPojo.getUrl());
                contentValues.put(COLUMN_ARTISTS, musicListPojo.getArtists());
                contentValues.put(COLUMN_COVER_IMAGE, musicListPojo.getCoverImage());
                contentValues.put(COLUMN_IS_PLAYING, musicListPojo.isPlaying());
                contentValues.put(COLUMN_IS_PLAYING, 0);
                contentValues.put(COLUMN_IS_FAVOURITE, 0);
                contentValues.put(COLUMN_PLAYED_COUNT, 0);
                try {
                    sqLiteDatabase.insertOrThrow(TABLE_SONGS, null, contentValues);
                } catch (SQLException e) {
                    if (BuildConfig.DEBUG)
                        e.printStackTrace();
                    cursor.close();
                    return false;
                }
            }
            cursor.close();
        }
        return true;
    }

    /**
     * Returns songs all data
     *
     * @param context Context
     * @return {@link ArrayList}<{@link MusicListPojo}>
     */
    public synchronized static ArrayList<MusicListPojo> getAllSongs(Context context) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getReadableDatabase();

        ArrayList<MusicListPojo> musicListPojos = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.query(TABLE_SONGS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                MusicListPojo musicListPojo = new MusicListPojo();
                musicListPojo.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                musicListPojo.setSong(cursor.getString(cursor.getColumnIndex(COLUMN_SONG)));
                musicListPojo.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
                musicListPojo.setArtists(cursor.getString(cursor.getColumnIndex(COLUMN_ARTISTS)));
                musicListPojo.setCoverImage(cursor.getString(cursor.getColumnIndex(COLUMN_COVER_IMAGE)));
                musicListPojo.setPlaying(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PLAYING)) == 1);
                musicListPojo.setFavourite(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVOURITE)) == 1);
                musicListPojo.setPlayedCount(cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYED_COUNT)));

                musicListPojos.add(musicListPojo);
            } while (cursor.moveToNext());
        } else {
            cursor.close();
            return null;
        }

        cursor.close();
        return musicListPojos;
    }

    /**
     * Clear all songs in the cache
     *
     * @param context Context
     */
    public synchronized static void clearAllSongs(Context context) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getWritableDatabase();
        sqLiteDatabase.delete(TABLE_SONGS, null, null);
    }

    /**
     * Returns songs data based on page number
     *
     * @param context Context
     * @param pageNo  page number
     * @return {@link ArrayList}<{@link MusicListPojo}>
     */
    public synchronized static ArrayList<MusicListPojo> getSongsByPage(Context context, int pageNo) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getReadableDatabase();

        ArrayList<MusicListPojo> musicListPojos = new ArrayList<>();

        int offset = (pageNo - 1) * ITEMS_PER_PAGE;

        String sql = "SELECT * FROM " + TABLE_SONGS + "  LIMIT " + ITEMS_PER_PAGE + " OFFSET " + offset;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                MusicListPojo musicListPojo = new MusicListPojo();
                musicListPojo.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                musicListPojo.setSong(cursor.getString(cursor.getColumnIndex(COLUMN_SONG)));
                musicListPojo.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
                musicListPojo.setArtists(cursor.getString(cursor.getColumnIndex(COLUMN_ARTISTS)));
                musicListPojo.setCoverImage(cursor.getString(cursor.getColumnIndex(COLUMN_COVER_IMAGE)));
                musicListPojo.setPlaying(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PLAYING)) == 1);
                musicListPojo.setFavourite(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVOURITE)) == 1);
                musicListPojo.setPlayedCount(cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYED_COUNT)));

                musicListPojos.add(musicListPojo);
            } while (cursor.moveToNext());
        } else {
            cursor.close();
            return null;
        }

        cursor.close();
        return musicListPojos;
    }

    /**
     * Returns songs data based on song id
     *
     * @param context Context
     * @param id      Song Id
     * @return {@link MusicListPojo} object
     */
    public synchronized static MusicListPojo getSongById(Context context, int id) {
        if (context == null || id <= 0)
            return null;
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(TABLE_SONGS, null, COLUMN_ID + " = " + id, null, null, null, null);

        MusicListPojo musicListPojo = new MusicListPojo();
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            musicListPojo.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            musicListPojo.setSong(cursor.getString(cursor.getColumnIndex(COLUMN_SONG)));
            musicListPojo.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
            musicListPojo.setArtists(cursor.getString(cursor.getColumnIndex(COLUMN_ARTISTS)));
            musicListPojo.setCoverImage(cursor.getString(cursor.getColumnIndex(COLUMN_COVER_IMAGE)));
            musicListPojo.setPlaying(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PLAYING)) == 1);
            musicListPojo.setFavourite(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVOURITE)) == 1);
            musicListPojo.setPlayedCount(cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYED_COUNT)));

        } else {
            cursor.close();
            return null;
        }

        cursor.close();
        return musicListPojo;
    }

    /**
     * Returns count of maximum pages possible
     *
     * @param context Context
     * @return Count of maximum pages possible
     */
    public synchronized static int getMaxPages(Context context) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_SONGS, null, null, null, null, null, null);
        int totalRecords = cursor.getCount();
        cursor.close();
        double tempPages = (double) totalRecords / ITEMS_PER_PAGE;
        return (int) Math.ceil(tempPages);
    }

    /**
     * Update song data
     *
     * @param context       Context
     * @param musicListPojo {@link MusicListPojo} object
     * @return true/false based on update status
     */
    public synchronized static boolean updateSongRecord(Context context, @Nullable MusicListPojo musicListPojo) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int recordCount = 0;
        if (musicListPojo != null) {
            if (musicListPojo.getId() <= 0)
                return false;

            if (musicListPojo.getSong() != null) {
                contentValues.put(COLUMN_SONG, musicListPojo.getSong());
            }
            if (musicListPojo.getUrl() != null) {
                contentValues.put(COLUMN_URL, musicListPojo.getUrl());
            }
            if (musicListPojo.getArtists() != null) {
                contentValues.put(COLUMN_ARTISTS, musicListPojo.getArtists());
            }
            if (musicListPojo.getCoverImage() != null) {
                contentValues.put(COLUMN_COVER_IMAGE, musicListPojo.getCoverImage());
            }
            if (musicListPojo.getPlayedCount() > 0) {
                contentValues.put(COLUMN_PLAYED_COUNT, musicListPojo.getPlayedCount());
            }
            contentValues.put(COLUMN_IS_PLAYING, musicListPojo.isPlaying());
            contentValues.put(COLUMN_IS_FAVOURITE, musicListPojo.isFavourite());
            recordCount = sqLiteDatabase.update(TABLE_SONGS, contentValues, COLUMN_ID + " = " + musicListPojo.getId(), null);
        } else {
            contentValues.put(COLUMN_IS_PLAYING, false);
            recordCount = sqLiteDatabase.update(TABLE_SONGS, contentValues, null, null);
        }

        return recordCount > 0;
    }

    /**
     * Rest all pause buttons to play state
     *
     * @param context Context
     * @return true/false based on update status
     */
    public synchronized static boolean clearPlayStatus(Context context) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        MusicListPojo musicListPojo = new MusicListPojo();
        musicListPojo.setPlaying(false);
        contentValues.put(COLUMN_IS_PLAYING, musicListPojo.isPlaying());

        int recordCount = sqLiteDatabase.update(TABLE_SONGS, contentValues, null, null);
        return recordCount > 0;
    }


    /**
     * Returns songs all favourite songs
     *
     * @param context Context
     * @return {@link ArrayList}<{@link MusicListPojo}>
     */
    public synchronized static ArrayList<MusicListPojo> getAllFavouriteSongs(Context context) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getReadableDatabase();

        ArrayList<MusicListPojo> musicListPojos = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.query(TABLE_SONGS, null, COLUMN_IS_FAVOURITE + " = 1", null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                MusicListPojo musicListPojo = new MusicListPojo();
                musicListPojo.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                musicListPojo.setSong(cursor.getString(cursor.getColumnIndex(COLUMN_SONG)));
                musicListPojo.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
                musicListPojo.setArtists(cursor.getString(cursor.getColumnIndex(COLUMN_ARTISTS)));
                musicListPojo.setCoverImage(cursor.getString(cursor.getColumnIndex(COLUMN_COVER_IMAGE)));
                musicListPojo.setPlaying(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PLAYING)) == 1);
                musicListPojo.setFavourite(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVOURITE)) == 1);
                musicListPojo.setPlayedCount(cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYED_COUNT)));

                musicListPojos.add(musicListPojo);
            } while (cursor.moveToNext());
        } else {
            cursor.close();
            return null;
        }

        cursor.close();
        return musicListPojos;
    }


    /**
     * Returns songs in most played order
     *
     * @param context Context
     * @return {@link ArrayList}<{@link MusicListPojo}>
     */
    public synchronized static ArrayList<MusicListPojo> getMostPlayedSongs(Context context) {
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance(context).getReadableDatabase();

        ArrayList<MusicListPojo> musicListPojos = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.query(TABLE_SONGS, null, COLUMN_PLAYED_COUNT + " > 0", null, null, null, COLUMN_PLAYED_COUNT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                MusicListPojo musicListPojo = new MusicListPojo();
                musicListPojo.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                musicListPojo.setSong(cursor.getString(cursor.getColumnIndex(COLUMN_SONG)));
                musicListPojo.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
                musicListPojo.setArtists(cursor.getString(cursor.getColumnIndex(COLUMN_ARTISTS)));
                musicListPojo.setCoverImage(cursor.getString(cursor.getColumnIndex(COLUMN_COVER_IMAGE)));
                musicListPojo.setPlaying(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PLAYING)) == 1);
                musicListPojo.setFavourite(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVOURITE)) == 1);
                musicListPojo.setPlayedCount(cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYED_COUNT)));

                musicListPojos.add(musicListPojo);
            } while (cursor.moveToNext());
        } else {
            cursor.close();
            return null;
        }

        cursor.close();
        return musicListPojos;
    }
}
