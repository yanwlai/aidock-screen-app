package com.clevo.recorder;

import static com.clevo.recorder.socket.SocketConstants.SOCKET_NAME;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class MyLocalClient extends Thread {
    private static final String TAG = "TestLocalSocket";
    private LocalSocket clientSocket;
    private PrintWriter pw;

    @Override
    public void run() {
        try {
            clientSocket = new LocalSocket();
            // 1. 连接到指定名称的服务端
            // 使用 Namespace.ABSTRACT (推荐)，不需要文件系统权限
            LocalSocketAddress address = new LocalSocketAddress(
                    SOCKET_NAME,
                    LocalSocketAddress.Namespace.ABSTRACT
            );
            clientSocket.connect(address);

            handleClient();
        } catch (IOException e) {
            Log.e(TAG, "Client: 连接失败或发送错误");
            e.printStackTrace();
        } finally {
            // 3. 关闭连接
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient() {
        try {
            pw = new PrintWriter(clientSocket.getOutputStream(), true); // true 表示自动 flush
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                Log.e(TAG, "Client 收到消息: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (pw != null) {
            pw.println(message);
            Log.e(TAG, "Client: 发送成功 -> " + message);
        }
    }

    public void stopClient() {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
