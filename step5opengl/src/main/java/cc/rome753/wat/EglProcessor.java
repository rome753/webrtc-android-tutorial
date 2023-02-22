package cc.rome753.wat;

import android.opengl.GLES20;

import org.webrtc.JavaI420Buffer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoProcessor;
import org.webrtc.VideoSink;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import cc.rome753.wat.egl.EglThread;

public class EglProcessor implements VideoProcessor {

        EglThread eglThread = new EglThread("my-egl");
        YUVRender yuvRender = new YUVRender();
        VideoSink videoSink;

        ByteBuffer dataY;
        ByteBuffer dataU;
        ByteBuffer dataV;

        @Override
        public void setSink(VideoSink videoSink) {
            this.videoSink = videoSink;
        }

        @Override
        public void onCapturerStarted(boolean b) {
        }

        @Override
        public void onCapturerStopped() {

        }

        @Override
        public void onFrameCaptured(VideoFrame videoFrame) {
            int w = videoFrame.getBuffer().getWidth();
            int h = videoFrame.getBuffer().getHeight();

//                ImageBytes imageBytes = ImageBytes.create(videoFrame);
            VideoFrame.I420Buffer ori = videoFrame.getBuffer().toI420();

            eglThread.initEglCore(w, h);
            eglThread.post(() -> {
                yuvRender.onSurfaceCreated(null, null);
                yuvRender.onSurfaceChanged(null, w, h);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                yuvRender.getYuvShader().draw(w, h, ori.getDataY(), ori.getDataU(), ori.getDataU());

                IntBuffer buffer =  IntBuffer.allocate(w * h);
                GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
                byte[] yuv = YUVTools.rgb2Yuv420(buffer.array(), w, h);
                buffer.rewind();

                if (dataY == null) {
                    dataY = ByteBuffer.allocateDirect(w * h);
                }
                dataY.put(yuv, 0, w * h);

                if (dataU == null) {
                    dataU = ByteBuffer.allocateDirect(w * h / 4);
                }
                dataU.put(yuv, w * h, w * h / 4);

                if (dataV == null) {
                    dataV = ByteBuffer.allocateDirect(w * h / 4);
                }
                dataV.put(yuv, w * h + w * h / 4, w * h / 4);

                dataY.position(0);
                dataU.position(0);
                dataV.position(0);

                JavaI420Buffer nb = JavaI420Buffer.wrap(w, h, dataY, ori.getStrideY(), dataU, ori.getStrideU(), dataV, ori.getStrideV(), null);
                VideoFrame newFrame = new VideoFrame(nb, videoFrame.getRotation(), videoFrame.getTimestampNs());
                videoSink.onFrame(newFrame);
            });

//                videoSink.onFrame(videoFrame);
        }

}
