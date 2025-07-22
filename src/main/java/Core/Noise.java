package Core;

import java.util.Random;

public class Noise {
    private final int[] permutation;

    public Noise(int seed) {
        permutation = new int[512];
        int[] p = new int[256];
        Random rand = new Random(seed);

        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        for (int i = 0; i < 512; i++) {
            permutation[i] = p[i % 256];
        }
    }

    public float noise(float x, float y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        float u = fade(x);
        float v = fade(y);

        int A = permutation[X] + Y;
        int B = permutation[X + 1] + Y;

        return lerp(v,
                lerp(u, grad(permutation[A], x, y), grad(permutation[B], x - 1, y)),
                lerp(u, grad(permutation[A + 1], x, y - 1), grad(permutation[B + 1], x - 1, y - 1))
        );
    }

    public float fbm(float x, float y, int octaves, float lacunarity, float gain) {
        float total = 0;
        float amplitude = 1;
        float frequency = 1;
        float maxAmplitude = 0;

        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= gain;
            frequency *= lacunarity;
        }

        return total / maxAmplitude;
    }

    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y) {
        int h = hash & 7;
        float u = h < 4 ? x : y;
        float v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}