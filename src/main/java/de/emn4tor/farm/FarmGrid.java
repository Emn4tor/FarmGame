package de.emn4tor.farm;

import java.util.HashMap;
import java.util.Map;

public class FarmGrid {

    private final int width;
    private final int height;
    private final float tileSize;
    private final Map<String, FarmTile> tiles;

    public FarmGrid(int width, int height, float tileSize) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.tiles = new HashMap<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles.put(key(x, y), new FarmTile(x, y));
            }
        }
    }

    // Convert grid coords -> world coords
    public float gridToWorldX(int gridX) { return gridX * tileSize; }
    public float gridToWorldY(int gridY) { return gridY * tileSize; }

    // Convert world coords -> grid coords
    public int worldToGridX(float worldX) { return (int)(worldX / tileSize); }
    public int worldToGridY(float worldY) { return (int)(worldY / tileSize); }

    // Tile operations
    public void tillTile(int x, int y) {
        FarmTile tile = getTile(x, y);
        if (tile != null) tile.till();
    }

    public void waterTile(int x, int y) {
        FarmTile tile = getTile(x, y);
        if (tile != null) tile.water();
    }

    public FarmTile getTile(int x, int y) {
        return tiles.get(key(x, y));
    }

    public Map<String, FarmTile> getAllTiles() {
        return tiles;
    }

    private String key(int x, int y) { return x + "," + y; }

    public void resetGrid() {
        for (FarmTile tile : tiles.values()) tile.reset();
    }

    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public float getTileSize() { return tileSize; }
}
