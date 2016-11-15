package com.zl.wifi.wifisendfile.ui;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zl.wifi.wifisendfile.Config.ConstValue;
import com.zl.wifi.wifisendfile.R;
import com.zl.wifi.wifisendfile.httpserver.ServerRunner;
import com.zl.wifi.wifisendfile.utils.FileAccessUtil;
import com.zl.wifi.wifisendfile.utils.IpUtils;
import com.zl.wifi.wifisendfile.utils.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView addrTv;
    private TextView promptTv;

    private static Boolean isExit = false;
    private static Boolean hasTask = false;
    private Timer tExit;
    private TimerTask task;

    private Handler handler;
    private String IPAddr;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private boolean wifiIsAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initData();
    }

    private void initData() {
        // var used for double click to exit
        tExit = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                isExit = false;
                hasTask = true;
            }
        };

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // get the ip address successfully, start the server
                Bundle bundle = msg.getData();
                Log.i("ip", bundle.getString("ip") + "");
                IPAddr = bundle.getString("ip");
                addrTv.setText(getBrowserAddr());
                promptTv.setText(getBrowserPrompt());
                // start the server
                ServerRunner.startServer(ConstValue.PORT);
                // TODO notifiction
            }
        };

        // create the dir to store the received files
        ConstValue.BASE_DIR = FileAccessUtil.createDir(ConstValue.DIR_NAME);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        // wifi is available?
        wifiIsAvailable = IpUtils.isWifiConnected(wifiManager, wifiInfo);
        if (wifiIsAvailable) {
            // wifi is available, get the ip address in a new thread
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Bundle bundle = new Bundle();
                    bundle.putString("ip",
                            IpUtils.int2Ip(wifiInfo.getIpAddress()));
                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isExit == false) {
                isExit = true;
                ToastUtil
                        .showShortToast(
                                this,
                                getResources().getString(
                                        R.string.double_click_to_exit));
                if (!hasTask) {
                    tExit.schedule(task, 2000);
                }
            } else {
                ServerRunner.stopServer();
                finish();
                System.exit(0);
            }
        }
        return false;
    }
    /**
     * called by the Main Fragment
     *
     * @return
     */
    private String getBrowserAddr() {
        if (wifiIsAvailable) {
            return "http://" + IPAddr + ":" + ConstValue.PORT;
        } else {
            return "";
        }
    }

    /**
     * called by the Main Fragment
     *
     * @return
     */
    private String getBrowserPrompt() {
        int promptId = wifiIsAvailable ? R.string.input_on_browser
                : R.string.wifi_not_avaliable;
        return getString(promptId);
    }

    private void findViews() {
        addrTv = (TextView) findViewById(R.id.addr_tv);
        promptTv = (TextView) findViewById(R.id.main_info_tv);
    }
}
