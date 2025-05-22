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
    }

    public boolean exchange(Player player, AnimalType give, AnimalType get, int amount) {
        // uproszczona tabela wymian
        int rate = getRate(give, get);
        if (rate == 0) return false;
        int cost = rate * amount;

        if (player.removeAnimal(give, cost) && stock.getOrDefault(get, 0) >= amount) {
            player.addAnimal(get, amount);
            stock.put(give, stock.get(give) + cost);
            stock.put(get, stock.get(get) - amount);
            return true;
        } else {
            return false;
        }
    }

    private int getRate(AnimalType give, AnimalType get) {
        if (give == AnimalType.KROLIK && get == AnimalType.OWCA) return 6;
        if (give == AnimalType.OWCA && get == AnimalType.SWINIA) return 2;
        if (give == AnimalType.SWINIA && get == AnimalType.KROWA) return 3;
        if (give == AnimalType.KROWA && get == AnimalType.KON) return 2;
        return 0;
    }

    public Map<AnimalType, Integer> getResources() {
        return stock;
    }
}
