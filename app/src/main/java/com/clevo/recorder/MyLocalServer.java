package com.clevo.recorder;

import static com.clevo.recorder.socket.SocketConstants.SOCKET_NAME;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class MyLocalServer extends Thread {
    private static final String TAG = "TestLocalSocket";
    private LocalServerSocket serverSocket;
    private boolean isRunning = true;
    private PrintWriter pw;

    @Override
    public void run() {
        try {
            serverSocket = new LocalServerSocket(SOCKET_NAME);
            Log.e(TAG, "Server: 启动监听...");

            while (isRunning) {
                LocalSocket clientSocket = serverSocket.accept();
                Log.e(TAG, "Server: 客户端已连接");

                handleClient(clientSocket);
            }
        } catch (IOException e) {
            String msg = e.getMessage();
            Log.e(TAG, "Server: " + (!TextUtils.isEmpty(msg) ? msg : "启动监听失败"));
        }
    }

    private void handleClient(LocalSocket clientSocket) {
        try (clientSocket) {
            try {
                pw = new PrintWriter(clientSocket.getOutputStream(), true); // true 表示自动 flush
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    Log.e(TAG, "Server: 收到消息" + line);

                    pw.println("Server已经收到消息: " + line);
                }
            } catch (IOException e) {
                String msg = e.getMessage();
                Log.e(TAG, "Server: " + (!TextUtils.isEmpty(msg) ? msg : "接收或发送消息失败"));
            }
        } catch (IOException e) {
            Log.e(TAG, "Server: 客户端已断开连接");
        }
    }

    public void sendMessage(String message) {
        if (pw != null) {
            try {
                pw.println(message);
                Log.e(TAG, "Server: 发送成功 -> " + message);
            } catch (Exception e) {
                String msg = e.getMessage();
                Log.e(TAG, "Server: " + (!TextUtils.isEmpty(msg) ? msg : "发送消息失败"));
            }
        } else {
            Log.e(TAG, "Server: 写入通道不存在");
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
