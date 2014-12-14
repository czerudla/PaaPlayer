package cz.hofmanladislav.paaplayer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SongsManager {
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    // Constructor
    public SongsManager(){
    }

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(String path){
        File home = new File(path);

        if (home.listFiles(new FileExtensionFilter()).length > 0) {
            File[] files = home.listFiles();
            Arrays.sort(files);//serazeni dle nazvu
            for (File file : home.listFiles(new FileExtensionFilter())) {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", file.getName().substring(0, (file.getName().length()/* - 4*/)));
                song.put("songPath", file.getPath());
                song.put("isColored", Integer.toString(0));
                // Adding each song to SongList
                songsList.add(song);
            }
        }

        // sort arraylist by filename
        Collections.sort(songsList, new Comparator<HashMap< String,String >>() {
            @Override
            public int compare(HashMap<String, String> first,
                               HashMap<String, String> second) {
                // Do your comparison logic here and retrn accordingly.
                return first.get("songTitle").compareTo(second.get("songTitle"));
            }
        });

        // return songs list array
        return songsList;
    }

    /**
     * Class to filter files which are having .mp3 extension
     * */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.endsWith(".jpg") || name.endsWith(".bak") || name.endsWith(".txt")) return false;
            return (name.endsWith(".mp3") || name.endsWith(".MP3") || name.endsWith(""));
        }
    }
}
