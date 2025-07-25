#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in float aNoise;

out float noise;

uniform mat4 projection;

void main() {
    gl_Position = projection * vec4(aPos, 0.0, 1.0);
    noise = aNoise;
}