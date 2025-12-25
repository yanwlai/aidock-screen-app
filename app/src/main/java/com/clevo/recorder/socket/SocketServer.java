package com.clevo.recorder.socket;

import static com.clevo.recorder.socket.SocketConstants.SOCKET_NAME;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;

public class SocketServer {
    private static final String TAG = "SocketServer";
    private LocalServerSocket serverSocket;
    private LocalSocket clientSocket;
    private boolean isRunning;

    public void init() {
        try {
            serverSocket = new LocalServerSocket(SOCKET_NAME);
            isRunning = true;

            while (isRunning) {
                clientSocket = serverSocket.accept();
            }
        } catch (IOException e) {
            Log.e(TAG, "Server socket: 启动监听失败, " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Server socket: 关闭失败, " + e.getMessage());
            }
        }
    }

    public FileDescriptor getFileDescriptor() {
        if (clientSocket != null) {
            return clientSocket.getFileDescriptor();
        }
        return null;
    }

    public void clear() {
        isRunning = false;
    }
}
