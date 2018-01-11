package io.github.sp4rx.hackereartholaapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import io.github.sp4rx.hackereartholaapp.BuildConfig;
import io.github.sp4rx.shothanddb.Constraint;
import io.github.sp4rx.shothanddb.DataType;
import io.github.sp4rx.shothanddb.Database;
import io.github.sp4rx.shothanddb.Logger;
import io.github.sp4rx.shothanddb.Schema;
import io.github.sp4rx.shothanddb.ShortHandSchema;
import io.github.sp4rx.shothanddb.Table;

import static io.github.sp4rx.hackereartholaapp.BuildConfig.DATABASE_VERSION;

/**
 * Created by suvajit.<br>
 * Base class for database handling which is extended from {@link SQLiteOpenHelper}.<br>
 *     This class contains a singleton database instance
 */

public class DbHelper extends SQLiteOpenHelper {
    /**
     * Database name
     */
    private static final String DATABASE_NAME = "ola.db";

    //Table and column variables
    /**
     * Table name for the music list
     */
    static final String TABLE_SONGS = "music";
    static final String COLUMN_ID = "id";
    static final String COLUMN_SONG = "song";
    static final String COLUMN_URL = "url";
    static final String COLUMN_ARTISTS = "artists";
    static final String COLUMN_COVER_IMAGE = "cover_image";
    static final String COLUMN_IS_PLAYING = "is_playing";
    static final String COLUMN_IS_FAVOURITE = "is_favourite";
    static final String COLUMN_PLAYED_COUNT = "played_count";


    /**
     * Singleton database instance
     */
    private static DbHelper dbInstance;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Returns singleton instance of DbHelper
     *
     * @param context Context
     * @return instance of DbHelper
     */
    static DbHelper getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DbHelper(context);
        }
        return dbInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Logger.isDebuggable = BuildConfig.DEBUG;

        //Table structure
        ShortHandSchema shortHandSchema = new ShortHandSchema() {
            @Override
            public ArrayList<Schema> getSchema() {
                ArrayList<Schema> schemas = new ArrayList<>();
                schemas.add(new Schema(COLUMN_ID, DataType.INTEGER, new Constraint[]{Constraint.PRIMARY_KEY, Constraint.AUTOINCREMENT}));
                schemas.add(new Schema(COLUMN_SONG, DataType.TEXT));
                schemas.add(new Schema(COLUMN_URL, DataType.TEXT));
                schemas.add(new Schema(COLUMN_ARTISTS, DataType.TEXT));
                schemas.add(new Schema(COLUMN_COVER_IMAGE, DataType.TEXT));
                schemas.add(new Schema(COLUMN_IS_PLAYING, DataType.INTEGER));
                schemas.add(new Schema(COLUMN_IS_FAVOURITE, DataType.INTEGER));
                schemas.add(new Schema(COLUMN_PLAYED_COUNT, DataType.INTEGER));
                return schemas;
            }
        };

        //Crete the table with my library
        Table.create(TABLE_SONGS, shortHandSchema, sqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Delete all previous tables and create again for the sake of simplicity
        Database.deleteAllTables(sqLiteDatabase);
        onCreate(sqLiteDatabase);
    }
}
