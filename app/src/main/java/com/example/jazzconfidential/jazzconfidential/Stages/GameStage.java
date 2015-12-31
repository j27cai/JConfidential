package com.example.jazzconfidential.jazzconfidential.Stages;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by simonvilleneuve on 15-07-04.
 */
public interface GameStage {
    void setGameStage(GameStage gameStage);

    void onDraw(Canvas canvas);
    boolean onTouch(View v, MotionEvent event);
}
