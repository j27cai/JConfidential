package com.example.jazzconfidential.jazzconfidential.Stages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

import com.example.jazzconfidential.jazzconfidential.GameActivity;
import com.example.jazzconfidential.jazzconfidential.LoginFragment;
import com.example.jazzconfidential.jazzconfidential.UnitView;

/**
 * Created by simonvilleneuve on 15-07-04.
 */
public class EndStage implements GameStage {
    private UnitView context;

    public EndStage(UnitView context) {
        this.context = context;
    }

    @Override
    public void setGameStage(GameStage gameStage) {
        context.setGameStage(gameStage);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (context.game.winner == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context.getContext());
            builder.setTitle("You tied!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            context.gameActivity.onBackPressed();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if ((LoginFragment.UserId.equals(context.game.createdBy) && context.game.winner == 0) ||
                (LoginFragment.UserId.equals(context.game.playedWith) && context.game.winner == 1)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context.getContext());
            builder.setTitle("You won!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            context.gameActivity.onBackPressed();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context.getContext());
            builder.setTitle("You lost!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            context.gameActivity.onBackPressed();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        /*float left = context.tileWidth * 5;
        float top = context.tileHeight * 5;
        float right = context.tileWidth * 17;
        float bottom = context.tileHeight * 10;

        context.paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, right, bottom, context.paint);

        context.paint.setColor(Color.BLACK);
        context.paint.setTextSize(36);

        if (context.game.winner == 0) {
            canvas.drawText("Player 1 Wins!", 0, 14, left + (context.tileWidth * 5), top + context.tileHeight, context.paint);
        } else if (context.game.winner == 1) {
            canvas.drawText("Player 2 Wins!", 0, 14, left + (context.tileWidth * 5), top + context.tileHeight, context.paint);
        } else {
            canvas.drawText("Tie Game :/", 0, 11, left + (context.tileWidth * 5), top + context.tileHeight, context.paint);
        }

        left = context.tileWidth * 9;
        top = context.tileHeight * 8;
        right = context.tileWidth * 14;
        bottom = context.tileHeight * 9;

        context.paint.setColor(Color.BLUE);
        canvas.drawRect(left, top, right, bottom, context.paint);

        context.paint.setColor(Color.WHITE);
        context.paint.setTextSize(36);

        canvas.drawText("Peace Out", 0, 9, left + context.tileWidth, top + (context.tileHeight / 2), context.paint);*/
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Get x and y of touch event
        float x = event.getX() / context.scaleFactor + context.rect.left, y = event.getY() / context.scaleFactor + context.rect.top;

        int xCoord = (int) (x / context.tileWidth), yCoord = (int) (y / context.tileHeight);

        if (xCoord >= 9 && xCoord <= 14 && yCoord >= 8 && yCoord <= 9) {
            GameActivity.gameToLoad = context.game;
            ((Activity) context.getContext()).finish();
        }

        return true;
    }
}
