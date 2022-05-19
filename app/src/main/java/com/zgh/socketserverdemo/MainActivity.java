package com.zgh.socketserverdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtIP;
    private EditText mEtContent;
    public static final String TAG = "SocketServer";
    private UESocketServer mSocketServer;
    private String mLocalIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start_server).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        mEtIP = findViewById(R.id.edit_ip);
        mEtContent = findViewById(R.id.edit_content);
        mSocketServer = UESocketServer.getInstance(this);
        mLocalIP = NetUtils.getIPAddress(true);
        if (!TextUtils.isEmpty(mLocalIP)) {
            mEtIP.setText(mLocalIP);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_server:
                String serverIP = mEtIP.getText().toString().trim();
                if (serverIP.isEmpty()) {
                    Toast.makeText(this, "请输入服务器IP", Toast.LENGTH_SHORT).show();
                } else {
                    mSocketServer.startServer(serverIP);
                }
                break;
            case R.id.btn_send:
                String content = mEtContent.getText().toString().trim();
                if (!content.isEmpty()) {
                    mSocketServer.sendMsg(content);
                    mEtContent.getText().clear();
                }
                break;

            default:
                break;
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}