package Render;

import Core.Noise;

import static Core.Main.height;
import static Core.Main.width;
import static org.lwjgl.opengl.GL11.*;

public class SmockRender {
    private Noise noise;
    private final float time = 0;

    public void init() {
        noise = new Noise(42);
    }

    public void render() {
        int cols = 100;
        int rows = 75;
        float cellWidth = width / (float)cols;
        float cellHeight = height / (float)rows;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                float nx = x * 0.05f;
                float ny = y * 0.05f;

                float n = noise.fbm(nx + time * 0.3f, ny + time * 0.2f, 5, 2.0f, 0.5f);
                float alpha = (n + 1f) * 0.5f; // remap [-1,1] → [0,1]

                glColor4f(0.9f, 0.9f, 1.0f, alpha * 0.4f); // Blanc bleuté semi-transparent
                float px = x * cellWidth;
                float py = y * cellHeight;
                drawQuad(px, py, cellWidth, cellHeight);
            }
        }
    }

    private void drawQuad(float x, float y, float w, float h) {
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + h);
        glVertex2f(x, y + h);
        glEnd();
    }
}
