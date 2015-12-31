package com.example.jazzconfidential.jazzconfidential;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.jazzconfidential.jazzconfidential.Game.Game;

public class GameActivity extends Activity {
    public static Game gameToLoad = null;
    static UnitView unitView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Force landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Create prototype game
        if (gameToLoad == null) {
            gameToLoad = new Game(MainMenuActivity.gameModeSelected, LoginFragment.UserId, MainMenuActivity.playedWith, LoginFragment.UserName,MainMenuActivity.playedWithUserName);
        }
        /*else
            gameToLoad = new Game(gameToLoad.getMap(), gameToLoad.getUnits(), gameToLoad.getAvailableUnits(), gameToLoad.getCapturePoints(),
                    gameToLoad.getSetupTiles(), gameToLoad.getRangeTiles(), gameToLoad.getPowah(), gameToLoad.currentPlayer, gameToLoad.winner,
                    gameToLoad.remainingUnits, gameToLoad.stage, gameToLoad.gameType);*/

        // Get Fragment's FrameLayout
        //Database.LoadGame(2);

        FrameLayout frameLayout = new FrameLayout(this);
        setContentView(frameLayout);

        // Add view representing map
        //MapView gb = new MapView(this, gameToLoad);
        //frameLayout.addView(gb);

        // Add view representing units
        unitView = new UnitView(this, gameToLoad, this);
        frameLayout.addView(unitView);
    }

    @Override
    public void onBackPressed() {
        Database.SaveGame(gameToLoad);
        gameToLoad = null;

        MainMenuActivity.gameAdapter.UpdateData();

        finish();
    }
}