package de.emn4tor.farm;

import static org.lwjgl.opengl.GL11.*;

public class FarmRenderer {

    private final FarmGrid farmGrid;

    public FarmRenderer(FarmGrid farmGrid) {
        this.farmGrid = farmGrid;
    }

    public void render() {
        glDisable(GL_TEXTURE_2D);

        for (FarmTile tile : farmGrid.getAllTiles().values()) {
            renderTile(tile);
        }

        glEnable(GL_TEXTURE_2D);
    }

    private void renderTile(FarmTile tile) {
        float x = farmGrid.gridToWorldX(tile.getGridX());
        float y = farmGrid.gridToWorldY(tile.getGridY());
        float size = farmGrid.getTileSize();

        // Draw base tile color based on state
        if (tile.getTillState() == FarmTile.TillState.TILLED) {
            if (tile.isWatered()) {
                // Dark brown for watered soil
                glColor4f(0.3f, 0.2f, 0.1f, 1.0f);
            } else {
                // Light brown for dry tilled soil
                glColor4f(0.55f, 0.35f, 0.2f, 1.0f);
            }

            // Draw filled tile
            glBegin(GL_QUADS);
            glVertex2f(x, y);
            glVertex2f(x + size, y);
            glVertex2f(x + size, y + size);
            glVertex2f(x, y + size);
            glEnd();

            // Draw tile border
            glColor4f(0.2f, 0.15f, 0.1f, 1.0f);
            glLineWidth(2.0f);
            glBegin(GL_LINE_LOOP);
            glVertex2f(x, y);
            glVertex2f(x + size, y);
            glVertex2f(x + size, y + size);
            glVertex2f(x, y + size);
            glEnd();

            // Draw plant if present
            if (tile.getPlantType() != FarmTile.PlantType.NONE) {
                renderPlant(tile, x, y, size);
            }
        }
    }

    private void renderPlant(FarmTile tile, float x, float y, float size) {
        float centerX = x + size / 2;
        float centerY = y + size / 2;
        float plantSize = size * 0.4f + (tile.getGrowthStage() * size * 0.1f);

        // Color based on plant type
        switch (tile.getPlantType()) {
            case WHEAT:
                // Yellow-green for wheat
                glColor4f(0.8f, 0.8f, 0.2f, 1.0f);
                break;
            case CARROT:
                // Orange for carrot
                glColor4f(1.0f, 0.5f, 0.0f, 1.0f);
                break;
            case POTATO:
                // Light green for potato
                glColor4f(0.6f, 0.8f, 0.4f, 1.0f);
                break;
            default:
                glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
        }

        // Draw simple plant representation
        glBegin(GL_QUADS);
        glVertex2f(centerX - plantSize / 2, centerY - plantSize / 2);
        glVertex2f(centerX + plantSize / 2, centerY - plantSize / 2);
        glVertex2f(centerX + plantSize / 2, centerY + plantSize / 2);
        glVertex2f(centerX - plantSize / 2, centerY + plantSize / 2);
        glEnd();

        // Draw plant border
        glColor4f(0.0f, 0.4f, 0.0f, 1.0f);
        glLineWidth(1.5f);
        glBegin(GL_LINE_LOOP);
        glVertex2f(centerX - plantSize / 2, centerY - plantSize / 2);
        glVertex2f(centerX + plantSize / 2, centerY - plantSize / 2);
        glVertex2f(centerX + plantSize / 2, centerY + plantSize / 2);
        glVertex2f(centerX - plantSize / 2, centerY + plantSize / 2);
        glEnd();
    }
}