package io.github.sp4rx.hackereartholaapp.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.sp4rx.hackereartholaapp.BuildConfig;
import io.github.sp4rx.hackereartholaapp.MainActivity;
import io.github.sp4rx.hackereartholaapp.R;
import io.github.sp4rx.hackereartholaapp.adapter.MusicListAdapter;
import io.github.sp4rx.hackereartholaapp.adapter.PaginationAdapter;
import io.github.sp4rx.hackereartholaapp.database.MusicCache;
import io.github.sp4rx.hackereartholaapp.networking.VolleyResponseListener;
import io.github.sp4rx.hackereartholaapp.networking.VolleyUtils;
import io.github.sp4rx.hackereartholaapp.pojo.MusicListPojo;
import io.github.sp4rx.hackereartholaapp.util.Toaster;

import static io.github.sp4rx.hackereartholaapp.global.Constants.OLA_PLAY_STUDIOS_API;

/**
 * Created by suvajit.
 */

public class AllSongsFragment extends Fragment {
    private static final String TAG = AllSongsFragment.class.getSimpleName();
    private Context context;
    private ArrayList<MusicListPojo> musicListPojos;
    public MusicListAdapter musicListAdapter;
    private SwipeRefreshLayout srlMusicList;
    private int currentPage = 0;
    private RecyclerView rvMusicList, rvPagination;
    private SearchView searchView;
    private TextView noDataMessage;
    private PaginationAdapter paginationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_songs, container, false);

        //Context
        context = getContext();

        //Init -- Start
        //Music List Recycler view
        rvMusicList = rootView.findViewById(R.id.rvAllSongsList);
        musicListPojos = new ArrayList<>();
        musicListAdapter = new MusicListAdapter(musicListPojos);
        musicListAdapter.setOnItemClickListener(onItemClickListener);
        rvMusicList.setLayoutManager(new LinearLayoutManager(context));
        rvMusicList.setAdapter(musicListAdapter);
        //Error message
        noDataMessage = rootView.findViewById(R.id.tvAllSongsNoDataMessage);
        //SwipeRefreshLayout Layout
        srlMusicList = rootView.findViewById(R.id.srlAllSongsListRefresh);
        srlMusicList.setColorSchemeResources(R.color.darkGrey);
        srlMusicList.setOnRefreshListener(onRefreshListener);
        //Pagination Recycler View
        rvPagination = rootView.findViewById(R.id.rvAllSongsPagination);
        paginationAdapter = new PaginationAdapter(0);
        paginationAdapter.setOnItemClickListener(new PaginationAdapter.OnItemClickListener() {
            @Override
            public void onPageNoClick(int pageNo) {
                currentPage = pageNo;
                updateMusicList();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvPagination.setLayoutManager(linearLayoutManager);
        rvPagination.setAdapter(paginationAdapter);
        paginationAdapter.rowIndex = currentPage - 1;
        //Init -- End

        //get available data from local cache
        createPaginationViews();
        updateMusicList();
        if (isNetworkConnected()) {
            //Api call
            if (!srlMusicList.isRefreshing())
                srlMusicList.setRefreshing(true);
            getData();
        }

        return rootView;
    }

    /**
     * Call the OLA API and retrieves data from it
     */
    private void getData() {
        VolleyUtils.GET_METHOD(context, OLA_PLAY_STUDIOS_API, new VolleyResponseListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "OLA_PLAY_STUDIOS_API: " + response);
                //Save data to local cache
                saveListToCache(response);
            }

            @Override
            public void onError(String message) {
                if (srlMusicList.isRefreshing())
                    srlMusicList.setRefreshing(false);
                Toaster.makeText(context, R.string.no_internet, Toaster.LENGTH_SHORT).show();
                Log.e(TAG, "OLA_PLAY_STUDIOS_API error: " + message);
            }
        });
    }

    /**
     * Save songs data to local database
     *
     * @param response Response from the API
     */
    private void saveListToCache(String response) {
        ArrayList<MusicListPojo> musicListPojos = new ArrayList<>();
        try {
            JSONArray jsonRoot = new JSONArray(response);
            for (int i = 0; i < jsonRoot.length(); i++) {
                JSONObject jsonSongObject = jsonRoot.getJSONObject(i);
                musicListPojos.add(new MusicListPojo(
                        jsonSongObject.getString("song"),
                        jsonSongObject.getString("url"),
                        jsonSongObject.getString("artists"),
                        jsonSongObject.getString("cover_image")));
            }

            if (MusicCache.saveSongs(context, musicListPojos)) {
                createPaginationViews();
                updateMusicList();
            } else {
                Toaster.makeText(context, R.string.went_wrong, Toaster.LENGTH_LONG).show();
                Log.e(TAG, "Failed to save songs");
            }

        } catch (JSONException e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
            Toaster.makeText(context, R.string.went_wrong, Toaster.LENGTH_LONG).show();
        }
    }

    /**
     * Get data from local and update music list
     */
    public void updateMusicList() {
        ArrayList<MusicListPojo> tempMusicListPojos = MusicCache.getSongsByPage(context, currentPage);
        if (tempMusicListPojos != null) {
            rvMusicList.setVisibility(View.VISIBLE);
            noDataMessage.setVisibility(View.GONE);
            this.musicListPojos.clear();
            this.musicListPojos.addAll(tempMusicListPojos);
            musicListAdapter.notifyDataSetChanged();
        } else {
            rvMusicList.setVisibility(View.GONE);
            //Set no data available message
            noDataMessage.setVisibility(View.VISIBLE);
        }

        if (srlMusicList.isRefreshing())
            srlMusicList.setRefreshing(false);

    }


    /**
     * Creates the bottom page numbers
     */
    private void createPaginationViews() {
        int maxPages = MusicCache.getMaxPages(context);
        if (maxPages > 1) {
            paginationAdapter.maxPages = maxPages;
            paginationAdapter.notifyDataSetChanged();
        }
    }


    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (isNetworkConnected()) {
                //Api call
                getData();
            } else {
                //get available data from local cache
                updateMusicList();
            }
        }
    };

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


    /**
     * Checks if internet is available
     *
     * @return true/false based on Network connectivity
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        Context themedContext = ((MainActivity) context).getSupportActionBar().getThemedContext();
        if (themedContext != null) {
            searchView = new SearchView(themedContext);
        } else {
            searchView = new SearchView(context);
        }
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                musicListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                musicListAdapter.getFilter().filter(query);
                return false;
            }
        });
//        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_search || super.onOptionsItemSelected(item);
    }
}
