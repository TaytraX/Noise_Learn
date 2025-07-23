#version 330 core

in float noise;
out vec4 FragColor;

void main() {
    vec3 color;

    if (noise < 0.5) {
        // Eau - Bleu
        color = vec3(0.2, 0.4, 0.8);
    } else if (noise < 0.6) {
        // Terre/Herbe - Vert
        color = vec3(0.2, 0.7, 0.2);
    } else if (noise < 0.7) {
        // Montagne - Gris/Marron
        color = vec3(0.5, 0.4, 0.3);
    } else {
        // Neige/Pics - Blanc
        color = vec3(0.9, 0.9, 1.0);
    }

    FragColor = vec4(color, 1.0);
}