package cc.rome753.wat;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

/**
 * Created by chao on 2019/1/28.
 */

public class PeerConnectionAdapter implements PeerConnection.Observer {

    private String tag;

    public PeerConnectionAdapter(String tag) {
        this.tag = "chao " + tag;
    }

    private void log(String s) {
        Log.d(tag, s);
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        log("onSignalingChange " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        log("onIceConnectionChange " + iceConnectionState);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        log("onIceConnectionReceivingChange " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        log("onIceGatheringChange " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        log("onIceCandidate " + iceCandidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        log("onIceCandidatesRemoved " + iceCandidates);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        log("onAddStream " + mediaStream);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        log("onRemoveStream " + mediaStream);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        log("onDataChannel " + dataChannel);
    }

    @Override
    public void onRenegotiationNeeded() {
        log("onRenegotiationNeeded ");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        log("onAddTrack " + mediaStreams);
    }
}
