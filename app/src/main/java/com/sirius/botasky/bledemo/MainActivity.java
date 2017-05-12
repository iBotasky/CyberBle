package com.sirius.botasky.bledemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sirius.botasky.bledemo.callbacks.ConnectResultCallback;
import com.sirius.botasky.bledemo.callbacks.OperationResultCallback;
import com.sirius.botasky.cyberble.callback.ScanCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button mBtnStartScan, mBtnStopScan;
    private RecyclerView mRvDevicesList;
    private BleManager mBleManager;
    private String mCurrentDeviceAddress;
    private BleDeviceAdapter mRecyclerAdapter;

    private ProgressDialog mConnectingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //蓝牙管理单例初始化
        BleManager.init(getApplicationContext());
        mBleManager = BleManager.getInstance();

        //设置监听
        mBleManager.setmConnectResultCallback(new ConnectResultCallback() {
            @Override
            public void connectResult(final String results, final boolean isConnect) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, results, Toast.LENGTH_SHORT).show();
                        if (mConnectingDialog.isShowing()) {
                            mConnectingDialog.dismiss();
                        }
                        if (isConnect){
                            startActivity(new Intent(MainActivity.this, OperationActivity.class));
                        }
                    }
                });

            }
        });



        mBtnStartScan = (Button) findViewById(R.id.btn_start_scan);
        mBtnStopScan = (Button) findViewById(R.id.btn_stop_scan);
        mRvDevicesList = (RecyclerView) findViewById(R.id.rv_devices);

        mConnectingDialog = new ProgressDialog(this);
        mConnectingDialog.setCancelable(true);
        mConnectingDialog.setMessage("加载中...");
        mConnectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mBleManager.disconnect(mCurrentDeviceAddress);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvDevicesList.setLayoutManager(linearLayoutManager);
        mRecyclerAdapter = new BleDeviceAdapter();
        mRvDevicesList.setAdapter(mRecyclerAdapter);
        //开始搜索
        mBtnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        //停止搜索
        mBtnStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
            }
        });

    }

    /**
     * 开始搜索
     */
    private void startScan(){
        mBleManager.startScan(new ScanCallback() {
            @Override
            public void onDeviceFound(final List<BluetoothDevice> devices) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerAdapter.setDevices(devices);
                    }
                });
            }
        });
    }

    /**
     * 停止搜索
     */
    private void stopScan(){
        mBleManager.stopScan();
    }




    private class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {
        private List<BluetoothDevice> devices;
        private LayoutInflater mInflator;
        private Map<String, Integer> mDevicesRssi;

        public BleDeviceAdapter() {
            this.devices = new ArrayList<>();
            this.mDevicesRssi = new HashMap<>();
            this.mInflator = MainActivity.this.getLayoutInflater();
        }

        private void setDevices(List<BluetoothDevice> devices) {
            this.devices = devices;
            notifyDataSetChanged();
        }


        @Override
        public BleDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflator.inflate(R.layout.item_device, parent, false);
            BleDeviceAdapter.ViewHolder item = new BleDeviceAdapter.ViewHolder(view);
            return item;
        }

        @Override
        public void onBindViewHolder(BleDeviceAdapter.ViewHolder holder, final int position) {
            BleDeviceAdapter.ViewHolder viewHolder = ((BleDeviceAdapter.ViewHolder) holder);
            final BluetoothDevice device = devices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknow");
            viewHolder.deviceAddress.setText(device.getAddress());


            viewHolder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentDeviceAddress = device.getAddress();
                    mBleManager.connect(device);
                    mConnectingDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout content;
            private TextView deviceName;
            private TextView deviceAddress;

            public ViewHolder(View itemView) {
                super(itemView);
                content = ((LinearLayout) itemView.findViewById(R.id.content));
                deviceAddress = ((TextView) itemView.findViewById(R.id.device_address));
                deviceName = (TextView) itemView.findViewById(R.id.device_name);
            }
        }
    }
}
