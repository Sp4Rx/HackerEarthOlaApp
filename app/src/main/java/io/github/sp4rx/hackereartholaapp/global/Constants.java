package io.github.sp4rx.hackereartholaapp.global;

import android.os.Environment;

/**
 * Created by suvajit.<br>
 */

public class Constants {
    //Apis
    public static final String OLA_PLAY_STUDIOS_API = "http://starlord.hackerearth.com/studio";

    //Fragment Names
    public static final String ALL_SONGS = "All Songs";
    public static final String FAVOURITES = "Favourites";
    public static final String MOST_PLAYED = "Most Played";

    //File Store Path
    public static final String STORAGE_DIR_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/OLA Music/";
    //Default File Extension
    public static final String FILE_EXTENSION = ".mp3";

}
