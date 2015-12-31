package com.example.jazzconfidential.jazzconfidential;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.example.jazzconfidential.jazzconfidential.Game.Coordinate;
import com.example.jazzconfidential.jazzconfidential.Game.Game;
import com.example.jazzconfidential.jazzconfidential.Game.Unit;
import com.example.jazzconfidential.jazzconfidential.Stages.AttackStage;
import com.example.jazzconfidential.jazzconfidential.Stages.EndStage;
import com.example.jazzconfidential.jazzconfidential.Stages.GameStage;
import com.example.jazzconfidential.jazzconfidential.Stages.MovementStage;
import com.example.jazzconfidential.jazzconfidential.Stages.UnitSelectionStage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by simonvilleneuve on 15-07-04.
 */
public class UnitView extends View implements View.OnTouchListener {
    public Rect rect;

    public boolean dragged = false;
    private float displayWidth;
    private float displayHeight;

    //These two constants specify the minimum and maximum zoom
    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

    public float scaleFactor = 1.f;
    private ScaleGestureDetector detector;

    //These constants specify the mode that we're in
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;

    public int mode;

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;

    private GameStage gameStage;

    public Paint paint;
    public Game game;

    // Total power for each player
    public int power;

    public float tileWidth = 0, tileHeight = 0;

    public int selectX = 0, selectY = 0;

    public boolean processingUnit = false;
    public Unit selectedUnit = null;

    public GameActivity gameActivity;

    public UnitView(Context context, Game game, GameActivity gameActivity) {
        super(context);

        detector = new ScaleGestureDetector(getContext(), new ScaleListener());

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        displayWidth = display.getWidth();
        displayHeight = display.getHeight();

        paint = new Paint();
        this.game = game;

        this.power = game.getPowah();

        // Fill game.remainingUnits
        game.remainingUnits = new HashMap<>();
        List<Unit> units = this.game.getAvailableUnits();
        for (Unit u: units) {
            if (game.remainingUnits.containsKey(u.getUnitType())) {
                if (game.remainingUnits.get(u.getUnitType()).containsKey(u.getAlliance())) {
                    game.remainingUnits.get(u.getUnitType()).put(u.getAlliance(), game.remainingUnits.get(u.getUnitType()).get(u.getAlliance()) + 1);
                }
                else {
                    game.remainingUnits.get(u.getUnitType()).put(u.getAlliance(), 1);
                }
            } else {
                HashMap<Integer, Integer> allianceMap = new HashMap<>();
                allianceMap.put(u.getAlliance(), 1);

                game.remainingUnits.put(u.getUnitType(), allianceMap);
            }
        }

        setOnTouchListener(this);

        if (game.stage == Game.Stage.UnitSelection) {
            gameStage = new UnitSelectionStage(this);
        }
        else if (game.stage == Game.Stage.Movement) {
            gameStage = new MovementStage(this);
        }
        else if (game.stage == Game.Stage.Attack) {
            gameStage = new AttackStage(this);
        }
        else {
            gameStage = new EndStage(this);
        }

        this.gameActivity = gameActivity;
    }

    public void setGameStage(GameStage gameStage) {
        this.gameStage = gameStage;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        //We're going to scale the X and Y coordinates by the same amount
        canvas.scale(scaleFactor, scaleFactor);

        //If translateX times -1 is lesser than zero, let's set it to zero. This takes care of the left bound
        if((translateX * -1) < 0) {
            translateX = 0;
        }

        //This is where we take care of the right bound. We compare translateX times -1 to (scaleFactor - 1) * displayWidth.
        //If translateX is greater than that value, then we know that we've gone over the bound. So we set the value of
        //translateX to (1 - scaleFactor) times the display width. Notice that the terms are interchanged; it's the same
        //as doing -1 * (scaleFactor - 1) * displayWidth
        else if((translateX * -1) > (scaleFactor - 1) * displayWidth) {
            translateX = (1 - scaleFactor) * displayWidth;
        }

        if(translateY * -1 < 0) {
            translateY = 0;
        }

        //We do the exact same thing for the bottom bound, except in this case we use the height of the display
        else if((translateY * -1) > (scaleFactor - 1) * displayHeight) {
            translateY = (1 - scaleFactor) * displayHeight;
        }

        //We need to divide by the scale factor here, otherwise we end up with excessive panning based on our zoom level
        //because the translation amount also gets scaled according to how much we've zoomed into the canvas.
        canvas.translate(translateX / scaleFactor, translateY / scaleFactor);

        /* The rest of your canvas-drawing code */
        // Draw the map
        int[][] map = game.getMap();
        Bitmap bm = null;
        for (int row = 0; row < 14; row++) {
            for (int col = 0; col < 22; col++) {
                // Set color
                if (map[row][col] == 0) {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_wtr);
                }
                else if (map[row][col] == 2) {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_grs);
                }
                else if (map[row][col] == 3) {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_mtn);
                }
                bm = Bitmap.createScaledBitmap(bm, (int)tileWidth, (int)tileHeight, false);
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
                canvas.drawRect(left, top, right, bottom, paint);
                paint.setStyle(style);
            }
        }

        // Draw the capture points
        List<Coordinate> capturePoints = game.getCapturePoints();
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_cpt);
        bm = Bitmap.createScaledBitmap(bm, (int) tileWidth, (int) tileHeight, false);
        for (int i = 0; i < capturePoints.size(); i++) {
            // Calculate left/top/right/bottom
            float left = (capturePoints.get(i).getX() * tileWidth);
            float top = (capturePoints.get(i).getY() * tileHeight);

            // Draw capture point
            paint.setColor(Color.BLACK);
            canvas.drawBitmap(bm, left, top, null);
        }

        gameStage.onDraw(canvas);

        rect = canvas.getClipBounds();

        canvas.restore();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;

                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                //amount for each coordinates This works even when we are translating the first time because the initial
                //values for these two variables is zero.
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;
                break;

            case MotionEvent.ACTION_MOVE:
                translateX = event.getX() - startX;
                translateY = event.getY() - startY;

                //We cannot use startX and startY directly because we have adjusted their values using the previous translation values.
                //This is why we need to add those values to startX and startY so that we can get the actual coordinates of the finger.
                double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +
                                Math.pow(event.getY() - (startY + previousTranslateY), 2)
                );

                if(distance > 0) {
                    dragged = true;
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_UP:
                mode = NONE;
                dragged = false;

                //All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
                //previousTranslate
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = DRAG;

                //This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX
                //and previousTranslateY when the second finger goes up
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
        }

        detector.onTouchEvent(event);

        gameStage.onTouch(v, event);
        invalidate();

        //We redraw the canvas only in the following cases:
        //
        // o The mode is ZOOM
        //        OR
        // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is
        //   set to true (meaning the finger has actually moved)
        /*if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM) {
            invalidate();
        }*/

        return true;
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

    public void remove_dead_unit() {
        List<Unit> units = game.getUnits();
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getCurrentHealth() <= 0) {
                units.remove(i);
                i--;
            }
        }
        game.setUnits(units);
    }

    public Coordinate getEmptyLocation(Unit u, Coordinate location) {
        if (getUnitAt(location.getX(), location.getY(), 0) == null) {
            return location;
        }
        Coordinate current = u.getLocation();
        Coordinate revised = null;
        if (location.getX() - current.getX() > 0) {
            revised = new Coordinate(location.getX() - 1, location.getY());
        }else if (location.getX() - current.getX() < 0) {
            revised = new Coordinate(location.getX() + 1, location.getY());
        }else if (location.getY() - current.getY() > 0) {
            revised = new Coordinate(location.getX(), location.getY() - 1);
        }else if (location.getY() - current.getY() < 0) {
            revised = new Coordinate(location.getX(), location.getY() + 1);
        } else {
            return location;
        }
        return getEmptyLocation(u, revised);
    }

    public boolean canTarget(List<Coordinate> tiles, int xCoord, int yCoord) {
        for ( Coordinate c : tiles ) {
            if (c.getX() == xCoord && c.getY() == yCoord) {
                return true;
            }
        }
        return false;
    }

    //type 0 = movement, type 1 = attack range
    public List<Coordinate> generateTile (Unit u, int type) {
        int range = 0;
        int[][] map = game.getMap();
        Coordinate unitCoord = u.getLocation();
        List<Coordinate> result = new ArrayList<>();

        if (type == 0) { range = u.getMovementRange(); }
        else if (type == 1) { range = u.getAttackRange(); }

        for (int i = 1; i <= range; i ++){
            for (int j = 0; j <= range -i; j++) {
                int targetX = unitCoord.getX() + i;
                int targetY = unitCoord.getY() + j;
                if (targetX < 0 || targetX >= map[0].length || targetY < 0 || targetY >= map.length) { continue; }
                if (map[targetY][targetX] != 0 && map[targetY][targetX] != 3) {
                    Coordinate newCoord = new Coordinate(targetX, targetY);
                    result.add(newCoord);
                }
            }
        }
        for (int i = 1; i <= range; i ++){
            for (int j = 0; j <= range -i; j++) {
                int targetX = unitCoord.getX() - j;
                int targetY = unitCoord.getY() + i;
                if (targetX < 0 || targetX >= map[0].length || targetY < 0 || targetY >= map.length) { continue; }
                if (map[targetY][targetX] != 0 && map[targetY][targetX] != 3) {
                    Coordinate newCoord = new Coordinate(targetX, targetY);
                    result.add(newCoord);
                }
            }
        }
        for (int i = 1; i <= range; i ++){
            for (int j = 0; j <= range -i; j++) {
                int targetX = unitCoord.getX() - i;
                int targetY = unitCoord.getY() - j;
                if (targetX < 0 || targetX >= map[0].length || targetY < 0 || targetY >= map.length) { continue; }
                if (map[targetY][targetX] != 0 && map[targetY][targetX] != 3) {
                    Coordinate newCoord = new Coordinate(targetX, targetY);
                    result.add(newCoord);
                }
            }
        }
        for (int i = 1; i <= range; i ++){
            for (int j = 0; j <= range -i; j++) {
                int targetX = unitCoord.getX() + j;
                int targetY = unitCoord.getY() - i;
                if (targetX < 0 || targetX >= map[0].length || targetY < 0 || targetY >= map.length) { continue; }
                if (map[targetY][targetX] != 0 && map[targetY][targetX] != 3) {
                    Coordinate newCoord = new Coordinate(targetX, targetY);
                    result.add(newCoord);
                }
            }
        }
        return result;
    }

    // checkAlliance : 0 - no check, 1 - same, 2 - different
    public Unit getUnitAt(int xCoord, int yCoord, int checkAlliance) {
        for ( Unit u : game.getUnits() ) {
            if (u.getLocation().getX() == xCoord && u.getLocation().getY() == yCoord &&
                    (checkAlliance == 0 || (checkAlliance == 1 && u.getAlliance() == game.currentPlayer) ||
                    (checkAlliance == 2 && u.getAlliance() != game.currentPlayer))) {
                return u;
            }
        }
        return null;
    }

    public Bitmap unitTypeToBitMap(Unit.UnitType unitType, int tileWidth, int tileHeight) {
        Bitmap bm = null;

        switch (unitType) {
            case Swordsman:
                if (MainMenuActivity.swordsman != null) {
                    bm = MainMenuActivity.swordsman;
                } else {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_swd);
                }
                break;
            case Bowman:
                if (MainMenuActivity.archer != null) {
                    bm = MainMenuActivity.archer;
                } else {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_ach);
                }
                break;
            case Horseman:
                if (MainMenuActivity.horseman != null) {
                    bm = MainMenuActivity.horseman;
                } else {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_cav);
                }
                break;
            case Pikeman:
                if (MainMenuActivity.pikeman != null) {
                    bm = MainMenuActivity.pikeman;
                } else {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.jc_pkm);
                }
                break;
        }
        bm = Bitmap.createScaledBitmap(bm,tileWidth,tileHeight, false);
        return bm;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            return true;
        }
    }
}
