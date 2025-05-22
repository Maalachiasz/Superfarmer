package model;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private String name;
    private Map<AnimalType, Integer> resources = new HashMap<>();

    public Player(String name) {
        this.name = name;
        for (AnimalType type : AnimalType.values()) {
            resources.put(type, 0);
        }
        resources.put(AnimalType.KROLIK, 1); // start z 1 królikiem
    }

    public boolean removeAnimal(AnimalType type, int qty) {
        int current = resources.getOrDefault(type, 0);
        if (current >= qty) {
            resources.put(type, current - qty);
            return true;
        }
        return false;
    }

    public void addAnimal(AnimalType type, int qty) {
        resources.put(type, resources.getOrDefault(type, 0) + qty);
    }

    public Map<AnimalType, Integer> getResources() {
        return resources;
    }

    public String getName() {
        return name;
    }
    public boolean hasSmallDog() {
        return resources.getOrDefault(AnimalType.MALY_PIES, 0) > 0;
    }

    public boolean hasBigDog() {
        return resources.getOrDefault(AnimalType.DUZY_PIES, 0) > 0;
    }

    public void useSmallDog() {
        removeAnimal(AnimalType.MALY_PIES, 1);
    }

    public void useBigDog() {
        removeAnimal(AnimalType.DUZY_PIES, 1);
    }

    public int getAnimalCount(AnimalType type) {
        return resources.getOrDefault(type, 0);
    }

    public void setAnimalCount(AnimalType type, int count) {
        resources.put(type, count);
    }

}
