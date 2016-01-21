package de.tinf13aibi.cardboardbro.Shader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Tommy on 02.01.2016.
 */
public class ShaderCollection {
    private static ShaderCollection mInstance = new ShaderCollection();
    private static HashMap<Shaders, Integer> mShaders = new HashMap<>();
    private static HashMap<Programs, Integer> mPrograms = new HashMap<>();
    private static HashMap<Textures, Integer> mTextures = new HashMap<>();

    public static ShaderCollection getInstance() {
        return mInstance;
    }

    private ShaderCollection() {
    }

    public static int getProgram(Programs program){
        return mPrograms.get(program);
    }

    public static int getTexture(Textures texture){
        return mTextures.get(texture);
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

    public static void loadTexture(final Context context, Textures texture, final int resourceId) {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling
            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            //TODO Mipmapping testen: http://www.learnopengles.com/android-lesson-six-an-introduction-to-texture-filtering/
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }
        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }
        mTextures.put(texture, textureHandle[0]);
    }
}
