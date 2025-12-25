package com.clevo.recorder;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    protected String TAG;
    protected VB binding;

    public void darkToolBar() {
        SystemUtils.setStatusBarColor(this, true);
    }

    public void lightToolBar() {
        SystemUtils.setStatusBarColor(this, false);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        darkToolBar();

        TAG = getClass().getSimpleName();
        binding = getViewBinding();
        setContentView(binding.getRoot());

        if (!notFillStatusBar()) {
            View vRoot = findViewById(R.id.layout_root);
            if (vRoot != null) {
                ViewCompat.setOnApplyWindowInsetsListener(vRoot, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }
        }

        init(savedInstanceState);
    }

    protected abstract VB getViewBinding();

    protected boolean notFillStatusBar() {
        return false;
    }

    protected abstract void init(@Nullable Bundle savedInstanceState);
}
