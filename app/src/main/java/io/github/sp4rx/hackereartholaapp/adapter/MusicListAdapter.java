package io.github.sp4rx.hackereartholaapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import io.github.sp4rx.hackereartholaapp.R;
import io.github.sp4rx.hackereartholaapp.database.MusicCache;
import io.github.sp4rx.hackereartholaapp.pojo.MusicListPojo;

import static io.github.sp4rx.hackereartholaapp.global.Constants.FILE_EXTENSION;
import static io.github.sp4rx.hackereartholaapp.global.Constants.STORAGE_DIR_PATH;

/**
 * Created by suvajit.<br>
 * RecyclerView adapter for music list row
 */

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> implements Filterable {
    private ArrayList<MusicListPojo> musicListPojosInitial;
    private ArrayList<MusicListPojo> musicListPojosFiltered;
    private ArrayList<ViewHolder> holders;

    /**
     * Interface for handling on click events
     */
    public interface OnItemClickListener {
        void onPlayButtonClick(MusicListPojo musicListPojo);

        void onPauseButtonClick();

        void onFavouriteClick(MusicListPojo musicListPojo, boolean isFavourite);

        void onDownloadButtonClick(MusicListPojo musicListPojo, int position);
    }

    private OnItemClickListener onItemClickListener;

    /**
     * Constructor
     *
     * @param musicListPojos {@link ArrayList}<{@link MusicListPojo}>
     */
    public MusicListAdapter(ArrayList<MusicListPojo> musicListPojos) {
        this.musicListPojosInitial = musicListPojos;
        this.musicListPojosFiltered = musicListPojos;
        holders = new ArrayList<>();
    }

    @Override
    public MusicListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_music_list, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final MusicListAdapter.ViewHolder holder, final int position) {
        holders.add(holder);
        final MusicListPojo musicListPojo = musicListPojosFiltered.get(position);
        holder.tvTitle.setText(musicListPojo.getSong());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ic_music_placeholder);
        Glide.with(holder.ivCoverImage.getContext())
                .load(musicListPojo.getCoverImage())
                .apply(requestOptions)
                .into(holder.ivCoverImage);

        holder.tvArtists.setText(String.format("Artists: %s", musicListPojo.getArtists()));

        showPlayButton(!musicListPojo.isPlaying(), holder);

        holder.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = holder.ivPlay.getContext();
                resetAllPauseButtons();
                showPlayButton(false, holder);
                int currentPlayedCount = musicListPojo.getPlayedCount();
                musicListPojo.setPlaying(true);
                musicListPojo.setPlayedCount(++currentPlayedCount);
                MusicCache.updateSongRecord(context, musicListPojo);
                onItemClickListener.onPlayButtonClick(musicListPojo);
            }
        });
        holder.ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlayButton(true, holder);
                musicListPojo.setPlaying(false);
                MusicCache.updateSongRecord(holder.ivPlay.getContext(), musicListPojo);
                onItemClickListener.onPauseButtonClick();
            }
        });

        final File checkSongFile = new File(STORAGE_DIR_PATH + musicListPojo.getSong() + FILE_EXTENSION);
        if (checkSongFile.exists()) {
            holder.ivDownload.setVisibility(View.GONE);
        } else {
            holder.ivDownload.setVisibility(View.VISIBLE);
        }
        holder.ivDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onDownloadButtonClick(musicListPojo, position);
            }
        });

        Context context = holder.cbFavourite.getContext();
        holder.cbFavourite.setChecked(MusicCache.getSongById(context, musicListPojo.getId()).isFavourite());
        holder.cbFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = view.findViewById(R.id.cbRowMusicListFavourite);
                Context context = holder.cbFavourite.getContext();
                musicListPojo.setFavourite(checkBox.isChecked());
                MusicCache.updateSongRecord(context, musicListPojo);
                onItemClickListener.onFavouriteClick(musicListPojo, checkBox.isChecked());
            }
        });
    }

    /**
     * Switch all pause buttons to play state
     */
    public void resetAllPauseButtons() {
        for (ViewHolder holder : holders) {
            showPlayButton(true, holder);
            MusicCache.updateSongRecord(holder.ivPlay.getContext(), null);
        }
    }

    /**
     * Toggle play/pause button based on the parameter
     *
     * @param show       true/false
     * @param viewHolder current {@link MusicListAdapter.ViewHolder}
     */
    private void showPlayButton(boolean show, MusicListAdapter.ViewHolder viewHolder) {
        if (show) {
            viewHolder.ivPlay.setVisibility(View.VISIBLE);
            viewHolder.ivPause.setVisibility(View.GONE);
        } else {
            viewHolder.ivPlay.setVisibility(View.GONE);
            viewHolder.ivPause.setVisibility(View.VISIBLE);
        }
    }

    public void hideDownloadButton(int position) {
        if (position <= holders.size()) {
            holders.get(position).ivDownload.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return musicListPojosFiltered.size();
    }

    /**
     * Filter the results based on the search text
     *
     * @return {@link Filter.FilterResults} of {@link ArrayList}<{@link MusicListPojo}>
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    musicListPojosFiltered = musicListPojosInitial;
                } else {
                    ArrayList<MusicListPojo> filteredList = new ArrayList<>();
                    for (MusicListPojo musicListPojo : musicListPojosInitial) {

                        //Filter Logic
                        if (musicListPojo.getSong().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(musicListPojo);
                        }
                    }

                    musicListPojosFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = musicListPojosFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                musicListPojosFiltered = (ArrayList<MusicListPojo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvArtists;
        private ImageView ivCoverImage, ivPlay, ivPause, ivDownload;
        private CheckBox cbFavourite;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRowMusicListSong);
            ivCoverImage = itemView.findViewById(R.id.ivRowMusicListCoverImage);
            tvArtists = itemView.findViewById(R.id.tvRowMusicListArtists);
            ivPlay = itemView.findViewById(R.id.ivRowMusicListPlay);
            ivPause = itemView.findViewById(R.id.ivRowMusicListPause);
            ivDownload = itemView.findViewById(R.id.ivRowMusicListDownload);
            cbFavourite = itemView.findViewById(R.id.cbRowMusicListFavourite);
        }
    }
}
