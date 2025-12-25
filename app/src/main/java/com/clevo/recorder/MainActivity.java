package com.clevo.recorder;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.clevo.recorder.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private MyLocalServer server;
    private MyLocalClient client;

    @Override
    protected ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        server = new MyLocalServer();
        client = new MyLocalClient();

        binding.btStartServer.setOnClickListener(v -> server.start());
        binding.btSendMsgToClient.setOnClickListener(v -> server.sendMessage("Hello Client!"));
        binding.btConnectServer.setOnClickListener(v -> client.start());
        binding.btSendMsgToServer.setOnClickListener(v -> client.sendMessage("Hello Server!"));
    }

    @Override
    protected void onDestroy() {
        if (server != null && server.isAlive()) {
            server.stopServer();
        }
        if (client != null && client.isAlive()) {
            client.stopClient();
        }
        super.onDestroy();
    }
}