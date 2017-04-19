# CyberBle

## 1.效果



![效果](http://oktzkaa8p.bkt.clouddn.com/BleDemo.gif)

效果地址，GITHUB显示不出来不知道什么鬼

http://oktzkaa8p.bkt.clouddn.com/BleDemo.gif 



## 2.使用

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







### 3. 借鉴

[BleBus](https://github.com/backav/android-ble-bus)

[FastBle](https://github.com/Jasonchenlijian/FastBle)

