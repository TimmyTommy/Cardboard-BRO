package de.tinf13aibi.cardboardbro;

import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import de.tinf13aibi.cardboardbro.Enums.Programs;
import de.tinf13aibi.cardboardbro.Enums.Shaders;

/**
 * Created by Tommy on 02.01.2016.
 */
public class ShaderCollection {
    private static ShaderCollection ourInstance = new ShaderCollection();
    private static HashMap<Shaders, Integer> mShaders = new HashMap<>();
    private static HashMap<Programs, Integer> mPrograms = new HashMap<>();

    public static ShaderCollection getInstance() {
        return ourInstance;
    }

    private ShaderCollection() {
    }

    public static int getProgram(Programs program){
        return mPrograms.get(program);
    }

    public static int addProgram(Programs program, Shaders vertexShader, Shaders fragmentShader){
        int aProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(aProgram, mShaders.get(vertexShader));
        GLES20.glAttachShader(aProgram, mShaders.get(fragmentShader));
        GLES20.glLinkProgram(aProgram);
        mPrograms.put(program, aProgram);
        return aProgram;
    }

    public static int loadGLShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    public static int loadGLShader(Shaders shaderType, int type, InputStream inputStream) {
        String code = readRawTextFile(inputStream);
        int shader = loadGLShader(type, code);
        mShaders.put(shaderType, shader);
        return shader;
    }

    private static String readRawTextFile(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
