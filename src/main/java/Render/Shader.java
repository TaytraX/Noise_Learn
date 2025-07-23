package Render;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private int programId;

    public Shader(String shaderName) {
        String vertexPath = "src/main/resources/Shaders/" + shaderName + ".vs.glsl";
        String fragmentPath = "src/main/resources/Shaders/" + shaderName + ".fs.glsl";

        int vertexShader = createShader(vertexPath, GL_VERTEX_SHADER);
        int fragmentShader = createShader(fragmentPath, GL_FRAGMENT_SHADER);

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        // Vérifier les erreurs de link
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Shader linking failed: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int createShader(String filePath, int type) {
        String source;
        try {
            source = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shader: " + filePath);
        }

        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader compilation failed: " + glGetShaderInfoLog(shader));
        }

        return shader;
    }

    public void use() {
        glUseProgram(programId);
    }

    public void setMatrix4f(String name, float[] matrix) {
        int location = glGetUniformLocation(programId, name);
        glUniformMatrix4fv(location, false, matrix);
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }
}