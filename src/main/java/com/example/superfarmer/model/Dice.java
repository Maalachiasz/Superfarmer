package model;

import java.util.Random;

public class Dice {
    private static final Random rand = new Random();

    public static String rollTwoDice() {
        String[] animals = {"KRÓLIK", "OWCA", "ŚWINIA", "KROWA", "KOŃ", "LIS", "WILK"};
        String d1 = animals[rand.nextInt(animals.length)];
        String d2 = animals[rand.nextInt(animals.length)];
        return d1 + " + " + d2;
    }
}
