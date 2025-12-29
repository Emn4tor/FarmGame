package de.emn4tor;

import org.lwjgl.glfw.GLFW;

import static org.lwjgl.system.MemoryStack.stackPush;

public class InputManager {

    private final long window;
    private float mouseX, mouseY;
    private boolean leftMousePressed;
    private boolean rightMousePressed;
    private boolean wasLeftMousePressed;
    private boolean wasRightMousePressed;

    public InputManager(long window) {
        this.window = window;
    }

    public void update() {
        try (var stack = stackPush()) {
            var xBuf = stack.mallocDouble(1);
            var yBuf = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(window, xBuf, yBuf);
            mouseX = (float) xBuf.get(0);
            mouseY = (float) yBuf.get(0);
        }

        wasLeftMousePressed = leftMousePressed;
        wasRightMousePressed = rightMousePressed;

        leftMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        rightMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    public float getMouseX() { return mouseX; }
    public float getMouseY() { return mouseY; }
    public boolean isLeftMousePressed() { return leftMousePressed; }
    public boolean isRightMousePressed() { return rightMousePressed; }

    // Returns true only on the frame the button was clicked (not held)
    public boolean isLeftMouseClicked() {
        return leftMousePressed && !wasLeftMousePressed;
    }

    public boolean isRightMouseClicked() {
        return rightMousePressed && !wasRightMousePressed;
    }

    public boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }
}