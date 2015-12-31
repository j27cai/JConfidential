package com.example.jazzconfidential.jazzconfidential;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.jazzconfidential.jazzconfidential.Game.Coordinate;
import com.example.jazzconfidential.jazzconfidential.Game.Game;

import java.util.List;

/**
 * Created by simonvilleneuve on 15-07-04.
 */
public class MapView extends View {
    private Paint paint;
    private Game game;

    // How wide/high individual tiles are
    float tileWidth = 0, tileHeight = 0;

    public MapView(Context context, Game game) {
        super(context);

        this.game = game;
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the map
        int[][] map = game.getMap();
        Bitmap bm = null;
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_grs);
        for (int row = 0; row < 14; row++) {
            for (int col = 0; col < 22; col++) {
                // Set color
                if (map[row][col] == 0) {
                    paint.setColor(Color.BLUE);
                }
                else if (map[row][col] == 2) {
                    paint.setColor(Color.GREEN);
                }
                else if (map[row][col] == 3) {
                    paint.setColor(Color.GRAY);
                }

                // Calculate left/top/right/bottom
                float left = col * tileWidth;
                float top = row * tileHeight;
                float right = left + tileWidth;
                float bottom = top + tileHeight;

                // Draw tile
                canvas.drawBitmap(bm, left, top, null);

                // Draw outline of tile
                Paint.Style style = paint.getStyle();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
//                canvas.drawRect(left, top, right, bottom, paint);
                paint.setStyle(style);
            }
        }

        // Draw the capture points
        List<Coordinate> capturePoints = game.getCapturePoints();
        for (int i = 0; i < capturePoints.size(); i++) {
            // Calculate left/top/right/bottom
            float left = (capturePoints.get(i).getX() * tileWidth) + 20;
            float top = (capturePoints.get(i).getY() * tileHeight) + 20;
            float right = left + (tileWidth - 40);
            float bottom = top + (tileHeight - 40);

            // Draw capture point
            paint.setColor(Color.BLACK);
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasuredSpec, int heightMeasuredSpec) {
        // Get the screen's width and height
        int width = MeasureSpec.getSize(widthMeasuredSpec);
        int height = MeasureSpec.getSize(heightMeasuredSpec);

        int [][]map = game.getMap();

        // Calculate tile height/width
        if (map.length > 0) {
            tileHeight = (float) height / map.length;
            tileWidth = (float) width / map[0].length;
        }

        // Set view to be size of screen
        setMeasuredDimension(width, height);
    }
}
