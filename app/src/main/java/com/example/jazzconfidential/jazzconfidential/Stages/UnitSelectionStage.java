package com.example.jazzconfidential.jazzconfidential.Stages;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.example.jazzconfidential.jazzconfidential.Game.Bowman;
import com.example.jazzconfidential.jazzconfidential.Game.Coordinate;
import com.example.jazzconfidential.jazzconfidential.Game.Game;
import com.example.jazzconfidential.jazzconfidential.Game.Horseman;
import com.example.jazzconfidential.jazzconfidential.Game.Pikeman;
import com.example.jazzconfidential.jazzconfidential.Game.Swordsman;
import com.example.jazzconfidential.jazzconfidential.Game.Unit;
import com.example.jazzconfidential.jazzconfidential.UnitView;
import com.parse.ParsePush;

import java.util.Iterator;
import java.util.List;

/**
 * Created by simonvilleneuve on 15-07-04.
 */
public class UnitSelectionStage implements GameStage {
    UnitView context;

    private boolean selectingUnit = false;

    public UnitSelectionStage(UnitView context) {
        this.context = context;
    }

    @Override
    public void setGameStage(GameStage gameStage) {
        context.setGameStage(gameStage);
    }

    @Override
    public void onDraw(Canvas canvas) {
        List<Coordinate> setupTiles = context.game.getSetupTiles().get(context.game.currentPlayer);

        for (int i = 0; i < setupTiles.size(); i++) {
            float left = setupTiles.get(i).getX() * context.tileWidth;
            float top = setupTiles.get(i).getY() * context.tileHeight;
            float right = left + context.tileWidth;
            float bottom = top + context.tileHeight;

            context.paint.setColor(Color.CYAN);

            // Draw selection tile
            canvas.drawRect(left, top, right, bottom, context.paint);

            // Draw selection tile border
            Paint.Style style = context.paint.getStyle();
            context.paint.setStyle(Paint.Style.STROKE);
            context.paint.setColor(Color.BLACK);
            canvas.drawRect(left, top, right, bottom, context.paint);
            context.paint.setStyle(style);
        }

        if (selectingUnit) {
            drawUnitSelection(canvas);
        }

        // Draw any units in play
        List<Unit> units = context.game.getUnits();
        for (Iterator<Unit> i = units.iterator(); i.hasNext(); ) {
            Unit unit = i.next();
            Coordinate location = unit.getLocation();

            int unitLocX = location.getX(), unitLocY = location.getY();


            // Draw the unit
            canvas.drawBitmap(context.unitTypeToBitMap(unit.getUnitType(), (int)context.tileWidth, (int)context.tileHeight), unitLocX * context.tileWidth, unitLocY * context.tileHeight, null);

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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Get x and y of touch event
            float x = event.getX() / context.scaleFactor + context.rect.left, y = event.getY() / context.scaleFactor + context.rect.top;

            List<Coordinate> setupTiles = context.game.getSetupTiles().get(context.game.currentPlayer);
            int xCoord = (int) (x / context.tileWidth), yCoord = (int) (y / context.tileHeight);

            // Determine if selection window is open
            if (selectingUnit) {
                // Indicates if we will remove a setup tile
                boolean remove = false;

                if (xCoord == 9 && yCoord == 7) {
                    // Don't do anything if there aren't any bowmen remaining
                    if (context.game.remainingUnits.get(Unit.UnitType.Bowman).get(context.game.currentPlayer) == 0) {
                        return true;
                    }

                    // Create a Bowman
                    Unit bowman = new Bowman(new Coordinate(context.selectX, context.selectY), context.game.currentPlayer);

                    // Subtract Bowman context.power from total
                    context.power -= Bowman.power;

                    // Add the bowman to the context.game
                    context.game.AddActiveUnit(bowman);

                    // Update remaining units count
                    context.game.remainingUnits.get(bowman.getUnitType()).put(context.game.currentPlayer, context.game.remainingUnits.get(bowman.getUnitType()).get(context.game.currentPlayer) - 1);

                    remove = true;
                } else if (xCoord == 10 && yCoord == 7) {
                    // Don't do anything if there aren't any Swordsmen remaining
                    if (context.game.remainingUnits.get(Unit.UnitType.Swordsman).get(context.game.currentPlayer) == 0) {
                        return true;
                    }

                    // Create a Swordsman
                    Unit swordsman = new Swordsman(new Coordinate(context.selectX, context.selectY), context.game.currentPlayer);

                    // Subtract Swordsman context.power from total
                    context.power -= swordsman.getPowerValue();

                    // Add the swordsman to the context.game
                    context.game.AddActiveUnit(swordsman);

                    // Update remaining units count
                    context.game.remainingUnits.get(swordsman.getUnitType()).put(context.game.currentPlayer, context.game.remainingUnits.get(swordsman.getUnitType()).get(context.game.currentPlayer) - 1);

                    remove = true;
                } else if (xCoord == 11 && yCoord == 7) {
                    // Don't do anything if there aren't any Pikemen remaining
                    if (context.game.remainingUnits.get(Unit.UnitType.Pikeman).get(context.game.currentPlayer) == 0) {
                        return true;
                    }

                    // Create a Pikeman
                    Unit pikeman = new Pikeman(new Coordinate(context.selectX, context.selectY), context.game.currentPlayer);

                    // Subtract Pikeman context.power from total
                    context.power -= pikeman.getPowerValue();

                    // Add the Pikeman to the context.game
                    context.game.AddActiveUnit(pikeman);

                    // Update remaining units count
                    context.game.remainingUnits.get(pikeman.getUnitType()).put(context.game.currentPlayer, context.game.remainingUnits.get(pikeman.getUnitType()).get(context.game.currentPlayer) - 1);

                    remove = true;
                } else if (xCoord == 12 && yCoord == 7) {
                    // Don't do anything if there aren't any Horsemen remaining
                    if (context.game.remainingUnits.get(Unit.UnitType.Horseman).get(context.game.currentPlayer) == 0) {
                        return true;
                    }

                    // Create a Horseman
                    Unit horseman = new Horseman(new Coordinate(context.selectX, context.selectY), context.game.currentPlayer);

                    // Subtract Horseman context.power from total
                    context.power -= horseman.getPowerValue();

                    // Add the Horseman to the context.game
                    context.game.AddActiveUnit(horseman);

                    // Update remaining units count
                    context.game.remainingUnits.get(horseman.getUnitType()).put(context.game.currentPlayer, context.game.remainingUnits.get(horseman.getUnitType()).get(context.game.currentPlayer) - 1);

                    remove = true;
                }

                if (remove) {
                    for (int i = 0; i < setupTiles.size(); i++) {
                        if (setupTiles.get(i).getX() == context.selectX && setupTiles.get(i).getY() == context.selectY) {
                            setupTiles.remove(i);
                            break;
                        }
                    }
                }

                // Determine if current player is done setting up
                if (setupTiles.isEmpty()) {
                    // Go to next player
                    context.game.currentPlayer = (context.game.currentPlayer + 1) % 2;

                    // Play for AI if necessary
                    playAITurn();

                    // Move on to movement context.game.stage if both players have setup
                    if (context.game.currentPlayer == 0) {
                        context.game.stage = context.game.stage.Movement;
                        setGameStage(new MovementStage(context));
                    }

                    if (context.game.gameType == Game.GameType.Multiplayer){
                        ParsePush push = new ParsePush();
                        push.setChannel("p"+ (context.game.currentPlayer == 0 ? context.game.createdBy : context.game.playedWith));
                        push.setMessage("Your opponent finished unit selection! It's your turn now!");
                        push.sendInBackground();
                    }

                    // Reset total context.power
                    context.power = context.game.getPowah();

                    if (context.game.gameType == Game.GameType.Multiplayer) {
                        context.gameActivity.onBackPressed();
                    }
                }

                selectingUnit = false;
            } else {
                // Determine if user selected a setup tile
                for (int i = 0; i < setupTiles.size(); i++) {
                    if (setupTiles.get(i).getX() == xCoord && setupTiles.get(i).getY() == yCoord) {
                        // record x and y
                        context.selectX = xCoord;
                        context.selectY = yCoord;

                        // Indicate that selection window should open
                        selectingUnit = true;

                        break;
                    }
                }
            }
        }

        return true;
    }

    private void playAITurn() {
        if (context.game.gameType == Game.GameType.SinglePlayer && context.game.currentPlayer == 1) {
            // Put one of each unit in the setup tiles
            List<Coordinate> setupTiles = context.game.getSetupTiles().get(context.game.currentPlayer);
            Unit aiUnit = null;

            for (int i = 0; i < setupTiles.size(); i++) {
                // Create a unit
                if (i == 0) {
                    aiUnit = new Horseman(new Coordinate(setupTiles.get(i).getX(), setupTiles.get(i).getY()), context.game.currentPlayer);
                } else if (i == 1) {
                    aiUnit = new Swordsman(new Coordinate(setupTiles.get(i).getX(), setupTiles.get(i).getY()), context.game.currentPlayer);
                } else if (i == 2) {
                    aiUnit = new Bowman(new Coordinate(setupTiles.get(i).getX(), setupTiles.get(i).getY()), context.game.currentPlayer);
                } else {
                    aiUnit = new Pikeman(new Coordinate(setupTiles.get(i).getX(), setupTiles.get(i).getY()), context.game.currentPlayer);
                }

                // Add the Horseman to the context.game
                context.game.AddActiveUnit(aiUnit);
            }

            context.game.currentPlayer = (context.game.currentPlayer + 1) % 2;
        }
    }

    // Draws unit select window
    public void drawUnitSelection(Canvas canvas) {
        float left = context.tileWidth * 5;
        float top = context.tileHeight * 5;
        float right = context.tileWidth * 17;
        float bottom = context.tileHeight * 10;

        context.paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, right, bottom, context.paint);

        Paint.Style style = context.paint.getStyle();
        context.paint.setStyle(Paint.Style.STROKE);
        context.paint.setColor(Color.BLACK);
        canvas.drawRect(left, top, right, bottom, context.paint);
        context.paint.setStyle(style);

        // Draw Headings
        context.paint.setColor(Color.BLACK);
        context.paint.setTextSize(36);
        canvas.drawText("Power Available: " + context.power, 0, 18, left + context.tileWidth, top + context.tileHeight, context.paint);

        canvas.drawText("Units: " + context.power, 0, 7, left + context.tileWidth, (7 * context.tileHeight) + (context.tileHeight / 2), context.paint);

        canvas.drawText("Power: " + context.power, 0, 7, left + context.tileWidth, (8 * context.tileHeight) + (context.tileHeight / 2), context.paint);

        canvas.drawText("Available: " + context.power, 0, 11, left + context.tileWidth, (9 * context.tileHeight) + (context.tileHeight / 2), context.paint);

        // Draw units

        canvas.drawBitmap(context.unitTypeToBitMap(Unit.UnitType.Bowman, (int)context.tileWidth, (int)context.tileHeight), 9 * context.tileWidth, 7 * context.tileHeight, null);
        context.paint.setColor(Color.BLACK);
        context.paint.setTextSize(36);
        String powerString = Integer.toString(Bowman.power);
        canvas.drawText(powerString, 0, powerString.length(), (int) (9 * context.tileWidth) + (context.tileWidth / 2), (int) (8 * context.tileHeight) + (context.tileHeight / 2), context.paint);
        powerString = Integer.toString(context.game.remainingUnits.get(Unit.UnitType.Bowman).get(context.game.currentPlayer));
        canvas.drawText(powerString, 0, powerString.length(), (int) (9 * context.tileWidth) + (context.tileWidth / 2), (int) (9 * context.tileHeight) + (context.tileHeight / 2), context.paint);


        canvas.drawBitmap(context.unitTypeToBitMap(Unit.UnitType.Swordsman, (int)context.tileWidth, (int)context.tileHeight), 10 * context.tileWidth, 7 * context.tileHeight, null);
        context.paint.setColor(Color.BLACK);
        context.paint.setTextSize(36);
        powerString = Integer.toString(Swordsman.power);
        canvas.drawText(powerString, 0, powerString.length(), (int) (10 * context.tileWidth) + (context.tileWidth / 2), (int) (8 * context.tileHeight) + (context.tileHeight / 2), context.paint);
        powerString = Integer.toString(context.game.remainingUnits.get(Unit.UnitType.Swordsman).get(context.game.currentPlayer));
        canvas.drawText(powerString, 0, powerString.length(), (int) (10 * context.tileWidth) + (context.tileWidth / 2), (int) (9 * context.tileHeight) + (context.tileHeight / 2), context.paint);


        canvas.drawBitmap(context.unitTypeToBitMap(Unit.UnitType.Pikeman, (int)context.tileWidth, (int)context.tileHeight), 11 * context.tileWidth, 7 * context.tileHeight, null);
        context.paint.setColor(Color.BLACK);
        context.paint.setTextSize(36);
        powerString = Integer.toString(Pikeman.power);
        canvas.drawText(powerString, 0, powerString.length(), (int) (11 * context.tileWidth) + (context.tileWidth / 2), (int) (8 * context.tileHeight) + (context.tileHeight / 2), context.paint);
        powerString = Integer.toString(context.game.remainingUnits.get(Unit.UnitType.Pikeman).get(context.game.currentPlayer));
        canvas.drawText(powerString, 0, powerString.length(), (int) (11 * context.tileWidth) + (context.tileWidth / 2), (int) (9 * context.tileHeight) + (context.tileHeight / 2), context.paint);


        canvas.drawBitmap(context.unitTypeToBitMap(Unit.UnitType.Horseman, (int)context.tileWidth, (int)context.tileHeight), 12 * context.tileWidth, 7 * context.tileHeight, null);
        context.paint.setColor(Color.BLACK);
        context.paint.setTextSize(36);
        powerString = Integer.toString(Horseman.power);
        canvas.drawText(powerString, 0, powerString.length(), (int) (12 * context.tileWidth) + (context.tileWidth / 2), (int) (8 * context.tileHeight) + (context.tileHeight / 2), context.paint);
        powerString = Integer.toString(context.game.remainingUnits.get(Unit.UnitType.Horseman).get(context.game.currentPlayer));
        canvas.drawText(powerString, 0, powerString.length(), (int) (12 * context.tileWidth) + (context.tileWidth / 2), (int) (9 * context.tileHeight) + (context.tileHeight / 2), context.paint);
    }
}
