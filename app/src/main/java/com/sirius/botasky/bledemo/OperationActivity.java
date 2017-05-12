package com.sirius.botasky.bledemo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sirius.botasky.bledemo.callbacks.OperationResultCallback;
import com.sirius.botasky.cyberble.ble.BleDeviceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sirius.botasky.bledemo.R.id.btn_write;

public class OperationActivity extends AppCompatActivity {
    private Button btnDisconnect, btnWrite;
    private EditText etWriteData;
    private TextView tvDataDisplay;
    private ExpandableListView mServiceList;
    private BleManager mBleManager;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            //如果当前有一个NOTIFY的特征正在活动，要先关闭NOTIFY特征，再去开启READ特征
                            Toast.makeText(OperationActivity.this, "这是一个READ特征，开启读特征", Toast.LENGTH_SHORT).show();
                            //开启读特征
                            mBleManager.startReadCharacteristic(characteristic.getUuid());
                        }
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            Toast.makeText(OperationActivity.this, "这是一个NOTIFY特征，开启读特征", Toast.LENGTH_SHORT).show();
                            mBleManager.startNotifyCharacteristic(characteristic.getUuid());
                        }
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
                                && (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                            Toast.makeText(OperationActivity.this, "这是一个WRITE特征", Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBleManager = BleManager.getInstance();
        mBleManager.discoverCurrentDeviceServices();

        btnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleManager.disconnect();
                mBleManager.setmOperationResultCallback(null);
                OperationActivity.this.finish();
            }
        });

        btnWrite = (Button) findViewById(btn_write);
        etWriteData = (EditText) findViewById(R.id.et_write);
        tvDataDisplay = (TextView) findViewById(R.id.tv_data_display);
        mServiceList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mServiceList.setOnChildClickListener(servicesListClickListner);

        mBleManager.setmOperationResultCallback(new OperationResultCallback() {
            @Override
            public void onDeviceDiscoverService(String address, List<BluetoothGattService> services) {
                displayGattServices(services);
            }

            @Override
            public void onDeviceReadResult(String address, final BluetoothGattCharacteristic characteristic) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final byte[] data = characteristic.getValue();
                        if (data != null && data.length > 0) {
                            final StringBuilder stringBuilder = new StringBuilder(data.length);
                            for (byte byteChar : data) {
                                stringBuilder.append(String.format("%02X ", byteChar));
                            }
                            tvDataDisplay.setText(new String(data) + "\n" + stringBuilder.toString());
                        }
                    }
                });
            }

            @Override
            public void onDeviceWriteResult(String address, boolean isSuccess) {

            }

            @Override
            public void onDevoiceNotifyData(String address, BluetoothGattCharacteristic characteristic) {

            }
        });
    }


    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    /**
     * 将获取到的服务展示出来，根据SampleGattAttributes对比，看有；没有符合的服务
     */
    private void displayGattServices(List<BluetoothGattService> services) {

        if (services == null) return;
        String uuid = null;
        String unknownServiceString = "未知服务";
        String unknownCharaString = "未知特征";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        for (BluetoothGattService gattService : services) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        final SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mServiceList.setAdapter(gattServiceAdapter);

            }
        });
    }
}
