package Render;

import Core.Noise;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Random;

import static Core.Main.height;
import static Core.Main.width;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SmockRender {
    private Noise noise;
    private Shader shader;
    private int VAO, VBO;
    private int vertexCount;
    Random seed = new Random();

    public void init() {
        noise = new Noise(seed.nextLong());
        shader = new Shader("terrain");

        setupMesh();

        // Configuration du blending pour la transparence
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void setupMesh() {
        int cols = 200;
        int rows = 150;
        float cellWidth = width / (float) cols;
        float cellHeight = height / (float) rows;

        // 6 vertices par quad (2 triangles)
        vertexCount = cols * rows * 6;
        FloatBuffer vertices = BufferUtils.createFloatBuffer(vertexCount * 3); // x, y, noise

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                float px = x * cellWidth;
                float py = y * cellHeight;

                // Convertir en coordonnées normalisées [-1, 1]
                float x1 = (px / width) * 2.0f - 1.0f;
                float y1 = (py / height) * 2.0f - 1.0f;
                float x2 = ((px + cellWidth) / width) * 2.0f - 1.0f;
                float y2 = ((py + cellHeight) / height) * 2.0f - 1.0f;

                // Calculer le noise pour ce quad
                float nx = x * 0.05f;
                float ny = y * 0.05f;
                float n = noise.fbm(nx, ny, 8, 2.0f, 0.5f);
                float normalizedNoise = (n + 1f) * 0.5f; // [-1,1] → [0,1]

                // Premier triangle
                vertices.put(x1).put(y1).put(normalizedNoise);
                vertices.put(x2).put(y1).put(normalizedNoise);
                vertices.put(x1).put(y2).put(normalizedNoise);

                // Deuxième triangle
                vertices.put(x2).put(y1).put(normalizedNoise);
                vertices.put(x2).put(y2).put(normalizedNoise);
                vertices.put(x1).put(y2).put(normalizedNoise);
            }
        }

        vertices.flip();

        // Créer VAO et VBO gt
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Position (location = 0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Noise value (location = 1)
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 3 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    public void render() {
        shader.use();

        // Matrice de projection orthographique (optionnelle ici car on normalise déjà)
        Matrix4f projection = new Matrix4f().identity();
        float[] projMatrix = new float[16];
        projection.get(projMatrix);
        shader.setMatrix4f("projection", projMatrix);

        glBindVertexArray(VAO);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        shader.cleanup();
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
    }
}