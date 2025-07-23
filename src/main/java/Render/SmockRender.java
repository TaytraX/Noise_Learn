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
                float nx = x * 0.8f; // Réduit pour voir les continents
                float ny = y * 0.8f;

                // Dans setupMesh(), remplace toute la section génération par :

// **GÉNÉRATION DE VRAIS CONTINENTS**

                // 1. Noise continental ULTRA grande échelle - quelques grands blobs seulement
                float continentalCore = cellular.cellularNoise(nx * 0.03f, ny * 0.03f, 0.02f);


                float continentalSupport = cellular.cellularNoise(nx * 0.05f + 100f, ny * 0.05f + 100f, 0.03f);

                // 3. Combinaison multiplicative pour créer de GROS blobs
                float baseLandmask = continentalCore * continentalSupport;

                // 4. Seuil TRÈS permissif pour avoir de grandes zones
                float mainLandmask = smoothstep(0.2f, 0.5f, baseLandmask);

                // 5. Découpage côtier MINIMAL et SEULEMENT sur les bordures
                float coastalDetail = cellular.cellularNoise2(nx * 0.6f, ny * 0.6f, 3);
                float borderZone = smoothstep(0.8f, 0.95f, mainLandmask); // Seulement les bordures externes

                // Appliquer le découpage côtier UNIQUEMENT sur une fine bande de bordure
                float coastalModifier = 1.0f - (borderZone * Math.max(0f, -coastalDetail * 0.2f));
                float finalLandmask = mainLandmask * coastalModifier;

                // 6. Élévation avec variation interne
                float detailNoise = noise.fbm(nx * 2f, ny * 2f, 3, 2.0f, 0.4f);

                float finalHeight;
                if (finalLandmask > 0.6f) { // Cœur continental - montagneux
                    finalHeight = 0.7f + (detailNoise * 0.25f);
                } else if (finalLandmask > 0.4f) { // Terres moyennes
                    finalHeight = 0.55f + (detailNoise * 0.2f);
                } else if (finalLandmask > 0.2f) { // Plaines côtières
                    finalHeight = 0.45f + (detailNoise * 0.1f);
                } else if (finalLandmask > 0.05f) { // Marécages/lagunes
                    finalHeight = 0.32f + (detailNoise * 0.05f);
                } else { // Océan
                    finalHeight = 0.2f + (detailNoise * 0.03f);
                }

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