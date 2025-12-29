package de.emn4tor.ui;

import de.emn4tor.util.TextureHandler;

import static org.lwjgl.opengl.GL11.*;

public class HotbarItem {

    private float x, y;
    private float slotX, slotY;
    private final float size;

    private boolean dragging;
    private float offsetX, offsetY;
    private final int textureId;
    private final String name;
    private final ToolType toolType;

    public enum ToolType {
        WATERING_CAN,
        HOE,
        NONE
    }

    // Define item names based on texture ID
    private static final String[] ITEM_NAMES = {
            "Watering Can",
            "Hoe",
            "Item 2",
            "Item 3",
            "Item 4",
            "Item 5"
    };

    // Define tool types based on texture ID
    private static final ToolType[] TOOL_TYPES = {
            ToolType.WATERING_CAN,
            ToolType.HOE,
            ToolType.NONE,
            ToolType.NONE,
            ToolType.NONE,
            ToolType.NONE
    };

    public HotbarItem(int textureId, float size) {
        this.textureId = textureId;
        this.size = size;
        this.name = textureId < ITEM_NAMES.length ? ITEM_NAMES[textureId] : "Unknown";
        this.toolType = textureId < TOOL_TYPES.length ? TOOL_TYPES[textureId] : ToolType.NONE;
    }

    public void setSlotPosition(float x, float y) {
        slotX = x;
        slotY = y;
        if (!dragging) {
            this.x = x;
            this.y = y;
        }
    }

    /** Update dragging logic */
    public void update(float mx, float my, boolean pressed) {
        if (pressed) {
            if (!dragging && isMouseOver(mx, my)) {
                dragging = true;
                offsetX = mx - x;
                offsetY = my - y;
            }
            if (dragging) {
                x = mx - offsetX;
                y = my - offsetY;
            }
        } else if (dragging) {
            dragging = false;
            x = slotX;
            y = slotY;
        }
    }

    private boolean isMouseOver(float mx, float my) {
        return mx >= x && mx <= x + size &&
                my >= y && my <= y + size;
    }

    /** Render item using bound texture */
    public void render() {
        glEnable(GL_TEXTURE_2D);
        TextureHandler.bind(textureId);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(x, y);
        glTexCoord2f(1, 0); glVertex2f(x + size, y);
        glTexCoord2f(1, 1); glVertex2f(x + size, y + size);
        glTexCoord2f(0, 1); glVertex2f(x, y + size);
        glEnd();

        glDisable(GL_TEXTURE_2D);
    }

    public boolean isDragging() {
        return dragging;
    }

    public String getName() {
        return name;
    }

    public ToolType getToolType() {
        return toolType;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }
}