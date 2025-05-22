package model;

import java.util.Random;

public class Dice {
    private static final String[] DIE_1 = {
            "KROLIK", "KROLIK", "KROLIK", "KROLIK", "KROLIK", "KROLIK",
            "OWCA", "OWCA", "OWCA",
            "SWINIA",
            "KROWA",
            "WILK"
    };

    private static final String[] DIE_2 = {
            "KROLIK", "KROLIK", "KROLIK", "KROLIK", "KROLIK", "KROLIK",
            "OWCA", "OWCA",
            "SWINIA", "SWINIA",
            "KON",
            "LIS"
    };

    private static final Random random = new Random();

    public static String[] rollTwoDice() {
        return new String[] {
                DIE_1[random.nextInt(DIE_1.length)],
                DIE_2[random.nextInt(DIE_2.length)]
        };
    }
}
