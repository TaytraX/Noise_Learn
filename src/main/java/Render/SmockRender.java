package Render;

import Core.Noise.*;
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
    private Perlin noise;
    private Cellular cellular;
    private Shader shader;
    private int VAO, VBO;
    private int vertexCount;
    Random seed = new Random();

    public void init() {
        long worldSeed = seed.nextLong(); // Seed fixe pour tests, tu peux la randomiser
        noise = new Perlin(worldSeed);
        cellular = new Cellular(worldSeed);
        shader = new Shader("terrain");

        setupMesh();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void setupMesh() {
        int cols = 400;
        int rows = 300;
        float cellWidth = width / (float) cols;
        float cellHeight = height / (float) rows;

        vertexCount = cols * rows * 6;
        FloatBuffer vertices = BufferUtils.createFloatBuffer(vertexCount * 3);

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                float px = x * cellWidth;
                float py = y * cellHeight;

                float x1 = (px / width) * 2.0f - 1.0f;
                float y1 = (py / height) * 2.0f - 1.0f;
                float x2 = ((px + cellWidth) / width) * 2.0f - 1.0f;
                float y2 = ((py + cellHeight) / height) * 2.0f - 1.0f;

                // Coordonnées pour le noise (plus petites pour voir les patterns)
                float nx = x * 0.05f; // Réduit pour voir les continents
                float ny = y * 0.05f;

                // **NOUVELLE GÉNÉRATION HYBRIDE**

                // 1. Cellular noise grande échelle pour les continents
                float continentalNoise = cellular.cellularNoise(nx * 0.3f, ny * 0.3f, 0.1f);

                // 2. Cellular noise moyenne échelle pour fragmenter
                float regionalNoise = cellular.cellularNoise2(nx, ny, 7);

                // 3. Perlin pour les détails
                float detailNoise = noise.fbm(nx * 4f, ny * 4f, 4, 2.0f, 0.5f);

                // 4. Combiner pour obtenir le résultat final
                float landMask = smoothstep(0.4f, 0.6f, continentalNoise); // Forme des continents
                landMask *= smoothstep(-0.1f, 0.1f, regionalNoise); // Fragmentation

                // Si c'est de la terre, ajouter les détails d'élévation
                float finalHeight;
                if (landMask > 0.3f) { // Terre
                    finalHeight = 0.6f + (detailNoise * 0.4f * landMask);
                } else { // Eau
                    finalHeight = 0.2f + (detailNoise * 0.1f); // Variation océan
                }

                // Clamp pour éviter les valeurs aberrantes
                finalHeight = Math.max(0f, Math.min(1f, finalHeight));

                // Premier triangle
                vertices.put(x1).put(y1).put(finalHeight);
                vertices.put(x2).put(y1).put(finalHeight);
                vertices.put(x1).put(y2).put(finalHeight);

                // Deuxième triangle
                vertices.put(x2).put(y1).put(finalHeight);
                vertices.put(x2).put(y2).put(finalHeight);
                vertices.put(x1).put(y2).put(finalHeight);
            }
        }

        vertices.flip();

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 1, GL_FLOAT, false, 3 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    // Fonction utilitaire smoothstep pour des transitions douces
    private float smoothstep(float edge0, float edge1, float x) {
        x = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return x * x * (3f - 2f * x);
    }

    public void render() {
        shader.use();

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