package com.example.jazzconfidential.jazzconfidential.Game;

/**
 * Created by simonvilleneuve on 15-06-07.
 */
public class Bowman extends Unit {
    public static int power = 1;

    public Bowman() {

    }

    public Bowman(Coordinate location, int alliance) {
        super(UnitType.Bowman,  // Unit type
                300, // attack
                0, // defense
                1, // powerValue
                2, // movementRange
                6, // attackRange
                location, // location on map
                location, // target location
                false, //processed
                6, // currentHealth
                6, // maxHealth
                0, // exp
                0, // level
                alliance
        );
    }
}
