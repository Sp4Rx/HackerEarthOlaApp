package io.github.sp4rx.hackereartholaapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.sp4rx.hackereartholaapp.MainActivity;
import io.github.sp4rx.hackereartholaapp.R;
import io.github.sp4rx.hackereartholaapp.adapter.MusicListAdapter;
import io.github.sp4rx.hackereartholaapp.database.MusicCache;
import io.github.sp4rx.hackereartholaapp.pojo.MusicListPojo;

/**
 * Created by suvajit.
 */

public class MostPlayedSongsFragment extends Fragment {
    private static final String TAG = MostPlayedSongsFragment.class.getSimpleName();
    private Context context;
    private ArrayList<MusicListPojo> musicListPojos;
    public MusicListAdapter musicListAdapter;
    private RecyclerView rvMusicList;
    private TextView noDataMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favourite_songs, container, false);

        //Context
        context = getContext();

        //Init -- Start
        //Music List Recycler view
        rvMusicList = rootView.findViewById(R.id.rvFavouriteSongsList);
        musicListPojos = new ArrayList<>();
        musicListAdapter = new MusicListAdapter(musicListPojos);
        musicListAdapter.setOnItemClickListener(onItemClickListener);
        rvMusicList.setLayoutManager(new LinearLayoutManager(context));
        rvMusicList.setAdapter(musicListAdapter);
        //Error message
        noDataMessage = rootView.findViewById(R.id.tvFavouriteSongsNoDataMessage);
        //Init -- End

        updateMusicList();

        return rootView;
    }

    /**
     * Get data from local and update music list
     */
    public void updateMusicList() {
        ArrayList<MusicListPojo> tempMusicListPojos = MusicCache.getMostPlayedSongs(context);
        if (tempMusicListPojos != null) {
            if (rvMusicList != null)
                rvMusicList.setVisibility(View.VISIBLE);
            if (noDataMessage != null)
                noDataMessage.setVisibility(View.GONE);
            this.musicListPojos.clear();
            this.musicListPojos.addAll(tempMusicListPojos);
            musicListAdapter.notifyDataSetChanged();
        } else {
            if (rvMusicList != null)
                rvMusicList.setVisibility(View.GONE);
            //Set no data available message
            if (noDataMessage != null) {
                noDataMessage.setText(R.string.no_most_played_songs);
                noDataMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    MusicListAdapter.OnItemClickListener onItemClickListener = new MusicListAdapter.OnItemClickListener() {
        @Override
        public void onPlayButtonClick(MusicListPojo musicListPojo) {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).playSong(musicListPojo);
            }
        }

        @Override
        public void onPauseButtonClick() {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).pauseSong();
            }
        }

        @Override
        public void onFavouriteClick(MusicListPojo musicListPojo, boolean isFavourite) {

        }

        @Override
        public void onDownloadButtonClick(MusicListPojo musicListPojo, int position) {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).downloadSong(musicListPojo, position);
            }
        }
    };
}
