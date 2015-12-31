package com.example.jazzconfidential.jazzconfidential.Stages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.example.jazzconfidential.jazzconfidential.Game.Coordinate;
import com.example.jazzconfidential.jazzconfidential.Game.Game;
import com.example.jazzconfidential.jazzconfidential.Game.Unit;
import com.example.jazzconfidential.jazzconfidential.UnitView;
import com.parse.ParsePush;

import java.util.Iterator;
import java.util.List;

/**
 * Created by simonvilleneuve on 15-07-04.
 */
public class MovementStage implements GameStage {
    private UnitView context;

    public MovementStage(UnitView context) {
        this.context = context;
    }

    @Override
    public void setGameStage(GameStage gameStage) {
        context.setGameStage(gameStage);
    }

    @Override
    public void onDraw(Canvas canvas) {
        List<Coordinate> movementTile = context.game.getRangeTiles();
        if (movementTile != null) {
            for (int i = 0; i < movementTile.size(); i++) {
                float left = movementTile.get(i).getX() * context.tileWidth;
                float top = movementTile.get(i).getY() * context.tileHeight;
                float right = left + context.tileWidth;
                float bottom = top + context.tileHeight;

                context.paint.setColor(Color.rgb(0, 153, 255));

                // Draw selection tile
                canvas.drawRect(left, top, right, bottom, context.paint);

                // Draw selection tile border
                Paint.Style style = context.paint.getStyle();
                context.paint.setStyle(Paint.Style.STROKE);
                context.paint.setColor(Color.BLACK);
                canvas.drawRect(left, top, right, bottom, context.paint);
                context.paint.setStyle(style);
            }
        }

        // Draw any units in play
        List<Unit> units = context.game.getUnits();
        for (Iterator<Unit> i = units.iterator(); i.hasNext(); ) {
            Unit unit = i.next();
            Coordinate location = unit.getLocation();

            int unitLocX = location.getX(), unitLocY = location.getY();

            // Draw the unit
            canvas.drawBitmap(context.unitTypeToBitMap(unit.getUnitType(), (int)context.tileWidth, (int)context.tileHeight), unitLocX * context.tileWidth, unitLocY * context.tileHeight, null);

            // Draw the unit potential location if it's movement phase
            if (unit.getAlliance() == context.game.currentPlayer) {
                Coordinate targetLocation = unit.getTargetLocation();
                int unitTargetLocX = targetLocation.getX(), unitTargetLocY = targetLocation.getY();
                if (unitTargetLocX != unitLocX || unitTargetLocY != unitLocY) {
                    //System.out.println("Simon: " + unitLocX + ", " + unitLocY + ", " + unitTargetLocX + ", " + unitTargetLocY + ", ");

                    float stroke = context.paint.getStrokeWidth();
                    context.paint.setStrokeWidth(10);
                    canvas.drawLine(unitLocX * context.tileWidth + (context.tileWidth / 2), unitLocY * context.tileHeight + (context.tileHeight / 2), unitTargetLocX * context.tileWidth + (context.tileWidth / 2), unitTargetLocY * context.tileHeight + (context.tileHeight / 2), context.paint);
                    context.paint.setStrokeWidth(stroke);
                    Paint paint = new Paint();
                    paint.setAlpha(150);
                    canvas.drawBitmap(context.unitTypeToBitMap(unit.getUnitType(), (int)context.tileWidth, (int)context.tileHeight), unitLocX * context.tileWidth, unitLocY * context.tileHeight, paint);
                }
            }

            // Draw the unit's health
            int currentHealth = unit.getCurrentHealth(), maxHealth = unit.getMaxHealth();
            float percentHealth = currentHealth / (float) maxHealth;

            float left = unitLocX * context.tileWidth;
            float top = unitLocY * context.tileHeight;
            float right = left + context.tileWidth;
            float bottom = top + 10;

            // Draw max health bar
            context.paint.setColor(Color.DKGRAY);
            canvas.drawRect(left, top, right, bottom, context.paint);

            // Draw border for max health bar
            Paint.Style style = context.paint.getStyle();
            context.paint.setStyle(Paint.Style.STROKE);
            context.paint.setColor(Color.BLACK);
            canvas.drawRect(left, top, right, bottom, context.paint);
            context.paint.setStyle(style);

            // Draw current health bar
            context.paint.setColor(unit.getAlliance() == 0 ? Color.RED : Color.YELLOW);
            canvas.drawRect(left, top, left + (context.tileWidth * percentHealth), bottom, context.paint);

            // Draw border for current health bar
            style = context.paint.getStyle();
            context.paint.setStyle(Paint.Style.STROKE);
            context.paint.setColor(Color.BLACK);
            canvas.drawRect(left, top, left + (context.tileWidth * percentHealth), bottom, context.paint);
            context.paint.setStyle(style);
        }
    }

    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            AlertDialog.Builder alert = new AlertDialog.Builder(context.getContext());
            alert.setTitle("End Phase!");
            alert.setMessage("Are you sure you want to end your movement phase?");
            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.game.currentPlayer = (context.game.currentPlayer + 1) % 2;

                    if (context.game.gameType == Game.GameType.SinglePlayer && context.game.currentPlayer == 1) {
                        playAITurn();
                    }

                    context.invalidate();
                    if (context.game.currentPlayer == 0) {
                        for (Unit u : context.game.getUnits()) {
                            Coordinate target = u.getTargetLocation();
                            Coordinate current = u.getLocation();
                            if (target.getX() != current.getX() || target.getY() != current.getY()) {
                                Coordinate empty = context.getEmptyLocation(u, target);
                                u.setTargetLocation(empty);
                                u.setLocation(empty);
                            }
                        }

                        // Check if someone has occupied an opposing capture point long enough
                        boolean player1Wins = false, player2Wins = false;
                        List<Coordinate> capturePoints = context.game.getCapturePoints();
                        if (context.getUnitAt(capturePoints.get(0).getX(), capturePoints.get(0).getY(), 2) != null) {
                            player2Wins = context.game.capturePointOccupied(capturePoints.get(0));
                        }
                        else {
                            context.game.capturePointNotOccupied(capturePoints.get(0));
                        }

                        if (context.getUnitAt(capturePoints.get(1).getX(), capturePoints.get(1).getY(), 1) != null) {
                            player1Wins = context.game.capturePointOccupied(capturePoints.get(1));
                        }
                        else {
                            context.game.capturePointNotOccupied(capturePoints.get(1));
                        }

                        // End game now
                        if (player1Wins && player2Wins) {
                            context.game.winner = 2;
                            context.game.stage = context.game.stage.End;
                            setGameStage(new EndStage(context));
                        }
                        else if (player1Wins) {
                            context.game.winner = 0;
                            context.game.stage = context.game.stage.End;
                            setGameStage(new EndStage(context));
                        }
                        else if (player2Wins) {
                            context.game.winner = 1;
                            context.game.stage = context.game.stage.End;
                            setGameStage(new EndStage(context));
                        }
                        else { // Otherwise, keep playing
                            context.game.stage = context.game.stage.Attack;
                            setGameStage(new AttackStage(context));
                        }

                    }

                    if (context.game.gameType == Game.GameType.Multiplayer){
                        ParsePush push = new ParsePush();
                        push.setChannel("p"+ (context.game.currentPlayer == 0 ? context.game.createdBy : context.game.playedWith));
                        push.setMessage("Your opponent finished their movement stage! It's your turn now!");
                        push.sendInBackground();

                        context.gameActivity.onBackPressed();
                    }

                    dialog.dismiss();

                }
            });
            alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            alert.show();
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            // Initiate count for long press
            handler.postDelayed(mLongPressed, 500);

            // Get x and y of touch event
            float x = event.getX() / context.scaleFactor + context.rect.left, y = event.getY() / context.scaleFactor + context.rect.top;

            int xCoord = (int) (x / context.tileWidth), yCoord = (int) (y / context.tileHeight);

            if (xCoord >= 20 && yCoord <= 2) {
                handler.post(mLongPressed);
                return true;
            }

            if (context.processingUnit) {
                List<Coordinate> rangeTiles = context.game.getRangeTiles();
                if (context.canTarget(rangeTiles, xCoord, yCoord)) {
                    Coordinate targetLocation = new Coordinate(xCoord, yCoord);
                    context.selectedUnit.setTargetLocation(targetLocation);
                    context.selectedUnit.setProcessed(true);

                    context.game.setRangeTiles(null);
                    context.processingUnit = false;
                } else if (xCoord == context.selectedUnit.getLocation().getX() && yCoord == context.selectedUnit.getLocation().getY()) {
                    context.game.setRangeTiles(null);
                    context.processingUnit = false;
                }
            } else {
                Unit u = context.getUnitAt(xCoord, yCoord, 1);
                if (u != null) {
                    context.selectedUnit = u;
                    context.game.setRangeTiles(context.generateTile(u, 0));
                    context.processingUnit = true;
                }
            }
        }
        if((event.getAction() == MotionEvent.ACTION_MOVE)||(event.getAction() == MotionEvent.ACTION_UP)) {
            // That wasn't no long press fool
            handler.removeCallbacks(mLongPressed);
        }

        return true;
    }

    private void playAITurn() {
        List<Unit> aiUnits = context.game.getUnits();

        // Cycle through all units cause there's no better way right now
        for (int i = 0; i < aiUnits.size(); i++) {
            Unit aiUnit = aiUnits.get(i);

            // We're dealing with an ai unit, so let's do something
            if (aiUnit.getAlliance() == context.game.currentPlayer) {
                // Get possible tiles to move to
                List<Coordinate> possibleMovementTiles = context.generateTile(aiUnit, 0);

                // Move to random possibility
                int randomIndex = (int)(Math.random() * possibleMovementTiles.size());
                aiUnit.setTargetLocation(possibleMovementTiles.get(randomIndex));
                aiUnit.setProcessed(true);
            }
        }

        context.game.currentPlayer = (context.game.currentPlayer + 1) % 2;
    }
}
