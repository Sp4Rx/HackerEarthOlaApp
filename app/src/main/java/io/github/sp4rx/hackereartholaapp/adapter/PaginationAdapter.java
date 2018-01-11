package io.github.sp4rx.hackereartholaapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.github.sp4rx.hackereartholaapp.R;

/**
 * Created by suvajit.<br>
 * RecyclerView adapter for bottom pagination number list
 */

public class PaginationAdapter extends RecyclerView.Adapter<PaginationAdapter.ViewHolder> {
    public int maxPages;
    private OnItemClickListener onItemClickListener;
    public int rowIndex = -1;

    /**
     * Start page
     */
    private static final int TYPE_START = 0;
    /**
     * Pages between start and end
     */
    private static final int TYPE_MIDDLE = 1;
    /**
     * End page
     */
    private static final int TYPE_END = 2;

    /**
     * Interface for handling on click events
     */
    public interface OnItemClickListener {
        void onPageNoClick(int pageNo);
    }

    /**
     * Constructor
     *
     * @param maxPages Maximum number of pages
     */
    public PaginationAdapter(int maxPages) {
        this.maxPages = maxPages;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public PaginationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_page, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PaginationAdapter.ViewHolder holder, int position) {
        final int positionFinal = position;
        //Switch page background according to its position
        switch (getItemViewType(position)) {
            case TYPE_START:
                if (rowIndex == position || rowIndex == -1) {
                    holder.btPage.setBackgroundResource(R.drawable.bg_pages_start_selected);
                } else {
                    holder.btPage.setBackgroundResource(R.drawable.bg_pages_start);
                }
                break;
            case TYPE_MIDDLE:
                if (rowIndex == position) {
                    holder.btPage.setBackgroundResource(R.drawable.bg_pages_selected);
                } else {
                    holder.btPage.setBackgroundResource(R.drawable.bg_pages);
                }
                break;
            case TYPE_END:
                if (rowIndex == position) {
                    holder.btPage.setBackgroundResource(R.drawable.bg_pages_end_selected);
                } else {
                    holder.btPage.setBackgroundResource(R.drawable.bg_pages_end);
                }
                break;
        }
        final int pageNo = position + 1;
        holder.btPage.setText(String.valueOf(pageNo));
        holder.btPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rowIndex != positionFinal) {
                    onItemClickListener.onPageNoClick(pageNo);
                    rowIndex = positionFinal;
                    notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Returns max item count
     *
     * @return Max item count
     */
    @Override
    public int getItemCount() {
        return maxPages;
    }

    /**
     * Returns item type based on its position
     *
     * @param position current position
     * @return item type of {@link #TYPE_START}, {@link #TYPE_MIDDLE} or {@link #TYPE_END}
     */
    @Override
    public int getItemViewType(int position) {
        if (isPositionStart(position)) {
            return TYPE_START;
        } else if (isPositionEnd(position)) {
            return TYPE_END;
        }
        return TYPE_MIDDLE;
    }

    /**
     * Checks if current position is end position
     *
     * @param position Current position
     * @return true if position equals to end position
     */
    private boolean isPositionEnd(int position) {
        return position == this.maxPages - 1;
    }

    /**
     * Checks if current position is start position
     *
     * @param position Current position
     * @return true if position equals to start position
     */
    private boolean isPositionStart(int position) {
        return position == 0;
    }

    /**
     * ViewHolder class for the {@link PaginationAdapter }
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        Button btPage;

        ViewHolder(View itemView) {
            super(itemView);
            btPage = itemView.findViewById(R.id.btRowPage);
        }
    }
}
