# CyberBle

## 1.效果

### 1.1Scan->Connect->Read Operation->Disconnect



![效果](https://github.com/iBotasky/BleDemo/blob/master/BleDemo.gif)
### 1.2 Write Operation->Notfiy Operation ->Disconnect
暂无





## 2.使用

### 2.0 添加依赖

```groovy
repositories {
    jcenter() //使用jcenter
    mavenCentral(); //使用maven
    //maven远程仓库地址
    maven {
        url 'https://dl.bintray.com/botasky/maven'
    }
}

dependencies {
    compile 'com.sirius.botasky:cyberble:1.0.1'
}
```





### 2.1 类说明

> BleAdmin.java ,蓝牙设备控制类，用来做蓝牙设备管理，扫描等

> BleOperator.java 对应一个蓝牙设备，管理设备的READ，INDICATE，NOTIFY，WRITE，CONNECT，DISCONNECT等操作

> BleDeviceService.java 用来描述蓝牙READ,INDICATE,NOTIFY,WRITE这四个功能的类

> callback 下面的类主要是控制一些回调

### 2.2 使用

#### 2.2.1 先初始化BleAdmin

```java
//mDeviceCallBack是蓝牙连接状态回调
//mDegviceOperationCallback是蓝牙操作的回调，WRITE，NOTIFY等操作的回调
mBleAdmin = new BleAdmin(this, mDeviceCallBack, mDeviceOperationCallback);
```



#### 2.2.2 SCAN

```Java
//打开蓝牙
mBleAdmin.openBle();
//开始搜索，设置搜索回调
mBleAdmin.startScanAllDevice(new ScanCallback() {
    @Override
    public void onDeviceFound(final List<BluetoothDevice> devices) {
        Log.e("Found", " " + devices.size());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //显示设备要在主线程
                mRecyclerAdapter.setDevices(devices);
            }
        });

    }
});
```



#### 2.2.3 Connect

```java
            viewHolder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  
                    mCurrentDeviceAddress = device.getAddress();
                    //mBleAdmin.connectDevice(device.getAddress());用地址直接连接
                    mBleAdmin.connectDevice(device);//用搜索到的设备直接连接, 推荐
                }
            });
```





#### 2.2.4 Read

```java
//开启READ操作
mBleAdmin.processDeviceService(new BleDeviceService(
        mCurrentDeviceAddress, //已连接的要开启读操作蓝牙的地址
        characteristic.getUuid(), //读操作的特征值
        BleDeviceService.OperateType.Read));//设置OperationType为READ

```



#### 2.2.5 Notify

```java
//开启Notify操作
mBleAdmin.processDeviceService(new BleDeviceService(
		mCurrentDeviceAddress, //开启Notify的已连接上的蓝牙的地址
		characteristic.getUuid(), //Notify操作的特征值
		BleDeviceService.OperateType.Notify));//设置OperationType为NOTIFY
```



#### 2.2.6 Write/Indicate

```java
mWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleAdmin.processDeviceService(new BleDeviceService(
                        mCurrentDeviceAddress,//开启Write的已连接上的蓝牙的地址
                        UUID.fromString(SampleGattAttributes.INSOLE_WRITE),//Write的UUID特征值
                        BleDeviceService.OperateType.Write,//OperationType
                        mEditData.getText().toString().getBytes()));//要写入的byte[]值
            }
        });
```



#### 2.2.7 Diconnect

```Java
 mDisconnecte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              	//mBleAdmin.disconnectDevice(mCurrentDevce);//或者直接传入设备
                mBleAdmin.disconnectDevice(mCurrentDeviceAddress);//直接传入已连接上的蓝牙设备地址
            }
        });
```



### 2.3回调（都要在主线程运行）

#### 2.3.1 连接状态回调

```java
private DeviceConnectStateCallback mDeviceCallBack = new DeviceConnectStateCallback() {
    @Override
    public void onDeviceConnected(final String address) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //连接成功后去发现设备的服务。停止搜索
                mBleAdmin.discoverDeviceServices(address);
                mBleAdmin.stopScan();
                discover.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
              //设备断开连接的回调
                discover.setVisibility(View.VISIBLE);
                connect.setVisibility(View.GONE);
            }
        });

    }
};
```



#### 2.3.2 DiscoveryServices/READ/WRITE/NOTIFY结果回调，都是只有成功才返回,这些接口设计还有一些不完善的地方。

```java
private DeviceOperationCallback mDeviceOperationCallback = new DeviceOperationCallback() {
    @Override
    public void onDeviceServiceDiscover(String deviceAddress, List<BluetoothGattService> services) {
      //发现服务回调
        displayGattServices(services);
    }

    @Override
    public void onDeviceCharacteristicRead(String deviceAddress, BluetoothGattCharacteristic characteristic) {
      //读操作回调
        displayData(characteristic);
    }

    @Override
    public void onDeviceCharacteristicWrite(String deviceAddress) {
		//写操作回调
    }

    @Override
    public void onDeviceCharacteristicNotify(String deviceAddress, BluetoothGattCharacteristic characteristic) {
      //Notify得到的回调
        displayData(characteristic);

    }
};
```








### 3. 借鉴

[BleBus](https://github.com/backav/android-ble-bus)

[FastBle](https://github.com/Jasonchenlijian/FastBle)

