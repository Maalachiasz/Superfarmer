package model;

import java.util.HashMap;
import java.util.Map;

public class Bank {
    private Map<AnimalType, Integer> stock = new HashMap<>();

    public Bank() {
        stock.put(AnimalType.KROLIK, 60);
        stock.put(AnimalType.OWCA, 24);
        stock.put(AnimalType.SWINIA, 20);
        stock.put(AnimalType.KROWA, 12);
        stock.put(AnimalType.KON, 6);
        stock.put(AnimalType.MALY_PIES, 4);
        stock.put(AnimalType.DUZY_PIES, 2);
    }

    public boolean exchange(Player player, AnimalType give, AnimalType get, int amount) {
        if (tryDirectExchangeWithCorrectAmounts(player, give, get, amount)) {
            return true;
        }
        return tryProgressiveExchange(player, give, get, amount);
    }

    private boolean tryProgressiveExchange(Player player, AnimalType give, AnimalType get, int amount) {
        int totalGiveNeeded = calculateProgressiveRate(give, get, amount);
        if (totalGiveNeeded == 0) return false;

        if (player.getAnimalCount(give) < totalGiveNeeded) return false;

        if (stock.getOrDefault(get, 0) < amount) return false;

        player.removeAnimal(give, totalGiveNeeded);
        player.addAnimal(get, amount);
        stock.put(give, stock.getOrDefault(give, 0) + totalGiveNeeded);
        stock.put(get, stock.get(get) - amount);
        return true;
    }

    private int getDirectRate(AnimalType give, AnimalType get) {
        if (give == AnimalType.KROLIK && get == AnimalType.OWCA) return 6;
        if (give == AnimalType.OWCA && get == AnimalType.SWINIA) return 2;
        if (give == AnimalType.SWINIA && get == AnimalType.KROWA) return 3;
        if (give == AnimalType.KROWA && get == AnimalType.KON) return 2;
        if (give == AnimalType.OWCA && get == AnimalType.MALY_PIES) return 1;
        if (give == AnimalType.KROWA && get == AnimalType.DUZY_PIES) return 1;

        if (give == AnimalType.OWCA && get == AnimalType.KROLIK) return 1; // 1 OWCA = 6 KROLIK
        if (give == AnimalType.SWINIA && get == AnimalType.OWCA) return 1; // 1 SWINIA = 2 OWCA
        if (give == AnimalType.KROWA && get == AnimalType.SWINIA) return 1; // 1 KROWA = 3 SWINIA
        if (give == AnimalType.KON && get == AnimalType.KROWA) return 1; // 1 KON = 2 KROWA
        if (give == AnimalType.MALY_PIES && get == AnimalType.OWCA) return 1; // 1 MALY_PIES = 1 OWCA
        if (give == AnimalType.DUZY_PIES && get == AnimalType.KROWA) return 1; // 1 DUZY_PIES = 1 KROWA

        return 0;
    }

    private int getReceiveAmount(AnimalType give, AnimalType get, int giveAmount) {
        if (give == AnimalType.OWCA && get == AnimalType.KROLIK) return giveAmount * 6;
        if (give == AnimalType.SWINIA && get == AnimalType.OWCA) return giveAmount * 2;
        if (give == AnimalType.KROWA && get == AnimalType.SWINIA) return giveAmount * 3;
        if (give == AnimalType.KON && get == AnimalType.KROWA) return giveAmount * 2;
        if (give == AnimalType.MALY_PIES && get == AnimalType.OWCA) return giveAmount * 1;
        if (give == AnimalType.DUZY_PIES && get == AnimalType.KROWA) return giveAmount * 1;

        return giveAmount;
    }

    private int calculateProgressiveRate(AnimalType give, AnimalType get, int amount) {
        int giveLevel = getAnimalLevel(give);
        int getLevel = getAnimalLevel(get);

        //if (giveLevel >= getLevel) return 0;

        int totalNeeded = amount;

        for (int level = getLevel - 1; level >= giveLevel; level--) {
            AnimalType currentType = getAnimalByLevel(level);
            AnimalType nextType = getAnimalByLevel(level + 1);

            int rate = getDirectRate(currentType, nextType);
            if (rate == 0) return 0;

            totalNeeded *= rate;
        }

        return totalNeeded;
    }

    private int getAnimalLevel(AnimalType type) {
        switch (type) {
            case KROLIK: return 1;
            case OWCA: return 2;
            case SWINIA: return 3;
            case KROWA: return 4;
            case KON: return 5;
            default: return 0;
        }
    }

    private AnimalType getAnimalByLevel(int level) {
        switch (level) {
            case 1: return AnimalType.KROLIK;
            case 2: return AnimalType.OWCA;
            case 3: return AnimalType.SWINIA;
            case 4: return AnimalType.KROWA;
            case 5: return AnimalType.KON;
            default: return null;
        }
    }

    private boolean tryDirectExchangeWithCorrectAmounts(Player player, AnimalType give, AnimalType get, int amount) {
        int rate = getDirectRate(give, get);
        if (rate == 0) return false;

        int giveAmount, getAmount;

        if (isForwardExchange(give, get)) {
            giveAmount = rate * amount;
            getAmount = amount;
        } else {
            giveAmount = amount;
            getAmount = getReceiveAmount(give, get, amount);
        }

        if (player.getAnimalCount(give) < giveAmount) return false;

        if (stock.getOrDefault(get, 0) < getAmount) return false;

        player.removeAnimal(give, giveAmount);
        player.addAnimal(get, getAmount);
        stock.put(give, stock.getOrDefault(give, 0) + giveAmount);
        stock.put(get, stock.get(get) - getAmount);
        return true;
    }

    private boolean isForwardExchange(AnimalType give, AnimalType get) {
        return (give == AnimalType.KROLIK && get == AnimalType.OWCA) ||
                (give == AnimalType.OWCA && get == AnimalType.SWINIA) ||
                (give == AnimalType.SWINIA && get == AnimalType.KROWA) ||
                (give == AnimalType.KROWA && get == AnimalType.KON) ||
                (give == AnimalType.OWCA && get == AnimalType.MALY_PIES) ||
                (give == AnimalType.KROWA && get == AnimalType.DUZY_PIES);
    }

    public boolean removeAnimal(AnimalType type, int qty) {
        int current = stock.getOrDefault(type, 0);
        if (current >= qty) {
            stock.put(type, current - qty);
            return true;
        }
        return false;
    }

    public void addAnimal(AnimalType type, int qty) {
        stock.put(type, stock.getOrDefault(type, 0) + qty);
    }

    public int getAnimalCount(AnimalType type) {
        return stock.getOrDefault(type, 0);
    }

    public Map<AnimalType, Integer> getResources() {
        return stock;
    }
}