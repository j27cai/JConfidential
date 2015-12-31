package com.example.jazzconfidential.jazzconfidential.Game;

/**
 * Created by simonvilleneuve on 15-06-07.
 */
public class Pikeman extends Unit {
    public static int power = 1;

    public Pikeman() {

    }

    public Pikeman(Coordinate location, int alliance) {
        super(UnitType.Pikeman,  // Unit type
                200, // attack
                1, // defense
                1, // powerValue
                3, // movementRange
                4, // attackRange
                location, // location on map
                location, // target location
                false, //processed
                7, // currentHealth
                7, // maxHealth
                0, // exp
                0, // level
                alliance
        );
    }
}
