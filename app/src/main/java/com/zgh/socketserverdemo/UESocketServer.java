package com.zgh.socketserverdemo;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UESocketServer {
    private static final Object GET_INSTANCE_LOCK = new Object();
    private static volatile UESocketServer instance;
    private static final String TAG = UESocketServer.class.getSimpleName();
    private String LOCAL_IP;
    private int LOCAL_SERVER_PORT = 5656;
    private static final int LOCAL_SERVER_BACKLOG = 100;
    private Handler mHandler = new Handler();
    private ConnectedThread mConnectedThread;
    private Context mContext;
    private ExecutorService mSingleThreadExecutor;

    private UESocketServer(Context context) {
        this.mContext = context;
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();

    }

    public static UESocketServer getInstance(Context context) {
        if (instance == null) {
            synchronized (GET_INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new UESocketServer(context);
                }
            }
        }
        return instance;
    }

    public void startServer(String serverIp) {
        LOCAL_IP = serverIp;
        new ServerThread().start();
    }

    public void sendMsg(String content) {
        mSingleThreadExecutor.execute(() -> {
            if (mConnectedThread != null) {
                mConnectedThread.write(content.getBytes());
            }
        });
    }

    class ServerThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                InetAddress inetAddress = InetAddress.getByName(LOCAL_IP);
                ServerSocket mServerSocket = new ServerSocket(LOCAL_SERVER_PORT,
                        LOCAL_SERVER_BACKLOG, inetAddress);
                Log.d(TAG, "ServerThread: local address build success " + LOCAL_IP);
                mHandler.post(() -> Toast.makeText(mContext, "server has started",
                        Toast.LENGTH_SHORT).show());

                Log.d(TAG, "wait for connecting...");
                while (true) {
                    Socket socket = mServerSocket.accept();
                    connected(socket);
                }
            } catch (IOException e) {
                Log.d(TAG, "ServerThread: local address build failed ,will rebuild after " + "3s ");
                mHandler.post(() -> Toast.makeText(mContext, "ServerThread: local address build " +
                        "failed", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void connected(Socket socket) {
        Log.d(TAG, "connected...");
        mHandler.post(() -> Toast.makeText(mContext, "has connected client", Toast.LENGTH_SHORT).show());

        if (mConnectedThread != null) {
            mConnectedThread.close();
            mConnectedThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    class ConnectedThread extends Thread {
        private Socket socket;
        private InputStream inputstream;
        private OutputStream outputstream;

        public ConnectedThread(Socket socket) {
            this.socket = socket;
            try {
                inputstream = socket.getInputStream();
                outputstream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int len;
            while (true) {
                try {
                    if ((len = inputstream.read(buffer)) != -1) {
                        String content = new String(buffer, 0, len);
                        Log.d(TAG, "read data from client: " + content);
                        mHandler.post(() -> Toast.makeText(mContext, content, Toast.LENGTH_LONG).show());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] data) {
            try {
                outputstream.write(data);
                outputstream.flush();
                Log.d(TAG, "write: " + new String(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
