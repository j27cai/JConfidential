package com.example.jazzconfidential.jazzconfidential.Game;

/**
 * Created by simonvilleneuve on 15-06-07.
 */
public class Unit implements java.io.Serializable {
    public enum UnitType { Horseman, Pikeman, Swordsman, Bowman }

    UnitType unitType;
    int attack;
    int defense;
    int powerValue;
    int movementRange;
    int attackRange;
    Coordinate location;
    Coordinate targetLocation;
    boolean processed;
    int currentHealth;
    int maxHealth;
    int exp;
    int level;
    int alliance;

    public Unit() {

    }

    public Unit(UnitType unitType, int attack, int defense, int powerValue, int movementRange, int attackRange, Coordinate location, Coordinate targetLocation, boolean processed, int currentHealth, int maxHealth, int exp, int level, int alliance) {
        this.unitType = unitType;
        this.attack = attack;
        this.defense = defense;
        this.powerValue = powerValue;
        this.movementRange = movementRange;
        this.attackRange = attackRange;
        this.location = location;
        this.targetLocation = targetLocation;
        this.processed = processed;
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
        this.exp = exp;
        this.level = level;
        this.alliance = alliance;
    }

    public Coordinate getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Coordinate targetLocation) {
        this.targetLocation = targetLocation;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getPowerValue() {
        return powerValue;
    }

    public void setPowerValue(int powerValue) {
        this.powerValue = powerValue;
    }

    public int getMovementRange() {
        return movementRange;
    }

    public void setMovementRange(int movementRange) {
        this.movementRange = movementRange;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }

    public Coordinate getLocation() {
        return location;
    }

    public void setLocation(Coordinate location) {
        this.location = location;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int health) {
        this.currentHealth = health;
    }

    public int getMaxHealth() { return maxHealth; }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAlliance() {
        return alliance;
    }

    public void setAlliance(int alliance) {
        this.alliance = alliance;
    }
}
