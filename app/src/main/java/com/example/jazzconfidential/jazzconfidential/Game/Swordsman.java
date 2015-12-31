package com.example.jazzconfidential.jazzconfidential.Game;

/**
 * Created by simonvilleneuve on 15-06-07.
 */
public class Swordsman extends Unit {
    public static int power = 1;

    public Swordsman() {

    }

    public Swordsman(Coordinate location, int alliance) {
        super(Unit.UnitType.Swordsman,  // Unit type
                100, // attack
                2, // defense
                1, // powerValue
                10, // movementRange
                2, // attackRange
                location, // location on map
                location, // target location
                false, //processed
                8, // currentHealth
                8, // maxHealth
                0, // exp
                0, // level
                alliance
        );
    }
}
