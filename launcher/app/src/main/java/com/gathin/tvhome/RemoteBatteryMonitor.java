package com.gathin.tvhome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import java.util.Locale;
import java.util.UUID;

/** Reads the paired remote's standard BLE battery characteristic, without pairing or writing to it. */
@SuppressLint("MissingPermission")
final class RemoteBatteryMonitor {
    private static final UUID SERVICE=UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID LEVEL=UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private final Context context;
    private final TextView label;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private BluetoothGatt connection;
    private BluetoothDevice remote;
    private boolean running, registered;
    private final Runnable refresh=new Runnable(){@Override public void run(){read();if(running)handler.postDelayed(this,300000);}};
    private final Runnable timeout=this::closeConnection;
    RemoteBatteryMonitor(Context context,TextView label){this.context=context;this.label=label;}
    private boolean permitted(){return Build.VERSION.SDK_INT<31||context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;}
    private void show(int level){
        label.setText(level>=0&&level<=100?"Remote  "+level+"%":"Remote  —");
        label.setTextColor(level>=0&&level<=20?0xffffb778:0xffb3b6bf);
        label.setContentDescription(level>=0?"Remote battery "+level+" percent":"Remote battery unavailable");
    }
    void start(){
        if(running)return;running=true;show(-1);
        if(!permitted())return;
        IntentFilter filter=new IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED");
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        if(Build.VERSION.SDK_INT>=33)context.registerReceiver(receiver,filter,Context.RECEIVER_EXPORTED);
        else context.registerReceiver(receiver,filter);
        registered=true;handler.post(refresh);
    }
    void stop(){
        running=false;handler.removeCallbacksAndMessages(null);closeConnection();
        if(registered){context.unregisterReceiver(receiver);registered=false;}
    }
    private final BroadcastReceiver receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent intent){
        BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if(device==null||!device.equals(remote))return;
        if("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED".equals(intent.getAction()))show(intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL",-1));
        else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction()))show(-1);
        else read();
    }};
    private void read(){
        if(!running||!permitted()||connection!=null)return;
        try{
            BluetoothManager manager=context.getSystemService(BluetoothManager.class);
            if(manager==null||manager.getAdapter()==null||!manager.getAdapter().isEnabled()){show(-1);return;}
            remote=null;
            for(BluetoothDevice device:manager.getAdapter().getBondedDevices()){
                String name=device.getName();String lower=name==null?"":name.toLowerCase(Locale.ROOT);
                if(lower.contains("remote")||lower.contains("tv-rc")||lower.endsWith("-rc")){remote=device;break;}
            }
            if(remote==null){show(-1);return;}
            connection=remote.connectGatt(context,false,callback,BluetoothDevice.TRANSPORT_LE);
            handler.postDelayed(timeout,12000);
        }catch(SecurityException|IllegalStateException e){show(-1);closeConnection();}
    }
    private final BluetoothGattCallback callback=new BluetoothGattCallback(){
        @Override public void onConnectionStateChange(BluetoothGatt g,int status,int state){handler.post(()->{
            if(!running||g!=connection){g.close();return;}
            if(status==BluetoothGatt.GATT_SUCCESS&&state==BluetoothProfile.STATE_CONNECTED)g.discoverServices();
            else if(state==BluetoothProfile.STATE_DISCONNECTED||status!=BluetoothGatt.GATT_SUCCESS)closeConnection();
        });}
        @Override public void onServicesDiscovered(BluetoothGatt g,int status){handler.post(()->{
            if(!running||g!=connection)return;
            BluetoothGattService service=status==BluetoothGatt.GATT_SUCCESS?g.getService(SERVICE):null;
            BluetoothGattCharacteristic value=service==null?null:service.getCharacteristic(LEVEL);
            if(value==null||!g.readCharacteristic(value))closeConnection();
        });}
        @Override public void onCharacteristicRead(BluetoothGatt g,BluetoothGattCharacteristic characteristic,int status){
            update(g,characteristic.getUuid(),characteristic.getValue(),status);
        }
        @Override public void onCharacteristicRead(BluetoothGatt g,BluetoothGattCharacteristic characteristic,byte[] value,int status){
            update(g,characteristic.getUuid(),value,status);
        }
    };
    private void update(BluetoothGatt g,UUID characteristic,byte[] value,int status){handler.post(()->{
        if(!running||g!=connection)return;
        if(LEVEL.equals(characteristic)&&status==BluetoothGatt.GATT_SUCCESS&&value!=null&&value.length>0)show(value[0]&255);
        closeConnection();
    });}
    private void closeConnection(){handler.removeCallbacks(timeout);BluetoothGatt old=connection;connection=null;if(old!=null)old.close();}
}
