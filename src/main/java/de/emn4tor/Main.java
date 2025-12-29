package de.emn4tor;

import de.emn4tor.ui.FPSCounter;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    public static FPSCounter fpsCounter = new FPSCounter();
    private long window;
    private Game game;
    private double lastTime;


    public void run() {
        System.out.println("Hello LWJGL!");

        init();
        loop();

        // Cleanup
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(800, 600, "Farm Game", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwMaximizeWindow(window);

        // Setup mouse button callback for hotbar selection
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS && game != null) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);
                game.getRenderer().handleHotbarClick((float) xpos[0], (float) ypos[0]);
            }
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // Initialize game and input manager
        game = new Game(window);
    }

    private void loop() {
        game.initGL(fpsCounter); // setup OpenGL

        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            double deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            game.update();   // update game logic
            game.render(fpsCounter);   // draw everything
            glfwSwapBuffers(window);
            glfwPollEvents();
            fpsCounter.update(deltaTime);
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}