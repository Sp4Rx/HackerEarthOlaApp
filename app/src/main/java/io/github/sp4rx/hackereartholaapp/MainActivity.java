package io.github.sp4rx.hackereartholaapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;
import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.listener.FetchListener;
import com.tonyodev.fetch.request.Request;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.github.sp4rx.hackereartholaapp.adapter.ViewPagerAdapter;
import io.github.sp4rx.hackereartholaapp.database.MusicCache;
import io.github.sp4rx.hackereartholaapp.fragment.AllSongsFragment;
import io.github.sp4rx.hackereartholaapp.fragment.FavouriteSongsFragment;
import io.github.sp4rx.hackereartholaapp.fragment.MostPlayedSongsFragment;
import io.github.sp4rx.hackereartholaapp.networking.GetRedirectUrl;
import io.github.sp4rx.hackereartholaapp.pojo.MusicListPojo;
import io.github.sp4rx.hackereartholaapp.util.Toaster;

import static io.github.sp4rx.hackereartholaapp.global.Constants.ALL_SONGS;
import static io.github.sp4rx.hackereartholaapp.global.Constants.FAVOURITES;
import static io.github.sp4rx.hackereartholaapp.global.Constants.FILE_EXTENSION;
import static io.github.sp4rx.hackereartholaapp.global.Constants.MOST_PLAYED;
import static io.github.sp4rx.hackereartholaapp.global.Constants.STORAGE_DIR_PATH;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private Context context;

    private SimpleExoPlayer player;
    private ExtractorsFactory extractorsFactory;
    private DataSource.Factory dataSourceFactory;
    private boolean isDurationSet;
    private SeekBar seekBar;
    private Handler seekBarHandler;
    private ViewPager viewPager;
    private int maxDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init --Start
        context = this;
        viewPager = findViewById(R.id.vpActivityMain);
        TabLayout tabLayout = findViewById(R.id.tlActivityMainTabs);
        seekBar = findViewById(R.id.sbActivityMainSeekBar);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        //Exo player -- Start
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();

        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        //To allow redirects , as short url is used
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(context, BuildConfig.APPLICATION_ID),
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true /* allowCrossProtocolRedirects */
        );
        dataSourceFactory = new DefaultDataSourceFactory(context, defaultBandwidthMeter, httpDataSourceFactory);
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        player.addListener(defaultEventListener);
        //Exo player -- End

        //SeekBar
        HandlerThread handlerThread = new HandlerThread("SeekBarHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        seekBarHandler = new Handler(looper);

        //Init --End

        //Setup ViewPager -- Start
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        final AllSongsFragment allSongsFragment = new AllSongsFragment();
        final FavouriteSongsFragment favouriteSongsFragment = new FavouriteSongsFragment();
        final MostPlayedSongsFragment mostPlayedSongsFragment = new MostPlayedSongsFragment();
        adapter.addFragment(allSongsFragment, ALL_SONGS);
        adapter.addFragment(favouriteSongsFragment, FAVOURITES);
        adapter.addFragment(mostPlayedSongsFragment, MOST_PLAYED);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    switch (position) {
                        case 0:
                            actionBar.setTitle(ALL_SONGS);
                            allSongsFragment.updateMusicList();
                            break;
                        case 1:
                            actionBar.setTitle(FAVOURITES);
                            favouriteSongsFragment.updateMusicList();
                            break;
                        case 2:
                            actionBar.setTitle(MOST_PLAYED);
                            mostPlayedSongsFragment.updateMusicList();
                            break;
                    }
                }
            }
        });
        //Setup ViewPager -- End

        //Setup Tabs
        tabLayout.setupWithViewPager(viewPager);
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null)
            tab.setIcon(R.drawable.ic_all_songs);
        tab = tabLayout.getTabAt(1);
        if (tab != null)
            tab.setIcon(R.drawable.ic_favourite_tab);
        tab = tabLayout.getTabAt(2);
        if (tab != null)
            tab.setIcon(R.drawable.ic_play);
    }

    private Player.DefaultEventListener defaultEventListener = new Player.DefaultEventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_READY && !isDurationSet) {
                long realDurationMillis = player.getDuration();
                maxDuration = (int) (realDurationMillis / 1000);
                Log.d(TAG, "Max duration: " + maxDuration);
                seekBar.setMax(maxDuration);
                isDurationSet = true;
                seekBarHandler.post(seekBarProgressRunnable);
            }
            super.onPlayerStateChanged(playWhenReady, playbackState);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            player.stop();
            player.seekTo(0L);
            seekBar.setMax(0);
            resetAllPauseButton();
            Toaster.makeText(context, R.string.playback_error, Toaster.LENGTH_SHORT).show();
            super.onPlayerError(error);
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (i >= maxDuration && i > 0) {
                resetAllPauseButton();
                player.stop();
                player.seekTo(0L);
                seekBar.setMax(0);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo((long) (seekBar.getProgress() * 1000));
        }
    };

    /**
     * Start playing song
     *
     * @param musicListPojo {@link MusicListPojo}
     */
    public void playSong(MusicListPojo musicListPojo) {
        isDurationSet = false;
        player.stop();
        player.seekTo(0L);
        seekBar.setMax(0);

        if (checkAndRequestPermissions()) {
            final File checkSongFile = new File(STORAGE_DIR_PATH + musicListPojo.getSong() + FILE_EXTENSION);
            if (!checkSongFile.exists()) {
                streamSong(musicListPojo);
            } else {
                playFromFile(musicListPojo);
            }
        } else {
            //Stream song online if no read/write permission
            streamSong(musicListPojo);
        }
    }


    /**
     * Pause current song
     */
    public void pauseSong() {
        player.setPlayWhenReady(false);
        player.stop();
        player.seekTo(0L);
        seekBar.setMax(0);
        seekBarHandler.removeCallbacks(seekBarProgressRunnable);
    }

    /**
     * Play song online
     *
     * @param musicListPojo {@link MusicListPojo}
     */
    private void streamSong(MusicListPojo musicListPojo) {
        if (isNetworkConnected()) {
            Toaster.makeText(context, "Buffering " + musicListPojo.getSong() + ". Please wait...", Toaster.LENGTH_LONG).show();

            isDurationSet = false;
            player.stop();
            player.seekTo(0L);
            seekBar.setMax(0);
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(musicListPojo.getUrl()), dataSourceFactory, extractorsFactory, null, null);
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
        } else {
            resetAllPauseButton();
            Toaster.makeText(context, R.string.no_internet, Toaster.LENGTH_SHORT).show();
        }
    }

    /**
     * Start song download
     *
     * @param musicListPojo {@link MusicListPojo}
     * @param position
     */
    public void downloadSong(final MusicListPojo musicListPojo, final int position) {
        if (checkAndRequestPermissions()) {
            final File checkSongFile = new File(STORAGE_DIR_PATH + musicListPojo.getSong() + FILE_EXTENSION);
            if (!checkSongFile.exists()) {
                if (isNetworkConnected()) {
                    String songUrl = null;
                    try {
                        songUrl = new GetRedirectUrl().execute(musicListPojo.getUrl()).get();
                        Log.d(TAG, "Url after redirect: " + songUrl);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (songUrl == null)
                        songUrl = musicListPojo.getUrl();

                    //Progress dialog
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Downloading " + musicListPojo.getSong());
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(false);

                    //Download file using fetch
                    final Fetch fetch = Fetch.newInstance(this);
                    Request request = new Request(songUrl, STORAGE_DIR_PATH, musicListPojo.getSong() + FILE_EXTENSION);
                    String filePath = request.getFilePath();
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                    Log.d(TAG, "Song store Path: " + filePath);

                    final long downloadId = fetch.enqueue(request);
                    if (downloadId != Fetch.ENQUEUE_ERROR_ID) {
                        Log.d(TAG, "Fetch: Download was successfully queued for download.");
                        progressDialog.show();
                    }

                    fetch.addFetchListener(new FetchListener() {

                        @Override
                        public void onUpdate(long id, int status, int progress, long downloadedBytes, long fileSize, int error) {

                            if (downloadId == id && status == Fetch.STATUS_DOWNLOADING) {
                                progressDialog.setProgress(progress);
                            } else if (error != Fetch.NO_ERROR) {
                                //An error occurred
                                Log.d(TAG, "Fetch: An error occurred .");
                                progressDialog.dismiss();

                                if (error == Fetch.ERROR_HTTP_NOT_FOUND) {
                                    //handle error
                                    Log.d(TAG, "Fetch: Http not found.");
                                }

                            } else if (status == Fetch.STATUS_DONE) {
                                hideDownloadButton(position);
                                Toaster.makeText(context, musicListPojo.getSong() + " downloaded successfully.", Toaster.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                } else {
                    Toaster.makeText(context, R.string.no_internet, Toaster.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "downloadSong: " + musicListPojo.getSong() + " Already exists.");
            }
        }
    }

    /**
     * Play song from local
     *
     * @param musicListPojo {@link MusicListPojo}
     */
    private void playFromFile(MusicListPojo musicListPojo) {
        if (checkAndRequestPermissions()) {
            String filePath = STORAGE_DIR_PATH + musicListPojo.getSong() + FILE_EXTENSION;
            Uri uri = Uri.fromFile(new File(filePath));
            isDurationSet = false;
            player.stop();
            player.seekTo(0L);
            seekBar.setMax(0);

            DataSpec dataSpec = new DataSpec(uri);
            final FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }

            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return fileDataSource;
                }
            };
            MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                    factory, new DefaultExtractorsFactory(), null, null);

            player.prepare(audioSource);
            player.setPlayWhenReady(true);
        }
    }

    /**
     * Checks if internet is available
     *
     * @return true/false based on Network connectivity
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    Runnable seekBarProgressRunnable = new Runnable() {

        @Override
        public void run() {
            if (player != null) {
                int mCurrentPosition = (int) (player.getCurrentPosition() / 1000);
                if (seekBar != null) {
                    seekBar.setProgress(mCurrentPosition);
                }
            }
            seekBarHandler.postDelayed(this, 1000);
        }
    };

    /**
     * Switch all pause buttons from any fragment to play state
     */
    private void resetAllPauseButton() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.vpActivityMain + ":" + viewPager.getCurrentItem());
        if (currentFragment instanceof AllSongsFragment) {
            ((AllSongsFragment) currentFragment).musicListAdapter.resetAllPauseButtons();
        } else if (currentFragment instanceof FavouriteSongsFragment) {
            ((FavouriteSongsFragment) currentFragment).musicListAdapter.resetAllPauseButtons();
        } else if (currentFragment instanceof MostPlayedSongsFragment) {
            ((MostPlayedSongsFragment) currentFragment).musicListAdapter.resetAllPauseButtons();
        }
    }

    /**
     * Hides the download button of the respective viewHolder
     *
     * @param position Position of the viewHolder
     */
    private void hideDownloadButton(int position) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.vpActivityMain + ":" + viewPager.getCurrentItem());
        if (currentFragment instanceof AllSongsFragment) {
            ((AllSongsFragment) currentFragment).musicListAdapter.hideDownloadButton(position);
        } else if (currentFragment instanceof FavouriteSongsFragment) {
            ((FavouriteSongsFragment) currentFragment).musicListAdapter.hideDownloadButton(position);
        } else if (currentFragment instanceof MostPlayedSongsFragment) {
            ((MostPlayedSongsFragment) currentFragment).musicListAdapter.hideDownloadButton(position);
        }
    }

    /**
     * Check runtime permissions
     *
     * @return true/false
     */
    private boolean checkAndRequestPermissions() {
        int readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();
        if (readExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writeExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Write permission granted");
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK(getString(R.string.runtime_permission_ok_dialog_message),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    showSnackBar();
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            showSnackBar();
                        }
                    }
                }
            }
        }

    }

    /**
     * Show dialog box if runtime permission cancelled
     *
     * @param message    Message
     * @param okListener Ok button press listener
     */
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    /**
     * Show snackBar message if runtime permission cancelled/marked don't ask along with the ok dialog cancelled.
     */
    private void showSnackBar() {
        Snackbar.make(findViewById(R.id.mainActivity), R.string.runtime_permission_snackbar_message, BaseTransientBottomBar.LENGTH_LONG)
                .setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        player.setPlayWhenReady(false);
        MusicCache.clearPlayStatus(context);
        seekBarHandler.removeCallbacks(seekBarProgressRunnable);
    }


}
