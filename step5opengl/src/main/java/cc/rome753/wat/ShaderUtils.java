package cc.rome753.wat;

import static android.opengl.GLES30.GL_COMPILE_STATUS;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_LINK_STATUS;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glAttachShader;
import static android.opengl.GLES30.glCompileShader;
import static android.opengl.GLES30.glCreateProgram;
import static android.opengl.GLES30.glCreateShader;
import static android.opengl.GLES30.glDeleteProgram;
import static android.opengl.GLES30.glDeleteShader;
import static android.opengl.GLES30.glGetProgramInfoLog;
import static android.opengl.GLES30.glGetProgramiv;
import static android.opengl.GLES30.glGetShaderInfoLog;
import static android.opengl.GLES30.glGetShaderiv;
import static android.opengl.GLES30.glLinkProgram;
import static android.opengl.GLES30.glShaderSource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ShaderUtils {

    public static int loadProgram() {
        int vShader = ShaderUtils.loadShader(GL_VERTEX_SHADER, loadAssets("shader_base_v.glsl"));
        int fShader = ShaderUtils.loadShader(GL_FRAGMENT_SHADER, loadAssets("shader_base_f.glsl"));
        return linkProgram(vShader, fShader);
    }

    public static int loadProgramYUV() {
        int vShader = ShaderUtils.loadShader(GL_VERTEX_SHADER, loadAssets("shader_yuv_v.glsl"));
        int fShader = ShaderUtils.loadShader(GL_FRAGMENT_SHADER, loadAssets("shader_yuv_f.glsl"));
        return linkProgram(vShader, fShader);
    }

    public static int loadProgramDiff() {
        int vShader = ShaderUtils.loadShader(GL_VERTEX_SHADER, loadAssets("shader_diff_v.glsl"));
        int fShader = ShaderUtils.loadShader(GL_FRAGMENT_SHADER, loadAssets("shader_diff_f.glsl"));
        return linkProgram(vShader, fShader);
    }

    public static int loadProgram(String vs, String fs) {
        int vShader = ShaderUtils.loadShader(GL_VERTEX_SHADER, vs);
        int fShader = ShaderUtils.loadShader(GL_FRAGMENT_SHADER, fs);
        return linkProgram(vShader, fShader);
    }

    private static int loadShader(int type, String shaderSrc) {
        int shader = glCreateShader(type);
        if (shader == 0) {
            Log.e("chao", "compile shader == 0");
            return 0;
        }
        glShaderSource(shader, shaderSrc);
        glCompileShader(shader);
        int[] compileStatus = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String log = glGetShaderInfoLog(shader);
            Log.e("chao", "glGetShaderiv fail " + log);
            glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static int linkProgram(int vShader, int fShader) {
        int program = glCreateProgram();
        if (program == 0) {
            Log.e("chao", "program == 0");
            return 0;
        }

        glAttachShader(program, vShader);
        glAttachShader(program, fShader);

        glLinkProgram(program);
        int[] linkStatus = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            String log = glGetProgramInfoLog(program);
            Log.e("chao", "linkProgram fail " + log);
            glDeleteProgram(program);
            return 0;
        }
        return program;
    }


    public static String loadAssets(String name) {
        String s = null;
        try {
            InputStream is = App.Companion.getApp().getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            s = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static Bitmap loadImageAssets(String name) {
        try {
            InputStream is = App.Companion.getApp().getAssets().open(name);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
