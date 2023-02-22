package cc.rome753.wat.egl;

import android.os.Handler;
import android.os.HandlerThread;

public class EglThread {
    Handler mHandler;
    EglCore mEglCore;

    public EglThread(String name) {
        HandlerThread ht = new HandlerThread(name);
        ht.start();

        mHandler = new Handler(ht.getLooper());
    }

    public void initEglCore(int w, int h) {
        if (mEglCore == null) {
            mHandler.post(() -> {
                mEglCore = new EglCore();
                mEglCore.makeCurrent(w, h);
            });
        }
    }

    public void post(Runnable r) {
        if (mHandler != null) {
            mHandler.post(r);
        }
    }


    public void destroy() {
        if (mHandler != null) {
            mHandler.post(() -> {
                mEglCore.release();
                mHandler.getLooper().quitSafely();
            });
        }
    }


}
