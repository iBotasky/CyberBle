package com.sirius.botasky.bledemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DiscoverActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private Button mScanButton;
    private RecyclerView mDevicesRecycler;

    private BluetoothAdapter mBleAdapter;
    private BleDeviceAdapter mRecyclerAdapter;

    private boolean isScaning = false;

    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupViews();
        getBleAdapter();
        setupPermissions();

    }




    /**
     * 获取BluetoothAdapter
     */
    private void getBleAdapter() {
        //获取BluetoothManager， 再从BluetoothManager获取适配器，需要的话去判断蓝牙是否可用或者蓝牙是否打开
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = bluetoothManager.getAdapter();

    }

    /**
     * 判断是否打开蓝牙
     */
    private boolean enableBle() {
        if (!mBleAdapter.isEnabled()) {
            if (!mBleAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            startScan();
        } else {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 基础view
     */
    private void setupViews() {
        mScanButton = (Button) findViewById(R.id.start_scan);
        mDevicesRecycler = ((RecyclerView) findViewById(R.id.device_list));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mDevicesRecycler.setLayoutManager(linearLayoutManager);
        mRecyclerAdapter = new BleDeviceAdapter();
        mDevicesRecycler.setAdapter(mRecyclerAdapter);

        //开始扫描
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });


    }

    /**
     * 动态获取权限
     */
    private void setupPermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN)
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            startScan();
                        } else {
                            return;
                        }
                    }
                });
    }

    private void startScan() {
        if (!enableBle()) {
            return;
        }
        //判断权限是否都有
        RxPermissions rxPermissions = new RxPermissions(this);
        if (rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
                && rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                && rxPermissions.isGranted(Manifest.permission.BLUETOOTH)
                && rxPermissions.isGranted(Manifest.permission.BLUETOOTH_ADMIN)) {
            if (isScaning) {
                Snackbar.make(mScanButton, " is scanning now ", Snackbar.LENGTH_LONG).show();
                return;
            }
            mRecyclerAdapter.clear();
            UUID[] insole = {UUID.fromString(SampleGattAttributes.UUID_MEASUREMENT)};

            mBleAdapter.startLeScan(insole, this);
            isScaning = true;
            Observable.timer(10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Long>() {
                        @Override
                        public void onCompleted() {
                            Log.e("Timer onComplete", " complete");
                            mBleAdapter.stopLeScan(DiscoverActivity.this);
                            isScaning = false;
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("Timer onError", " error  " + e);
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Long aLong) {
                            Log.e("Timer onNext", " long " + aLong);
                        }
                    });
        } else {
            setupPermissions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_discover, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
        Log.e("Timer device", " device " + device.getAddress());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerAdapter.addDevice(device, rssi);
            }
        });
    }


    private class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {
        private List<BluetoothDevice> devices;
        private LayoutInflater mInflator;
        private Map<String, Integer> mDevicesRssi;

        public BleDeviceAdapter() {
            this.devices = new ArrayList<>();
            this.mDevicesRssi = new HashMap<>();
            this.mInflator = DiscoverActivity.this.getLayoutInflater();
        }

        private void addDevice(BluetoothDevice device, int rssi) {
            if (!devices.contains(device)) {
                devices.add(device);
                mDevicesRssi.put(device.getAddress(), rssi);
                notifyDataSetChanged();
            }
        }

        private void clear() {
            devices.clear();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflator.inflate(R.layout.item_device, parent, false);
            ViewHolder item = new ViewHolder(view);
            return item;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ViewHolder viewHolder = ((ViewHolder) holder);
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
                    Intent intent = new Intent(DiscoverActivity.this, ConnectActivity.class);
                    intent.putExtra(ConnectActivity.DEVICE_ADDRESS, device.getAddress());
                    intent.putExtra(ConnectActivity.DEVICE_NAME, device.getName());
                    startActivity(intent);
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
