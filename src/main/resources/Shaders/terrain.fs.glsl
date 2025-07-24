#version 330 core

in float noise;
out vec4 FragColor;

void main() {
    vec3 color;

    // COULEURS PLUS CONTRASTÉES
    if (noise < 0.3) {
        color = vec3(0.0, 0.1, 0.5);  // Bleu foncé océan
    } else if (noise < 0.4) {
        color = vec3(0.0, 0.3, 0.7);  // Bleu moyen
    } else if (noise < 0.5) {
        color = vec3(0.8, 0.7, 0.4);  // BEIGE/SABLE - transition franche
    } else if (noise < 0.6) {
        color = vec3(0.2, 0.8, 0.1);  // VERT VIF - contraste fort
    } else if (noise < 0.75) {
        color = vec3(0.1, 0.6, 0.1);  // Vert foncé
    } else if (noise < 0.85) {
        color = vec3(0.5, 0.3, 0.1);  // Brun montagne
    } else {
        color = vec3(1.0, 1.0, 1.0);  // BLANC PUR - pics
    }

    FragColor = vec4(color, 1.0);
}