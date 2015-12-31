package com.example.jazzconfidential.jazzconfidential;

import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Derek on 2015-07-19.
 */
public class JazzPushBroadcastReceiver extends ParsePushBroadcastReceiver {
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        if (GameActivity.gameToLoad == null){
            Database.LoadGames(LoginFragment.UserId);
            MainMenuActivity.gameAdapter.UpdateData();
        }

        super.onPushReceive(context, intent);
    }
}
