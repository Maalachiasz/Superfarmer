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
        // Try direct exchange first
        if (tryDirectExchangeWithCorrectAmounts(player, give, get, amount)) {
            return true;
        }

        // Try progressive exchange
        return tryProgressiveExchange(player, give, get, amount);
    }

    private boolean tryProgressiveExchange(Player player, AnimalType give, AnimalType get, int amount) {
        // Calculate how many of 'give' animals we need to get 'amount' of 'get' animals
        int totalGiveNeeded = calculateProgressiveRate(give, get, amount);
        if (totalGiveNeeded == 0) return false;

        // Check if player has enough animals
        if (player.getAnimalCount(give) < totalGiveNeeded) return false;

        // Check if bank has enough target animals
        if (stock.getOrDefault(get, 0) < amount) return false;

        // Perform the exchange
        player.removeAnimal(give, totalGiveNeeded);
        player.addAnimal(get, amount);
        stock.put(give, stock.getOrDefault(give, 0) + totalGiveNeeded);
        stock.put(get, stock.get(get) - amount);
        return true;
    }

    private int getDirectRate(AnimalType give, AnimalType get) {
        // Forward exchanges
        if (give == AnimalType.KROLIK && get == AnimalType.OWCA) return 6;
        if (give == AnimalType.OWCA && get == AnimalType.SWINIA) return 2;
        if (give == AnimalType.SWINIA && get == AnimalType.KROWA) return 3;
        if (give == AnimalType.KROWA && get == AnimalType.KON) return 2;
        if (give == AnimalType.OWCA && get == AnimalType.MALY_PIES) return 1;
        if (give == AnimalType.KROWA && get == AnimalType.DUZY_PIES) return 1;

        // Reverse exchanges
        if (give == AnimalType.OWCA && get == AnimalType.KROLIK) return 1; // 1 OWCA = 6 KROLIK
        if (give == AnimalType.SWINIA && get == AnimalType.OWCA) return 1; // 1 SWINIA = 2 OWCA
        if (give == AnimalType.KROWA && get == AnimalType.SWINIA) return 1; // 1 KROWA = 3 SWINIA
        if (give == AnimalType.KON && get == AnimalType.KROWA) return 1; // 1 KON = 2 KROWA
        if (give == AnimalType.MALY_PIES && get == AnimalType.OWCA) return 1; // 1 MALY_PIES = 1 OWCA
        if (give == AnimalType.DUZY_PIES && get == AnimalType.KROWA) return 1; // 1 DUZY_PIES = 1 KROWA

        return 0;
    }

    private int getReceiveAmount(AnimalType give, AnimalType get, int giveAmount) {
        // For reverse exchanges, calculate how many we get
        if (give == AnimalType.OWCA && get == AnimalType.KROLIK) return giveAmount * 6;
        if (give == AnimalType.SWINIA && get == AnimalType.OWCA) return giveAmount * 2;
        if (give == AnimalType.KROWA && get == AnimalType.SWINIA) return giveAmount * 3;
        if (give == AnimalType.KON && get == AnimalType.KROWA) return giveAmount * 2;
        if (give == AnimalType.MALY_PIES && get == AnimalType.OWCA) return giveAmount * 1;
        if (give == AnimalType.DUZY_PIES && get == AnimalType.KROWA) return giveAmount * 1;

        return giveAmount; // For forward exchanges, it's 1:1 after applying the rate
    }

    private int calculateProgressiveRate(AnimalType give, AnimalType get, int amount) {
        // Define animal hierarchy: KROLIK < OWCA < SWINIA < KROWA < KON
        int giveLevel = getAnimalLevel(give);
        int getLevel = getAnimalLevel(get);

        if (giveLevel >= getLevel) return 0; // Can't exchange higher/equal level for lower

        int totalNeeded = amount;

        // Calculate progressive rate by going up the chain
        for (int level = getLevel - 1; level >= giveLevel; level--) {
            AnimalType currentType = getAnimalByLevel(level);
            AnimalType nextType = getAnimalByLevel(level + 1);

            int rate = getDirectRate(currentType, nextType);
            if (rate == 0) return 0; // Invalid chain

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
            default: return 0; // Dogs are not in the hierarchy
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

    // Override the exchange method to handle reverse exchanges properly
    private boolean tryDirectExchangeWithCorrectAmounts(Player player, AnimalType give, AnimalType get, int amount) {
        int rate = getDirectRate(give, get);
        if (rate == 0) return false;

        int giveAmount, getAmount;

        // For forward exchanges
        if (isForwardExchange(give, get)) {
            giveAmount = rate * amount;
            getAmount = amount;
        } else {
            // For reverse exchanges
            giveAmount = amount;
            getAmount = getReceiveAmount(give, get, amount);
        }

        // Check if player has enough animals
        if (player.getAnimalCount(give) < giveAmount) return false;

        // Check if bank has enough animals
        if (stock.getOrDefault(get, 0) < getAmount) return false;

        // Perform exchange
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