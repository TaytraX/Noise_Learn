package Core;

import Render.Window;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwTerminate;

public class Engine {
    public Window window;
    public boolean isRunning;
    private long lastTime = System.currentTimeMillis();
    private float deltaTime = 0.000016f;

    public void start() {
        init();
        run();
    }

    private void init() {

        try {
            // Vérification critique
            window = Main.getWindow();
            if(window == null) {
                throw new Exception("Window not initialized");
            }

            window.init();

        } catch (Exception e) {
            System.exit(1); // Arrêt propre au lieu de crash
        }
    }

    public void render() {
        try {
            window.clear();
            window.update();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void run() {
        isRunning = true;
        while (isRunning && !window.windowShouldClose()) {
            long currentTime = System.currentTimeMillis();
            deltaTime = (currentTime - lastTime) / 1000f;
            lastTime = currentTime;

            deltaTime = Math.min(deltaTime, 0.016f);

            update();
            render();
        }
        cleanup();
    }

    public void update() {}

    public void cleanup() {
        window.cleanup();
        glfwTerminate();
    }
}