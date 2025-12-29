package de.emn4tor.farm;

public class FarmTile {

    private final int gridX;
    private final int gridY;

    private TillState tillState;
    private boolean watered;
    private PlantType plantType;
    private int growthStage;

    public enum TillState {
        UNTILLED,
        TILLED
    }

    public enum PlantType {
        NONE,
        WHEAT,
        CARROT,
        POTATO
        // Add more plant types as needed
    }

    public FarmTile(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.tillState = TillState.UNTILLED;
        this.watered = false;
        this.plantType = PlantType.NONE;
        this.growthStage = 0;
    }

    // Getters
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public TillState getTillState() { return tillState; }
    public boolean isWatered() { return watered; }
    public PlantType getPlantType() { return plantType; }
    public int getGrowthStage() { return growthStage; }

    // State management
    public void till() {
        if (tillState == TillState.UNTILLED) {
            tillState = TillState.TILLED;
        }
    }

    public void water() {
        if (tillState == TillState.TILLED) {
            watered = true;
        }
    }

    public void dry() {
        watered = false;
    }

    public void plant(PlantType type) {
        if (tillState == TillState.TILLED && plantType == PlantType.NONE) {
            plantType = type;
            growthStage = 0;
        }
    }

    public void grow() {
        if (plantType != PlantType.NONE && growthStage < getMaxGrowthStage()) {
            growthStage++;
        }
    }

    public void harvest() {
        plantType = PlantType.NONE;
        growthStage = 0;
    }

    public boolean canHarvest() {
        return plantType != PlantType.NONE && growthStage >= getMaxGrowthStage();
    }

    private int getMaxGrowthStage() {
        switch (plantType) {
            case WHEAT: return 4;
            case CARROT: return 3;
            case POTATO: return 3;
            default: return 0;
        }
    }

    public void reset() {
        tillState = TillState.UNTILLED;
        watered = false;
        plantType = PlantType.NONE;
        growthStage = 0;
    }
}