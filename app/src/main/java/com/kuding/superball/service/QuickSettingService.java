package com.kuding.superball.service;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.kuding.superball.Utils.PreferenceUtils;


public class QuickSettingService extends TileService {

    /**
     * Called when the user adds this tile to Quick Settings.
     * <p/>
     * Note that this is not guaranteed to be called between {@link #onCreate()}
     * and {@link #onStartListening()}, it will only be called when the tile is added
     * and not on subsequent binds.
     */
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    /**
     * Called when the user removes this tile from Quick Settings.
     */
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }


    @Override
    public void onStartListening() {
        super.onStartListening();

        Tile mTile = getQsTile();

        if (NowKeyService.onDestroying) {
            mTile.setState(Tile.STATE_INACTIVE);
            mTile.updateTile();
        } else {
            mTile.setState(Tile.STATE_ACTIVE);
            mTile.updateTile();
        }
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        Tile mTile = getQsTile();

        if (NowKeyService.onDestroying) {
//            Intent intent = new Intent(this, NowKeyActivity.class);
//            startActivityAndCollapse(intent);
            mTile.setState(Tile.STATE_ACTIVE);
            PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_OPTION, true);
            Intent serviceIntent = new Intent(this, NowKeyService.class);
            serviceIntent.setAction(NowKeyService.START_NOW_KEY);
            startService(serviceIntent);
        } else {
            mTile.setState(Tile.STATE_INACTIVE);
            PreferenceUtils.setBoolean(PreferenceUtils.NOW_KEY_OPTION, false);
            Intent intent = new Intent(this, NowKeyService.class);
            stopService(intent);
        }
        mTile.updateTile();
    }
}