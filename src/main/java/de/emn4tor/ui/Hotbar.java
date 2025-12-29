package de.emn4tor.ui;

import de.emn4tor.Renderer;

import java.util.ArrayList;
import java.util.List;

public class Hotbar {

    private final List<HotbarItem> items = new ArrayList<>();
    private final int slots = 6;

    private final float slotSize = 64;
    private final float padding = 10;

    private int selectedSlot = -1; // -1 means nothing selected

    public Hotbar() {
        for (int i = 0; i < slots; i++) {
            items.add(new HotbarItem(i, slotSize)); // texture IDs 0-5
        }
        updatePositions();
    }

    /** Position items at bottom center */
    public void updatePositions() {
        float totalWidth = slots * slotSize + (slots - 1) * padding;
        float startX = (Renderer.windowWidth - totalWidth) / 2f;
        float y = Renderer.windowHeight - slotSize - 10;

        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSlotPosition(startX + i * (slotSize + padding), y);
        }
    }

    /** Update dragging for all items */
    public void update(float mouseX, float mouseY, boolean leftPressed) {
        for (HotbarItem item : items) {
            item.update(mouseX, mouseY, leftPressed);
        }
    }

    /** Check if mouse click is on a hotbar slot and select it */
    public void handleClick(float mouseX, float mouseY) {
        for (int i = 0; i < items.size(); i++) {
            HotbarItem item = items.get(i);
            float x = item.getX();
            float y = item.getY();
            float size = item.getSize();

            if (mouseX >= x && mouseX <= x + size &&
                    mouseY >= y && mouseY <= y + size) {
                selectedSlot = i;
                return;
            }
        }
    }

    /** Render all items */
    public void render() {
        for (HotbarItem item : items) {
            item.render();
        }
    }

    /** Get the currently dragged item, if any */
    public HotbarItem getDraggedItem() {
        for (HotbarItem item : items) {
            if (item.isDragging()) {
                return item;
            }
        }
        return null;
    }

    /** Get the currently selected item */
    public HotbarItem getSelectedItem() {
        if (selectedSlot >= 0 && selectedSlot < items.size()) {
            return items.get(selectedSlot);
        }
        return null;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }
}