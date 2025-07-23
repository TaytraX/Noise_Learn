package Core.Noise;

import java.util.Random;

public class Cellular {
    private final long seed;
    private final Random random;

    public Cellular(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    // Cellular noise pour les grandes structures continentales
    public float cellularNoise(float x, float y, float numPoints) {
        float minDist = Float.MAX_VALUE;

        // Grille pour optimiser la recherche
        int gridX = (int)Math.floor(x);
        int gridY = (int)Math.floor(y);

        // Chercher dans une grille 3x3 autour du point
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // Hash pour obtenir des points reproductibles
                Random cellRand = new Random(seed + (gridX + i) * 73856093 + (gridY + j) * 19349663);

                // Générer plusieurs points par cellule
                for (int p = 0; p < numPoints; p++) {
                    float px = gridX + i + cellRand.nextFloat();
                    float py = gridY + j + cellRand.nextFloat();

                    float dist = distance(x, y, px, py);
                    minDist = Math.min(minDist, dist);
                }
            }
        }

        return minDist;
    }

    // Distance à deux points pour créer des frontières plus organiques
    public float cellularNoise2(float x, float y, int numPoints) {
        float minDist1 = Float.MAX_VALUE;
        float minDist2 = Float.MAX_VALUE;

        int gridX = (int)Math.floor(x);
        int gridY = (int)Math.floor(y);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Random cellRand = new Random(seed + (gridX + i) * 73856093 + (gridY + j) * 19349663);

                for (int p = 0; p < numPoints; p++) {
                    float px = gridX + i + cellRand.nextFloat();
                    float py = gridY + j + cellRand.nextFloat();

                    float dist = distance(x, y, px, py);

                    if (dist < minDist1) {
                        minDist2 = minDist1;
                        minDist1 = dist;
                    } else if (dist < minDist2) {
                        minDist2 = dist;
                    }
                }
            }
        }

        // Retourner la différence pour des frontières plus nettes
        return minDist2 - minDist1;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
}