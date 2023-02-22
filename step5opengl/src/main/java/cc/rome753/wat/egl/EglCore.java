package cc.rome753.wat.egl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;


public class EglCore {

    EGLDisplay mEglDisplay = EGL14.EGL_NO_DISPLAY;
    EGLContext mEglContext = EGL14.EGL_NO_CONTEXT;
    EGLConfig mEglConfig = null;

    EGLSurface mEglSurface = null;

    public EglCore() {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 0)) {
            mEglDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }

        String vendor = EGL14.eglQueryString(mEglDisplay, EGL14.EGL_VENDOR);
        Log.d("chao", "egl vendor: " + vendor); // 打印此版本EGL的实现厂商

        String versionStr = EGL14.eglQueryString(mEglDisplay, EGL14.EGL_VERSION);
        Log.d("chao", "egl version: " + versionStr);// 打印EGL版本号

        String extension = EGL14.eglQueryString(mEglDisplay, EGL14.EGL_EXTENSIONS);
        Log.d("chao", "egl extension: " + extension); //打印支持的EGL扩展


        int[] attributes = new int[] {
                EGL14.EGL_RED_SIZE, 8,  //指定RGB中的R大小（bits）
                EGL14.EGL_GREEN_SIZE, 8, //指定G大小
                EGL14.EGL_BLUE_SIZE, 8,  //指定B大小
                EGL14.EGL_ALPHA_SIZE, 8, //指定Alpha大小，以上四项实际上指定了像素格式
                EGL14.EGL_DEPTH_SIZE, 16, //指定深度缓存(Z Buffer)大小
                EGL14.EGL_RENDERABLE_TYPE, 4, //指定渲染api类别，这里或者是硬编码的4，或者是EGL14.EGL_OPENGL_ES2_BIT
                EGL14.EGL_NONE };  //总是以EGL10.EGL_NONE结尾

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEglDisplay, attributes, 0, configs, 0, configs.length, numConfigs, 0)) {    //获取所有
            Log.w("chao", "unable to find RGB8888 suitable EGLConfig");
        }
        mEglConfig = configs[0];

        int[] attrs = { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE };
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, EGL14.EGL_NO_CONTEXT, attrs, 0);
    }

    EGLSurface createPbufferSurface(int w, int h) {
        int [] attrs = {
                EGL14.EGL_WIDTH, w,
                EGL14.EGL_HEIGHT, h,
                EGL14.EGL_NONE
        };
        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(mEglDisplay, mEglConfig, attrs, 0);
        if (eglSurface == null) {
            throw new RuntimeException("eglCreatePbufferSurface failed!");
        }
        return eglSurface;
    }

    public void makeCurrent(int w, int h) {
        mEglSurface = createPbufferSurface(w, h);
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            Log.e("chao", "eglMakeCurrent failed");
        }
    }

    public void release() {
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
    }
}
