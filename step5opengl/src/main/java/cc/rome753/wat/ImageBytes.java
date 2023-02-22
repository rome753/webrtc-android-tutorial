package cc.rome753.wat;

import android.media.Image;

import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;

public class ImageBytes {
    public byte[] bytes;
    public int width;
    public int height;

    public ByteBuffer bufY;
    public ByteBuffer bufU;
    public ByteBuffer bufV;

    public ImageBytes(byte[] bytes, int width, int height) {
        this.bytes = bytes;
        this.width = width;
        this.height = height;

        int r0 = width * height;
        int u0 = r0 / 4;
        int v0 = u0;

        bufY = ByteBuffer.allocate(r0).put(bytes, 0, r0);
        // camera1的nv21格式，需要把uv全部放到bufU中，否则画面只有一半正常
        // 实际上bufU只使用了一半，这跟相机画面扫描的方向有关
        bufU = ByteBuffer.allocate(u0 + v0).put(bytes, r0, u0 + v0);
        bufV = ByteBuffer.allocate(v0).put(bytes, r0 + u0, v0);

        bufY.position(0);
        bufU.position(0);
        bufV.position(0);
    }

    public ImageBytes(Image image) {
        final Image.Plane[] planes = image.getPlanes();

        Image.Plane p0 = planes[0];
        Image.Plane p1 = planes[1];
        Image.Plane p2 = planes[2];

        ByteBuffer b0 = p0.getBuffer();
        ByteBuffer b1 = p1.getBuffer();
        ByteBuffer b2 = p2.getBuffer();

        int r0 = b0.remaining();

        int w0 = p0.getRowStride();
        int h0 = r0 / w0;
        if(r0 % w0 != 0) h0++;

        this.width = w0;
        this.height = h0;
        this.bufY = b0;
        this.bufU = b1;
        this.bufV = b2;
    }

    public static ImageBytes create(VideoFrame image) {
        VideoFrame.I420Buffer buf = image.getBuffer().toI420();

        ByteBuffer b0 = buf.getDataY();
        ByteBuffer b1 = buf.getDataU();
        ByteBuffer b2 = buf.getDataV();

        int r0 = b0.remaining();
        int r1 = b1.remaining();
        int r2 = b2.remaining();

        int w0 = buf.getStrideY();
        int h0 = r0 / w0;
        if(r0 % w0 > 0) h0++;
        int w1 = buf.getStrideU();
        int h1 = r1 / w1;
        if(r1 % w1 > 1) h1++;
        int w2 = buf.getStrideV();
        int h2 = r2 / w2;
        if(r2 % w2 > 2) h2++;

        int y = w0 * h0;
        int u = w1 * h1;
        int v = w2 * h2;

        byte[] bytes = new byte[y + u + v];

        b0.get(bytes, 0, r0);
        b1.get(bytes, y, r1); // u
        b2.get(bytes, y + u, r2); // v

        return new ImageBytes(bytes, w0, h0);
    }

}
