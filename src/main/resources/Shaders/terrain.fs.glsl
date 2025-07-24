#version 330 core

in float noise;
out vec4 FragColor;

void main() {
    vec3 color;

    if (noise < 0.28) {          // Océan profond (était 0.3)
        color = vec3(0.1, 0.2, 0.6);
    } else if (noise < 0.44) {   // Eau peu profonde (était 0.45)
        color = vec3(0.3, 0.5, 0.8);
    } else if (noise < 0.58) {   // Plaines/Herbe (était 0.6)
        color = vec3(0.3, 0.7, 0.3);
    } else if (noise < 0.73) {   // Collines (était 0.75)
        color = vec3(0.4, 0.6, 0.2);
    } else if (noise < 0.83) {   // Montagnes (était 0.85)
        color = vec3(0.5, 0.4, 0.3);
    } else {
        color = vec3(0.9, 0.9, 1.0);
    }

    FragColor = vec4(color, 1.0);
}