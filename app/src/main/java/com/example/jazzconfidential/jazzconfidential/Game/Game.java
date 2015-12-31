package com.example.jazzconfidential.jazzconfidential.Game;

import com.example.jazzconfidential.jazzconfidential.LoginFragment;
import com.example.jazzconfidential.jazzconfidential.MainMenuActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by simonvilleneuve on 15-06-07.
 */
public class Game implements java.io.Serializable {

    public String createdBy = "";
    public String playedWith = "";

    public String createdByUserName = "";
    public String playedWithUserName = "";

    public String gameId = "";

    // Indicates what kind of game is being played
    public enum GameType { SinglePlayer, HotSeat, Multiplayer }

    // Indicates which of three possible stages game is in
    public enum Stage { UnitSelection, Movement, Attack, End }

    private int[][] map;
    private List<Unit> activeUnits;
    private List<Unit> availableUnits;
    private List<Coordinate> capturePoints; // 0 - player 1, 1 - player 2
    private HashMap<Coordinate, Integer> capturePointsTurns;
    private HashMap<Integer, List<Coordinate>> setupTiles;
    private List<Coordinate> rangeTiles;
    private int powah;

    // Indicates which player is playing
    public int currentPlayer;

    //Indicates who won (-1, no winner yet; 0, player 1; 1, player 2; 2, tie
    public int winner;

    // HashMap sorted by UnitType, then Alliance, indicating what units are available to each player
    public HashMap<Unit.UnitType, HashMap<Integer, Integer>> remainingUnits;

    // Start in UnitSelection stage
    public Stage stage = Game.Stage.UnitSelection;

    public GameType gameType;

    // Prototype constructor
    public Game(GameType gameType, String createdBy, String playedWith,String createdByUserName, String playedWithUserName ) {
        // Set game type
        this.gameType = gameType;
        this.createdBy = createdBy;
        this.createdByUserName = createdByUserName;
        this.playedWithUserName = playedWithUserName;
        this.playedWith = playedWith;

        // Create map
        map = new int[14][22];
        for (int row = 0; row < 14; row++) {
            for (int col = 0; col < 22; col++) {
                if (row == 0 || row == 13 || col == 0 || col == 21) { // Add water
                    map[row][col] = 0;
                }
                else if ((row == 4 && col == 7) || (row == 7 && col == 4)) { // Add mountain
                    map[row][col] = 3;
                }
                else { // Add land
                    map[row][col] = 2;
                }
            }
        }

        // Create active units
        activeUnits = new ArrayList<>();

        // Create available units
        availableUnits = new ArrayList<>();
        availableUnits.add(new Bowman(null, 0));
        availableUnits.add(new Bowman(null, 0));
        availableUnits.add(new Pikeman(null, 0));
        availableUnits.add(new Pikeman(null, 0));
        availableUnits.add(new Horseman(null, 0));
        availableUnits.add(new Horseman(null, 0));
        availableUnits.add(new Swordsman(null, 0));
        availableUnits.add(new Swordsman(null, 0));

        availableUnits.add(new Bowman(null, 1));
        availableUnits.add(new Bowman(null, 1));
        availableUnits.add(new Pikeman(null, 1));
        availableUnits.add(new Pikeman(null, 1));
        availableUnits.add(new Horseman(null, 1));
        availableUnits.add(new Horseman(null, 1));
        availableUnits.add(new Swordsman(null, 1));
        availableUnits.add(new Swordsman(null, 1));

        // Create capture points
        capturePoints = new ArrayList<>();
        capturePoints.add(new Coordinate(1, 1));
        capturePoints.add(new Coordinate(20, 12)); // col, row

        // Create capture point couters
        capturePointsTurns = new HashMap<>();
        for (int i = 0; i < capturePoints.size(); i++) {
            capturePointsTurns.put(capturePoints.get(i), 0);
        }

        // Create setup tiles
        setupTiles = new HashMap<>();
        List<Coordinate> playerOneSetup = new ArrayList<>();
        playerOneSetup.add(new Coordinate(1, 2));
        playerOneSetup.add(new Coordinate(2, 1));
        playerOneSetup.add(new Coordinate(2, 2));
        playerOneSetup.add(new Coordinate(1, 3));
        setupTiles.put(0, playerOneSetup);

        List<Coordinate> playerTwoSetup = new ArrayList<>();
        playerTwoSetup.add(new Coordinate(20, 10));
        playerTwoSetup.add(new Coordinate(19, 11));
        playerTwoSetup.add(new Coordinate(19, 12));
        playerTwoSetup.add(new Coordinate(20, 11));
        setupTiles.put(1, playerTwoSetup);

        // Set limit on power
        powah = 4;

        currentPlayer = 0;

        winner = -1;
    }

    public Game(int[][] map, List<Unit> units, List<Coordinate> capturePoints) {
        this.map = map;
        this.activeUnits = units;
        this.capturePoints = capturePoints;
    }

    public Game(int[][] map, List<Unit> activeUnits, List<Unit> availableUnits,
                List<Coordinate> capturePoints,
                HashMap<Integer, List<Coordinate>> setupTiles, List<Coordinate> rangeTiles,
                int powah, int currentPlayer, int winner,
                HashMap<Unit.UnitType, HashMap<Integer, Integer>> remainingUnits, Stage stage,
                GameType gameType) {
        this.map = map;
        this.activeUnits = activeUnits;
        this.availableUnits = availableUnits;
        this.capturePoints = capturePoints;
        this.setupTiles = setupTiles;
        this.rangeTiles = rangeTiles;
        this.powah = powah;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
        this.remainingUnits = remainingUnits;
        this.stage = stage;
        this.gameType = gameType;
    }

    public boolean capturePointOccupied(Coordinate capturePoint) {
        capturePointsTurns.put(capturePoint, capturePointsTurns.get(capturePoint) + 1);

        // If someone has been sitting on a capture point for 5 turns, then
        if (capturePointsTurns.get(capturePoint) == 5) {
            return true;
        }

        return false;
    }

    public void capturePointNotOccupied(Coordinate capturePoint) {
        capturePointsTurns.put(capturePoint, 0);
    }

    public List<Unit> getAvailableUnits() {
        return availableUnits;
    }

    public void AddActiveUnit(Unit unit) {
        activeUnits.add(unit);
    }

    public int[][] getMap() {
        return map;
    }

    public List<Unit> getUnits() { return activeUnits; }

    public void setUnits(List<Unit> units) { this.activeUnits = units; }

    public List<Coordinate> getCapturePoints() { return capturePoints; }

    public int getPowah() { return powah; }

    public HashMap<Integer, List<Coordinate>> getSetupTiles() { return setupTiles; }

    public List<Coordinate> getRangeTiles() {
        return rangeTiles;
    }

    public void setRangeTiles(List<Coordinate> rangeTiles) {
        this.rangeTiles = rangeTiles;
    }
}
