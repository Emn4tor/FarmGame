package de.emn4tor;

import de.emn4tor.farm.FarmGrid;
import de.emn4tor.farm.FarmRenderer;
import de.emn4tor.ui.FPSCounter;
import de.emn4tor.ui.HotbarItem;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;

public class Game {

    private long window;
    private Renderer renderer;
    private InputManager input;
    private FarmGrid farmGrid;
    private FarmRenderer farmRenderer;

    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 10;
    private static final int TILE_SIZE = 64;

    public Game(long window) {
        this.window = window;
    }

    public void initGL(FPSCounter fpsCounter) {
        GL.createCapabilities();
        renderer = new Renderer(fpsCounter, window);
        input = new InputManager(window);

        farmGrid = new FarmGrid(GRID_WIDTH, GRID_HEIGHT, TILE_SIZE);
        farmRenderer = new FarmRenderer(farmGrid);
    }

    public void update() {
        input.update();
        renderer.updateBox(input.getMouseX(), input.getMouseY(), input.isLeftMousePressed());

        // Handle drag-based tilling/watering
        handleDragTools();
    }

    private void handleDragTools() {
        float mouseX = input.getMouseX();
        float mouseY = input.getMouseY();

        int gridX = farmGrid.worldToGridX(mouseX);
        int gridY = farmGrid.worldToGridY(mouseY);

        // Skip invalid coordinates
        if (gridX < 0 || gridX >= GRID_WIDTH || gridY < 0 || gridY >= GRID_HEIGHT) return;

        // Left mouse = hoe, right mouse = watering can
        if (input.isLeftMousePressed()) {
            farmGrid.tillTile(gridX, gridY);
        }
        if (input.isRightMousePressed()) {
            farmGrid.waterTile(gridX, gridY);
        }
    }

    private void handleToolUse() {
        HotbarItem selectedItem = renderer.getSelectedItem();
        if (selectedItem == null) return;

        float mouseX = input.getMouseX();
        float mouseY = input.getMouseY();

        // Convert mouse position to grid coordinates
        int gridX = farmGrid.worldToGridX(mouseX);
        int gridY = farmGrid.worldToGridY(mouseY);

        // Use tool based on type
        switch (selectedItem.getToolType()) {
            case HOE:
                farmGrid.tillTile(gridX, gridY);
                break;
            case WATERING_CAN:
                farmGrid.waterTile(gridX, gridY);
                break;
            case NONE:
                // No action for non-tool items
                break;
        }
    }

    public void render(FPSCounter fpsCounter) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderer.renderBackground();

        // Render farm tiles
        renderer.setupFarmView();
        farmRenderer.render();

        // Render UI
        renderer.renderBox();
        renderer.renderFPS();
    }

    public Renderer getRenderer() {
        return renderer;
    }
}