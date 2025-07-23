#version 330 core

in float noise;
out vec4 FragColor;

void main() {
    vec3 color;

    if (noise < 0.3) {
        // Océan profond - Bleu foncé
        color = vec3(0.1, 0.2, 0.6);
    } else if (noise < 0.45) {
        // Eau peu profonde/côtes - Bleu clair
        color = vec3(0.3, 0.5, 0.8);
    } else if (noise < 0.6) {
        // Plaines/Herbe - Vert
        color = vec3(0.3, 0.7, 0.3);
    } else if (noise < 0.75) {
        // Collines - Vert foncé/Marron
        color = vec3(0.4, 0.6, 0.2);
    } else if (noise < 0.85) {
        // Montagnes - Gris/Marron
        color = vec3(0.5, 0.4, 0.3);
    } else {
        // Pics enneigés - Blanc
        color = vec3(0.9, 0.9, 1.0);
    }

    FragColor = vec4(color, 1.0);
}