package cc.rome753.wat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.webrtc.Camera1Enumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    PeerConnectionFactory peerConnectionFactory;
    PeerConnection peerConnectionLocal;
    PeerConnection peerConnectionRemote;
    SurfaceViewRenderer localView;
    SurfaceViewRenderer remoteView;
    MediaStream mediaStreamLocal;
    MediaStream mediaStreamRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(this)
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                new DefaultVideoDecoderFactory(eglBaseContext);
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
        // create VideoCapturer
        VideoCapturer videoCapturer = createCameraCapturer(true);
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(480, 640, 30);

        localView = findViewById(R.id.localView);
        localView.setMirror(true);
        localView.init(eglBaseContext, null);

        // create VideoTrack
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
//        // display in localView
//        videoTrack.addSink(localView);




        SurfaceTextureHelper remoteSurfaceTextureHelper = SurfaceTextureHelper.create("RemoteCaptureThread", eglBaseContext);
        // create VideoCapturer
        VideoCapturer remoteVideoCapturer = createCameraCapturer(false);
        VideoSource remoteVideoSource = peerConnectionFactory.createVideoSource(remoteVideoCapturer.isScreencast());
        remoteVideoCapturer.initialize(remoteSurfaceTextureHelper, getApplicationContext(), remoteVideoSource.getCapturerObserver());
        remoteVideoCapturer.startCapture(480, 640, 30);

        remoteView = findViewById(R.id.remoteView);
        remoteView.setMirror(false);
        remoteView.init(eglBaseContext, null);

        // create VideoTrack
        VideoTrack remoteVideoTrack = peerConnectionFactory.createVideoTrack("102", remoteVideoSource);
//        // display in remoteView
//        remoteVideoTrack.addSink(remoteView);



        mediaStreamLocal = peerConnectionFactory.createLocalMediaStream("mediaStreamLocal");
        mediaStreamLocal.addTrack(videoTrack);

        mediaStreamRemote = peerConnectionFactory.createLocalMediaStream("mediaStreamRemote");
        mediaStreamRemote.addTrack(remoteVideoTrack);

        call(mediaStreamLocal, mediaStreamRemote);
    }


    private void call(MediaStream localMediaStream, MediaStream remoteMediaStream) {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        peerConnectionLocal = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter("localconnection") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                peerConnectionRemote.addIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                runOnUiThread(() -> {
                    remoteVideoTrack.addSink(localView);
                });
            }
        });

        peerConnectionRemote = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter("remoteconnection") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                peerConnectionLocal.addIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                VideoTrack localVideoTrack = mediaStream.videoTracks.get(0);
                runOnUiThread(() -> {
                    localVideoTrack.addSink(remoteView);
                });
            }
        });

        peerConnectionLocal.addStream(localMediaStream);
        peerConnectionLocal.createOffer(new SdpAdapter("local offer sdp") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                // todo crashed here
                peerConnectionLocal.setLocalDescription(new SdpAdapter("local set local"), sessionDescription);
                peerConnectionRemote.addStream(remoteMediaStream);
                peerConnectionRemote.setRemoteDescription(new SdpAdapter("remote set remote"), sessionDescription);
                peerConnectionRemote.createAnswer(new SdpAdapter("remote answer sdp") {
                    @Override
                    public void onCreateSuccess(SessionDescription sdp) {
                        super.onCreateSuccess(sdp);
                        peerConnectionRemote.setLocalDescription(new SdpAdapter("remote set local"), sdp);
                        peerConnectionLocal.setRemoteDescription(new SdpAdapter("local set remote"), sdp);
                    }
                }, new MediaConstraints());
            }
        }, new MediaConstraints());
    }

    private VideoCapturer createCameraCapturer(boolean isFront) {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

}
