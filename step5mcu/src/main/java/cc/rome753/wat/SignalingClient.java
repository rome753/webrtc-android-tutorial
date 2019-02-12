package cc.rome753.wat;

import android.telecom.Call;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by chao on 2019/1/30.
 */

public class SignalingClient {

    private static SignalingClient instance;
    private SignalingClient(){}
    public static SignalingClient get() {
        if(instance == null) {
            synchronized (SignalingClient.class) {
                if(instance == null) {
                    instance = new SignalingClient();
                }
            }
        }
        return instance;
    }

    private Socket socket;
    private String room = "OldPlace";
    private Callback callback;

    private final TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
    };

    public void init(Callback callback) {
        this.callback = callback;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, null);
            IO.setDefaultHostnameVerifier((hostname, session) -> true);
            IO.setDefaultSSLContext(sslContext);

            socket = IO.socket("https://192.168.1.97:8080");
            socket.connect();

            socket.emit("create or join", room);

            socket.on("created", args -> {
                Log.e("chao", "room created:" + socket.id());
                callback.onCreateRoom();
            });
            socket.on("full", args -> {
                Log.e("chao", "room full");
            });
            socket.on("join", args -> {
                Log.e("chao", "peer joined " + Arrays.toString(args));
                callback.onPeerJoined(String.valueOf(args[1]));
            });
            socket.on("joined", args -> {
                Log.e("chao", "self joined:" + socket.id());
                callback.onSelfJoined();
            });
            socket.on("log", args -> {
                Log.e("chao", "log call " + Arrays.toString(args));
            });
            socket.on("bye", args -> {
                Log.e("chao", "bye " + args[0]);
                callback.onPeerLeave((String) args[0]);
            });
            socket.on("message", args -> {
                Log.e("chao", "message " + Arrays.toString(args));
                Object arg = args[0];
                if(arg instanceof String) {

                } else if(arg instanceof JSONObject) {
                    JSONObject data = (JSONObject) arg;
                    String type = data.optString("type");
                    if("offer".equals(type)) {
                        callback.onOfferReceived(data);
                    } else if("answer".equals(type)) {
                        callback.onAnswerReceived(data);
                    } else if("candidate".equals(type)) {
                        callback.onIceCandidateReceived(data);
                    }
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        socket.emit("bye", socket.id());
        socket.disconnect();
        socket.close();
        instance = null;
    }

    public void sendIceCandidate(IceCandidate iceCandidate, String to) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("type", "candidate");
            jo.put("label", iceCandidate.sdpMLineIndex);
            jo.put("id", iceCandidate.sdpMid);
            jo.put("candidate", iceCandidate.sdp);
            jo.put("from", socket.id());
            jo.put("to", to);

            socket.emit("message", jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendSessionDescription(SessionDescription sdp, String to) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("type", sdp.type.canonicalForm());
            jo.put("sdp", sdp.description);
            jo.put("from", socket.id());
            jo.put("to", to);
            socket.emit("message", jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onCreateRoom();
        void onPeerJoined(String socketId);
        void onSelfJoined();
        void onPeerLeave(String msg);

        void onOfferReceived(JSONObject data);
        void onAnswerReceived(JSONObject data);
        void onIceCandidateReceived(JSONObject data);
    }

}
