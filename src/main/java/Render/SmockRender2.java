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

public class SmockRender2 {
    private Perlin noise;
    private Shader shader;
    private int VAO, VBO;
    private int vertexCount;
    Random seed = new Random();

    public void init() {
        long worldSeed = seed.nextLong(); // Seed fixe pour tests, tu peux la randomiser
        noise = new Perlin(worldSeed);
        shader = new Shader("terrain");

        setupMesh();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void setupMesh() {
        int cols = 800;
        int rows = 600;
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

                // Coordonnées pour le bruit (plus petites pour voir les patterns)
                float nx = x * 0.8f; // Réduit pour voir les continents
                float ny = y * 0.8f;

                // **GÉNÉRATION AVEC PERLIN MODIFIÉ SEUL**

                // 1. TRÈS grande échelle pour définir les zones continentales
                float continentalBase = noise.fbm(nx * 0.015f, ny * 0.015f, 2, 2.0f, 0.7f);

                // 2. Échelle moyenne pour la forme générale
                float regionalShape = noise.fbm(nx * 0.04f, ny * 0.04f, 3, 2.0f, 0.6f);

                // 3. Détails locaux
                float localDetails = noise.fbm(nx * 0.12f, ny * 0.12f, 4, 2.0f, 0.4f);

                // 4. Combinaison hiérarchique_le continental domine
                float rawNoise = (continentalBase * 0.6f) + (regionalShape * 0.3f) + (localDetails * 0.1f);

                // 5. Modification pour favoriser les grandes masses
                float modifiedNoise = applyLandBias(rawNoise); // 1.2f favorise les terres

                // 6. Seuils ajustés pour plus de connectivité
                float finalLandmask = smoothstep(modifiedNoise);

                float finalHeight = getFinalHeight(finalLandmask, localDetails);

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

    private static float getFinalHeight(float finalLandmask, float localDetails) {
        float finalHeight;
        if (finalLandmask > 0.7f) {
            finalHeight = 0.7f + (localDetails * 0.25f);
        } else if (finalLandmask > 0.5f) {
            finalHeight = 0.55f + (localDetails * 0.2f);
        } else if (finalLandmask > 0.3f) {
            finalHeight = 0.45f + (localDetails * 0.1f);
        } else if (finalLandmask > 0.1f) {
            finalHeight = 0.35f + (localDetails * 0.05f);
        } else {
            finalHeight = 0.2f + (localDetails * 0.03f);
        }

        finalHeight = Math.max(0f, Math.min(1f, finalHeight));
        return finalHeight;
    }

    // Version plus claire et paramétrable
    private float applyLandBias(float noise) {
        // Normaliser vers [0,1]
        float normalized = noise * 0.5f + 0.5f;

        // Appliquer la courbe de puissance
        float curved = (float)Math.pow(normalized, (float) 1.2);

        // Retourner vers [-1,1]
        return curved * 2.0f - 1.0f;
    }

    // Fonction utilitaire smoothstep pour des transitions douces
    private float smoothstep(float x) {
        x = Math.max(0f, Math.min(1f, (x - (float) -0.2) / ((float) 0.2 - (float) -0.2)));
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