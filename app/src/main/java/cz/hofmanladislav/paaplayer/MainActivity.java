package cz.hofmanladislav.paaplayer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements NowPlayingFragment.INowPlaying,
        ControlFragment.IControl, SongProgressBarFragment.ISongProgressBar,
        LoadPlaylistDialog.ILoadPlaylistDialogListener, SavePlaylistDialog.ISavePlaylistDialogListener,
        DeletePlaylistsDialog.IDeletePlaylistsDialogListener {

    public QueueFragment qf;
    private ControlFragment cf;
    private FileSystemFragment fs;
    private NowPlayingFragment np;
    private SongProgressBarFragment sp;

    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    public ArrayList<HashMap<String, String>> filesList = new ArrayList<HashMap<String, String>>();
    public int actualSongIndex = 0;
    //private int seekTime = 5000; // default time for btnBack a btnForward
    private boolean isPaused = false;
    private final int MAX_VOLUME = 100;// Mute
    private int actualVolume = 100;// Mute

    public String actualMediaPath = new String(/*"/storage/emulated/0/Music/The Black Keys/"*/"/sdcard/");

    private MediaPlayer mp;
    private Utils utils;
    private FileSystemUtils fsUtils;
    private SongsManager plm;
    public PlayerDB playerDB = new PlayerDB(this);

    private final Context context = this;
    private SavePlaylistDialog savePlaylistDialog;
    private LoadPlaylistDialog loadPlaylistDialog;
    private DeletePlaylistsDialog deletePlaylistsDialog;
    public EditText playlistName;
    private ArrayList<PlaylistModel> playlistsFromDB;

    private Handler progressHandler = new Handler();
    private Handler muteHandler = new Handler();
    private ActionBar ab;

    private static float scale = 0;
    public static int dps2pixels(int dps, Context context) {
        if (scale == 0) {scale = context.getResources().getDisplayMetrics().density;}
        return (int) (dps * scale + 0.5f);
    }
    private int rowSizeInPx;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        plm = new SongsManager();
        filesList = plm.getPlayList(actualMediaPath);// get all songs from sdcard

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            //fs = (FileSystemFragment)getFragmentManager().getFragment(savedInstanceState, "fs");
        } else {
            fs = (FileSystemFragment)getFragmentManager().findFragmentById(R.id.fragment_filesystem);
        }

        qf = (QueueFragment)getFragmentManager().findFragmentById(R.id.fragment_queue);
        cf = (ControlFragment)getFragmentManager().findFragmentById(R.id.fragment_control);

        np = (NowPlayingFragment)getFragmentManager().findFragmentById(R.id.fragment_nowplaying);
        sp = (SongProgressBarFragment)getFragmentManager().findFragmentById(R.id.fragment_songprogressbar);

        playlistName = (EditText)findViewById(R.id.playlistName);

        rowSizeInPx = dps2pixels(60, getApplicationContext());
        //Log.d("rowSizeInPx", Integer.toString(rowSizeInPx));

        mp = new MediaPlayer();
        utils = new Utils();
        fsUtils = new FileSystemUtils();

        savePlaylistDialog = new SavePlaylistDialog();
        loadPlaylistDialog = new LoadPlaylistDialog();
        deletePlaylistsDialog = new DeletePlaylistsDialog();

        enableProgressBars(false);

        /*mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.reset();
                mp.release();
                playNextSong();
            }
        });*/

        setupDragDropStuff();
    }

    private void setupDragDropStuff() {

        // Drag drop would be triggered once you long tap on a list view's item
        fs.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
                ClipData data = ClipData.newPlainText("Position","-2");//malý hack pro spravne srovnani prave oprehravaneho songu
                if (v.getId() == R.id.btnDragDrop) v = (View) v.getParent(); // pokud je to ten button, tak budu hybat s celym radkem
                if (!fsUtils.isDirectory(filesList.get(position).get("songPath")))
                    v.startDrag(data, new MyDragShadowBuilder(v), filesList.get(position), 0);
                return true;
            }
        });

        qf.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3) {
                ClipData data = ClipData.newPlainText("Position",""+position);
                Object song = songsList.get(position);
                songsList.remove(position);
                if (v.getId() == R.id.btnDragDrop) v = (View)v.getParent(); // pokud je to ten button, tak budu hybat s celym radkem
                v.startDrag(data, new MyDragShadowBuilder(v), song, 0);
                return true;
            }
        });

        // Set the Drag Listener to the drop area.
        qf.getListView().setOnDragListener(new View.OnDragListener() {

            public boolean onDrag(View v, DragEvent event) {
                //System.out.println(v.getClass().getName());
                //TODO onDrag je smyčka, zde by se dalo expandovat listView a z eventu záskávat pozici
                //TODO dodělat scrollování pokud jsem na spodní hraně
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundColor(Color.GRAY);
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setBackgroundColor(Color.TRANSPARENT);
                        break;

                    case DragEvent.ACTION_DRAG_STARTED:
                        return processDragStarted(event);

                    case DragEvent.ACTION_DROP:
                        v.setBackgroundColor(Color.TRANSPARENT);
                        return processDrop(event);
                }
                return false;
            }
        });

    }

    /**
     * Process the drop event
     *
     * @param event
     * @return
     */
    private boolean processDrop(DragEvent event) {
        HashMap<String, String> song = (HashMap<String, String>)event.getLocalState();
        if (song != null) {
            int y = ((int)event.getY());
            if (songsList != null & songsList.size() > 0) {
                View c = qf.getListView().getChildAt(0);
                int scrolly = -c.getTop() + qf.getListView().getFirstVisiblePosition() * c.getHeight();
                Log.d("scrolly: "+ scrolly, " getY(): "+event.getY());
                y = y+scrolly;
            }
            int position = (y/* - rowSizeInPx*/) / rowSizeInPx;//vypocteni pozice kam vlozit song

            //pokud je vypocitana pozice vetsi, nez pocet skladeb, tak song vlozime na konec, v poacnem pripade na danou pozici
            if (songsList.size() <= position) songsList.add(song);
            else songsList.add(position, song);

            //malý MINDFUCK - první podmínka platí, pokud právě přesouvaný song zrovna hraje
            String fromPosition = event.getClipData().getItemAt(0).getText().toString();
            if (actualSongIndex == Integer.parseInt(fromPosition)) {
                if (position < songsList.size())
                    actualSongIndex = position;
                else
                    actualSongIndex = songsList.size()-1;
            } else if (position <= actualSongIndex) actualSongIndex++;//prepocitani actualsongIndex dle mista, kam se vlozila
            //else actualSongIndex--;//TODO kontrolovat i predchozi pozici prehravane skladby pokud presouvam na misto aktualne hrane

            highlightSongInQueue(actualSongIndex);
            qf.notifyAdapter(); // notifikace jiz obsazena v highlightSongInQueue(actualSongIndex); - evidentne ne (nefungovalo oznaceni prvni skladby)
            return true;
        }
        return false;
    }

    /**
     * Check if this is the drag operation you want. There might be other
     * clients that would be generating the drag event. Here, we check the mime
     * type of the data
     *
     * @param event
     * @return
     */
    private boolean processDragStarted(DragEvent event) {
        //TODO z event.getX() ziskat souradnice a ty pak pouzit zda jsem na spravnem miste pro drag (v pripade onClick namisto onLongClick)
        ClipDescription clipDesc = event.getClipDescription();
        if (clipDesc != null) {
            return clipDesc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        }
        return false;
    }

    public void enableProgressBars(boolean enabled) {
        sp.enableProgressBar(enabled);
    }

    public void updateFileSystem() {
        filesList.clear();
        filesList = plm.getPlayList(actualMediaPath);// get all songs from sdcard
        fs.notifyAdapter();
    }

    @Override
    public void onBtnOpenFolderClick(String path, View myView) {
        //NowPlayingFragment
        if (mp.isPlaying() || isPaused) {
            Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
            actualMediaPath = fsUtils.getParentFolder(songsList.get(actualSongIndex).get("songPath"));
            updateFileSystem();
        }
    }

    @Override
    public void onBtnAddToQueueClick(HashMap<String, String> song) {
        //NowPlayingFragment
        if (song != null) {
            songsList.add(song);
            qf.notifyAdapter();
        }
    }

    public void btnAddRemoveClickHandler(View v) {
        /**
         * kladná ID (včetně nuly) mají pouze songy ve frontě, záporná pak soubory filesystemu
         * kvůli nule bylo nutné ve FileSystemAdapter přičíst 1 a následně vynásobit -1
         * **/
        LinearLayout vwParentRow = (LinearLayout)v.getParent();

        /*Log.println(10,"tag","clicked "+Integer.toString(vwParentRow.getChildCount())+ " ID " + vwParentRow.getId());
        //ID = pozice
        Button btnChild = (Button)vwParentRow.getChildAt(0);
        btnChild.setText("CHILD");
        LinearLayout texty = (LinearLayout)vwParentRow.getChildAt(1);
        TextView tw = (TextView)texty.getChildAt(0);
        tw.setText("texty Child");*/

        if (vwParentRow.getId() >= 0){
            try {
                // pokud je odebiran song na nizsi pozici nez aktualne hrajici/vybrany
                if (vwParentRow.getId() <= actualSongIndex) {
                    actualSongIndex--;
                }

                songsList.remove(vwParentRow.getId());//ID ukladane v QueueAdapter
                qf.notifyAdapter();

                highlightSongInQueue(actualSongIndex);
            } catch (IndexOutOfBoundsException e){
                Log.println(10,"IndexOutOfBoundsException", "removeItemFromQueue" + vwParentRow.getId());
            }
        } else {
            if (!fsUtils.isDirectory(filesList.get(-1*(vwParentRow.getId())-1).get("songPath"))) {
                songsList.add(filesList.get(-1*(vwParentRow.getId())-1));
                qf.notifyAdapter();
            }
        }
    }

    public void rowSongClickHandler(View v) {
        if (v.getId() >= 0) {
            actualSongIndex = v.getId();
            playSong(actualSongIndex);
        } else {
            if (fsUtils.isDirectory(filesList.get(-1*(v.getId()+1)).get("songPath"))) {
                //podminka pro to, aby se oteviraly pouze slozky a ne jine soubory
                actualMediaPath = filesList.get(-1*(v.getId()+1)).get("songPath");
                updateFileSystem();
            }
        }
    }

    @Override
    public void onBtnPrevClick() {
        playPreviousSong();
    }

    /*@Override
    public void onBtnBackClick() {
        if (mp.isPlaying()) {
            if (mp.getCurrentPosition() > seekTime){
                seekSong(-seekTime);
            } else {
                if (mp.getCurrentPosition() > 1000) {
                    mp.seekTo(0);
                } else {
                    playPreviousSong();
                }
            }
        }
    }*/

    @Override
    public void onBtnPlayClick() {
        if (mp.isPlaying()){
            mp.pause(); isPaused = true;
            enableProgressBars(false);
            cf.setBtnPlayIcon(R.drawable.ic_media_play);
            progressHandler.removeCallbacks(mUpdateTimeTask);
        } else if (isPaused){
            mp.start(); isPaused = false;
            mp.setVolume(1,1); actualVolume = 100;
            enableProgressBars(true);
            cf.setBtnPlayIcon(R.drawable.ic_media_pause);
            updateProgressBar();
        } else {
            playSong(actualSongIndex);
        }
    }

    @Override
    public void onBtnMuteClick() {
        updateMuteVolume();
    }

    public void updateMuteVolume() {
        muteHandler.postDelayed(mUpdateMuteVolumeTask, 80);
    }

    private Runnable mUpdateMuteVolumeTask = new Runnable() {
        public void run() {
            if (mp.isPlaying() && actualVolume > 0) {
                actualVolume -= 3;
                if (actualVolume > 0) {
                    float volume = (float) (1 - (Math.log(MAX_VOLUME - actualVolume) / Math.log(MAX_VOLUME)));
                    mp.setVolume(volume, volume);
                    // Running this thread after 80 milliseconds
                    muteHandler.postDelayed(this, 60);
                } else {
                    actualVolume = 0;
                    float volume = (float) (1 - (Math.log(MAX_VOLUME - actualVolume) / Math.log(MAX_VOLUME)));
                    mp.setVolume(volume, volume);
                    muteHandler.removeCallbacks(mUpdateMuteVolumeTask);
                    progressHandler.removeCallbacks(mUpdateTimeTask);
                    mp.pause(); isPaused = true;
                    cf.setBtnPlayIcon(R.drawable.ic_media_play);
                    mp.setVolume(1,1);
                    actualVolume = 100;
                }
            }
        }
    };

    @Override
    public void onBtnStopClick() {
        btnStopClickHandler();
    }

    public void btnStopClickHandler() {
        if (mp.isPlaying() || isPaused) {
            progressHandler.removeCallbacks(mUpdateTimeTask);
            mp.stop();
            sp.setProgressBarPercentage(0);
            sp.setCurrentTime("0:00");
            isPaused = false;
            //cf.setBtnPlayText("Play");
            cf.setBtnPlayIcon(R.drawable.ic_media_play);
            enableProgressBars(false);
        }
    }

    /*@Override
    public void onBtnForwardClick() {
        if (mp.isPlaying()) {
            if (mp.getDuration() > mp.getCurrentPosition()+seekTime) {
                seekSong(seekTime);
            } else {
                mp.seekTo(mp.getDuration());
            }
        }
    }*/

    @Override
    public void onBtnNextClick() {
        playNextSong();
    }

    private void playPreviousSong() {
        if (actualSongIndex > 0)
            actualSongIndex--;
        else
            actualSongIndex = 0;
        playSong(actualSongIndex);
    }

    private void playNextSong() {
        actualSongIndex++;
        playSong(actualSongIndex);
    }

    private void playSong(int position) {
        try {
            // kontrola indexOutOfBounds, v tom pripade se hraje z pozice 0
            if (songsList.size() <= position) {
                position = 0;
                actualSongIndex = 0;
            }

            if (songsList.size() > 0 && position >= 0) {
                // Play song
                mp.reset();
                mp.setDataSource(songsList.get(position).get("songPath"));
                mp.prepare();
                mp.start();

                muteHandler.removeCallbacks(mUpdateMuteVolumeTask);//pokud byla zmenena skladba v prubehu mute
                mp.setVolume(1, 1);//nastaveni defaultni hlasitosti systému
                actualVolume = 100;

                isPaused = false;
                // Displaying Song title
                np.setSong(songsList.get(position));

                // zmena ikony z play na pause
                cf.setBtnPlayIcon(R.drawable.ic_media_pause);
                enableProgressBars(true);

                // zvírazňování aktuálního songu ve frontě
                highlightSongInQueue(actualSongIndex);


                sp.setTotalTime(utils.milliSecondsToTimer((long)mp.getDuration()));

                // Updating progress bar
                updateProgressBar();
            }
        } catch (IllegalArgumentException | NullPointerException | IllegalStateException | IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void highlightSongInQueue(int position) {
        for (int i = 0; i < songsList.size(); i++) songsList.get(i).put("isColored", Integer.toString(position));
        qf.notifyAdapter();
    }

    private void seekSong(int duration) {
        mp.seekTo(mp.getCurrentPosition()+duration);
    }

    public void updateProgressBar() {
        progressHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            //if mp isplayng pac muze hazet illegalstateexception
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            sp.setCurrentTime("" + utils.milliSecondsToTimer(currentDuration));

            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            sp.setProgressBarPercentage(progress);

            // Running this thread after 100 milliseconds
            progressHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChangedPB(int progress) {
        if (progress == 100) {
            playNextSong();
        }
    }

    @Override
    public void onStartTrackingTouchPB() {
        // remove message Handler from updating progress bar
        progressHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouchPB(int progress) {
        if (mp.isPlaying()) {
            progressHandler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = mp.getDuration();
            int currentPosition = utils.progressToTimer(progress, totalDuration);

            // forward or backward to certain seconds
            mp.seekTo(currentPosition);

            // update timer progress again
            updateProgressBar();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menuToParentFolder) {
            actualMediaPath = fsUtils.getParentFolder(actualMediaPath);
            updateFileSystem();
            qf.notifyAdapter();
            return true;
        } else if (id == R.id.menuSavePlaylist) {
            if (songsList.size() == 0)
                Toast.makeText(getApplicationContext(), "Nelze uložit prázdný playlist", Toast.LENGTH_LONG).show();
            else
                savePlaylistDialog.show(getFragmentManager(), "Save");
            return true;
        } else if (id == R.id.menuLoadPlaylist) {
            //předání seznamu playlistů danému dialogu k zobrazení
            playlistsFromDB = playerDB.getPlaylists();
            if (playlistsFromDB == null)
                Toast.makeText(getApplicationContext(), "V databázi není uložen žádný playlist", Toast.LENGTH_LONG).show();
            else {
                loadPlaylistDialog.setPlaylists(playlistsFromDB);
                loadPlaylistDialog.show(getFragmentManager(), "Load");
            }
            return true;
        } else if (id == R.id.menuDeletePlaylists) {
            playlistsFromDB = playerDB.getPlaylists();
            if (playlistsFromDB == null)
                Toast.makeText(getApplicationContext(), "V databázi není uložen žádný playlist", Toast.LENGTH_LONG).show();
            else {
                deletePlaylistsDialog.setPlaylists(playlistsFromDB);
                deletePlaylistsDialog.show(getFragmentManager(), "Delete");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogLoadPlaylistClick(int position) {
        songsList.clear();
        for (HashMap<String, String> song : playerDB.getPlaylistByID(playlistsFromDB.get(position).getId())) {
            songsList.add(song);
            qf.notifyAdapter();
        }
    }

    @Override
    public void onDialogSavePlaylistClick(String playlistName) {
        playerDB.savePlaylistToDB(songsList, playlistName);
    }

    //Odstraneni vybranych playlistu dle jejich pozice v DB, osetreni spravnosti ID v databazi
    @Override
    public void onDialogDeletePlaylistsClick(ArrayList selectedItems) {
        for (int i = 0; i < selectedItems.size(); i++) {
            playerDB.deletePlaylistFromDatabase(playlistsFromDB.get(Integer.valueOf(selectedItems.get(i).toString())).getId());
        }
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        getFragmentManager().putFragment(outState, "fs", fs);
    }*/

    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }*/
}
