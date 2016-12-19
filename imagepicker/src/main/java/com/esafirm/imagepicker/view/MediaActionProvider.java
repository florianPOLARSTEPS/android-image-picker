package com.esafirm.imagepicker.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import java.util.List;

/**
 * Created by florian on 15/12/2016.
 * (c) Polarsteps
 */

public class MediaActionProvider extends ActionProvider implements MenuItem.OnMenuItemClickListener {

    public interface OnIntentClickListener {
        void onIntentClick(Intent intent);
    }

    private final Context mContext;
    private Intent mIntent;
    private List<ResolveInfo> mResolveInfos;
    private OnIntentClickListener mListener;

    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public MediaActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Nullable
    public Drawable getFirstIcon() {
        PackageManager packageManager = mContext.getPackageManager();
        for (ResolveInfo mResolveInfo : mResolveInfos) {
            if (!"com.android.documentsui".equals(mResolveInfo.activityInfo.packageName)) {
                return mResolveInfo.loadIcon(packageManager);
            }
        }

        return null;
    }

    public int getItemCount() {
        return mResolveInfos != null ? mResolveInfos.size() : 0;
    }

    public void setIntent(Intent shareIntent) {
        mIntent = shareIntent;
        mResolveInfos = mContext.getPackageManager()
                .queryIntentActivities(mIntent, 0);

        refreshVisibility();
    }

    public void setOnIntentClickListener(OnIntentClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (mIntent != null
                && mListener != null
                && mResolveInfos != null
                && mResolveInfos.size() > menuItem.getItemId()) {
            ResolveInfo resolveInfo = mResolveInfos.get(menuItem.getItemId());
            ComponentName matchedComponent = new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name);
            mIntent.setComponent(matchedComponent);
            mListener.onIntentClick(mIntent);
        }
        return true;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public View onCreateActionView(MenuItem forItem) {
        return null;
    }

    @Override
    public boolean hasSubMenu() {
        return mResolveInfos != null && (mResolveInfos.size() > 0);
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        // Clear since the order of items may change.
        subMenu.clear();

        if (mIntent == null) {
            return;
        }

        PackageManager packageManager = mContext.getPackageManager();
        final int resolveInfoCount = mResolveInfos.size();
        for (int i = 0; i < resolveInfoCount; i++) {
            ResolveInfo resolveInfo = mResolveInfos.get(i);
            subMenu.add(0, i, i, resolveInfo.loadLabel(packageManager))
                    .setIcon(resolveInfo.loadIcon(packageManager))
                    .setOnMenuItemClickListener(this);
        }
    }
}
