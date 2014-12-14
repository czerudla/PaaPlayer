package cz.hofmanladislav.paaplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayerDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "paaplayer.db";
    private static final int VERSION = 1;

    public PlayerDB(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Playlist.CREATE_TABLE_STATEMENT);
        sqLiteDatabase.execSQL(Song.CREATE_TABLE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        /*sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Playlist.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Song.TABLE_NAME);
        onCreate(sqLiteDatabase);*/
    }

    public static abstract class Playlist implements BaseColumns {
        public static final String TABLE_NAME = "playlist";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";

        public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME + " TEXT)";
    }

    public static abstract class Song implements BaseColumns {
        public static final String TABLE_NAME = "song";
        public static final String KEY_ID = "id";
        public static final String KEY_PLAYLIST_ID = "playlist_id";
        public static final String KEY_PATH = "path";

        public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_PLAYLIST_ID + " INTEGER, " + KEY_PATH + " TEXT)";
    }

    public ArrayList<PlaylistModel> getPlaylists() {
        String selectQuery = "SELECT " + Playlist.KEY_ID + "," + Playlist.KEY_NAME +" FROM " + Playlist.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            ArrayList<PlaylistModel> playlistArray= new ArrayList<PlaylistModel>();
            do {
                PlaylistModel playlist = new PlaylistModel();
                playlist.setId(cursor.getInt(0));
                playlist.setName(cursor.getString(1));
                playlistArray.add(playlist);
            } while (cursor.moveToNext());
            cursor.close();
            return playlistArray;
        }

        cursor.close();
        return null;
    }

    public ArrayList<HashMap<String, String>> getPlaylistByID(int id) {
        String selectQuery = "Select " + Song.KEY_PATH + " FROM " + Song.TABLE_NAME
                + " WHERE " + Song.KEY_PLAYLIST_ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            ArrayList<HashMap<String, String>> playlist = new ArrayList<HashMap<String, String>>();
            do {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songPath", cursor.getString(0));
                song.put("songTitle", new File(cursor.getString(0)).getName());
                song.put("isColored", Integer.toString(0));
                playlist.add(song);
            } while (cursor.moveToNext());
            cursor.close();
            return playlist;
        }

        cursor.close();
        return null;
    }

    public void savePlaylistToDB(ArrayList<HashMap<String, String>> playlist, String name) {
        ContentValues val = new ContentValues();
        val.put(Playlist.KEY_NAME, name);
        SQLiteDatabase db = this.getWritableDatabase();
        long playlistID = db.insert(Playlist.TABLE_NAME, null, val);

        for (HashMap<String, String> song : playlist) {
            ContentValues val2 = new ContentValues();
            val2.put(Song.KEY_PLAYLIST_ID, playlistID);
            val2.put(Song.KEY_PATH, song.get("songPath"));
            long newRowId2;
            newRowId2 = db.insert(Song.TABLE_NAME, null, val2);
            Log.d("ROW_ID", String.valueOf(newRowId2));
        }
    }

    public void deletePlaylistFromDatabase(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Playlist.TABLE_NAME, Playlist.KEY_ID + " = " + id, null);
        db.delete(Song.TABLE_NAME, Song.KEY_PLAYLIST_ID + " = " + id, null);
    }
}
