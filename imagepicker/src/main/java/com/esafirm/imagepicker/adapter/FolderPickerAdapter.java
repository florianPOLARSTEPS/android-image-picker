package com.esafirm.imagepicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.helper.FrescoHelper;
import com.esafirm.imagepicker.listeners.OnFolderClickListener;
import com.esafirm.imagepicker.model.Folder;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by boss1088 on 8/22/16.
 */
public class FolderPickerAdapter extends RecyclerView.Adapter<FolderPickerAdapter.FolderViewHolder> {

    private final OnFolderClickListener folderClickListener;
    private Context context;
    private LayoutInflater inflater;
    private List<Folder> folders;
    private int maxWidth;
    private int maxHeight;

    public FolderPickerAdapter(Context context, OnFolderClickListener folderClickListener) {
        this.context = context;
        this.folderClickListener = folderClickListener;
        inflater = LayoutInflater.from(this.context);
        maxHeight = context.getResources().getDimensionPixelSize(R.dimen.ef_thumb_max_height);
        maxWidth = context.getResources().getDimensionPixelSize(R.dimen.ef_thumb_max_width);
    }

    public void setData(List<Folder> folders) {
        this.folders = folders;
        notifyDataSetChanged();
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.ef_imagepicker_item_folder, parent, false);
        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FolderViewHolder holder, int position) {

        final Folder folder = folders.get(position);

        if (folder.getImages() != null && folder.getImages().size() > 0) {
            Uri uri = folder.getImages().get(0).getUri();
            if (uri != null) {
                FrescoHelper.setImageUriResizeToImage(holder.image, uri, maxWidth, maxHeight);
            }
        }
        holder.name.setText(folders.get(position).getFolderName());
        holder.number.setText(String.valueOf(folders.get(position).getImages().size()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderClickListener != null)
                    folderClickListener.onFolderClick(folder);
            }
        });

        TypedValue typedValue = new TypedValue();
        if (folder.getType() == Folder.Type.RECENT) {
            context.getTheme().resolveAttribute(R.attr.ef_colorHighlight, typedValue, true);
            holder.background.setBackgroundColor(typedValue.data);
            context.getTheme().resolveAttribute(R.attr.ef_textHighlightColor, typedValue, true);
            holder.number.setTextColor(typedValue.data);
        } else {
            context.getTheme().resolveAttribute(R.attr.ef_colorBackground, typedValue, true);
            holder.background.setBackgroundColor(typedValue.data);
            context.getTheme().resolveAttribute(R.attr.ef_textSecondaryColor, typedValue, true);
            holder.number.setTextColor(typedValue.data);
        }

    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {

        private SimpleDraweeView image;
        private TextView name;
        private TextView number;
        private View background;

        public FolderViewHolder(View itemView) {
            super(itemView);

            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            number = (TextView) itemView.findViewById(R.id.tv_number);
            background = itemView.findViewById(R.id.v_bg);
        }
    }

}
