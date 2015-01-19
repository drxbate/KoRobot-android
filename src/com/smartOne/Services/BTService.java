package com.smartOne.Services;

import it.gerdavax.easybluetooth.BtSocket;
import it.gerdavax.easybluetooth.LocalDevice;
import it.gerdavax.easybluetooth.ReadyListener;
import it.gerdavax.easybluetooth.RemoteDevice;
import it.gerdavax.easybluetooth.ScanListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import com.smartOne.Throwable.CannotOpenSocketException;
import com.smartOne.Throwable.NoDeviceFoundExpcetion;
import com.smartOne.Throwable.NoEnsurePairedException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BTService extends Service {
	
	public static final String DeviceSearchingBroadcast="com.Kobot.Services.BTService.DeviceSearchingBroadcast";
	public static final String ScanCompleteBroadcast="com.Kobot.Services.BTService.ScanCompleteBroadcast";
	public static final String DeviceFoundBroadcast="com.Kobot.Services.BTService.DeviceFoundBroadcast";
	public static final String SendOutBroadcast="com.Kobot.Services.BTService.SendOutBroadcast";
	public static final String SendOutErrorBroadcast="com.Kobot.Services.BTService.SendOutErrorBroadcast";
	public static final String RecvInBroadcast="com.Kobot.Services.BTService.RecvInBroadcast";
	
	private static Context context=null;
	private static BTService service=null;
	private static BtServiceConnection conn=null;
	
	private Map<String,RemoteDevice> devices=new HashMap<String,RemoteDevice>();
	private Map<String,BtSocket> sockets=new HashMap<String,BtSocket>();
	
	private LocalDevice localDevice=null;
	
	private BTBinder binder=new BTBinder();
	
	private BroadcastReceiver br=null;
	
	
	public class BTBinder extends Binder{
		public BTService getService(){
			return BTService.this;
		}
		
	}
	
	public static class BtServiceConnection implements ServiceConnection{

		private ServiceGotListener listener=null;
		public BtServiceConnection(ServiceGotListener listener){
			this.listener=listener;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
            BTService.service = ((BTBinder) service).getService();
            BTService.service.scan();
            listener.gotOneInstanceService(BTService.service);			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public interface ServiceGotListener{
		public void gotOneInstanceService(BTService service);
	}
	
	public static void bindService(Context context,final ServiceGotListener listener){
		BTService.context=context;
		conn = new BtServiceConnection(listener);
		Intent intent = new Intent(context,BTService.class);
        if(!context.bindService(intent, conn, Context.BIND_AUTO_CREATE)){
        	Log.w("BTService","not bind service");
        }
        else{
        	Log.w("BTService","bind service success");
        }
	}
	
	public static void unbindService(){
		BTService.context.unbindService(conn);
	}
	
	
	public static void getInstance(Context context,ServiceGotListener listener){
		if(service!=null){
			listener.gotOneInstanceService(service);	
		}
		else{
			bindService(context,listener);
		}
	}
	

	public Map<String,RemoteDevice> getDevices(){
		return this.devices;
	}
	 
	
	public RemoteDevice getDevice(String address){
		return this.devices.get(address);
	}
	
	public boolean connectTo(final String address){
		
		if(this.sockets.containsKey(address)){
			return true;
		}
		
		try {
			final BtSocket socket=BTService.this.getSocket(address);
			if(socket==null){
				return false;
			}
		
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub

		initDevice();
		return binder;
	}
	
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		this.unregisterReceiver(br);
		return super.onUnbind(intent);
	}

	private void initDevice(){
		localDevice=LocalDevice.getInstance();
		localDevice.init(this, new ReadyListener(){

			@Override
			public void ready() {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
				intent.setAction(BTService.DeviceSearchingBroadcast);
				BTService.this.getBaseContext().sendBroadcast(intent);
				
				BTService.this.scan();
			}
			 
		});		
		
		br=new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				String action=arg1.getAction();
				if(action==BTService.SendOutBroadcast){
					Bundle b=arg1.getExtras();
					String address = b.getString("address");
					int msgid = b.getInt("msgid");

					byte[] data = b.getByteArray("data");
					try {
						BTService.this.sendTo(address, data,0,data.length);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Intent intent = new Intent();
						intent.setAction(BTService.SendOutErrorBroadcast);
						intent.putExtra("throwable", e);
						BTService.this.getBaseContext().sendBroadcast(intent);
					}
				}
			}
			
		};

		IntentFilter filter=new IntentFilter();
		filter.addAction(SendOutBroadcast);
		this.registerReceiver(br, filter);
	}

	private void scan(){
		
		localDevice.scan(new ScanListener(){

			@Override
			public void deviceFound(RemoteDevice arg0) {
				// TODO Auto-generated method stub
				String adr=arg0.getAddress();
				BTService.this.devices.put(adr, arg0);
				
				Intent intent=new Intent();
				intent.putExtra("address", adr);
				intent.setAction(BTService.DeviceFoundBroadcast);
				BTService.this.sendBroadcast(intent);
			}

			@Override
			public void scanCompleted() {
				// TODO Auto-generated method stub
				//BroadCast broadCast;
				Intent intent=new Intent();
				intent.setAction(BTService.ScanCompleteBroadcast);
				BTService.this.sendBroadcast(intent);
			}			
		});
	}

	private UUID getDeviceUUID(){		
//		TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE); 
//		final String tmDevice, tmSerial, tmPhone, androidId;    
//		  tmDevice = "" + tm.getDeviceId();   
//		  tmSerial = "" + tm.getSimSerialNumber();    
//		  androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
//		  return  new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		  return  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	}
	
	private BtSocket getSocket(String address) throws NoDeviceFoundExpcetion, NoEnsurePairedException, CannotOpenSocketException{
		if(sockets.containsKey(address)){
			return sockets.get(address);
		}
		
		RemoteDevice device=devices.get(address);
		if(device==null){
			throw new NoDeviceFoundExpcetion();
		}
		
		if(!device.ensurePaired()){
			throw new NoEnsurePairedException();
		}
		
		BtSocket socket;
		try {
			socket = device.openSocket(this.getDeviceUUID());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CannotOpenSocketException();
		}
		
		this.sockets.put(address, socket);
		return socket;
		
	}
	
	Thread tListen=null;
	private void startListen(){
		tListen=new Thread(){			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!this.isInterrupted()){
					for(Entry<String,BtSocket> s:BTService.this.sockets.entrySet()){
						readSocket(s.getKey(),s.getValue());
						try {
							sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							break;
						}
					}
				}
				super.run();
			}
			
			private void readSocket(String address,BtSocket socket){
				try {
					InputStream is = socket.getInputStream();
					if(is.available()>0){
						byte[] head=new byte[1];
						int sz = is.read(head);
						if(head[0]=='+'){
							StringBuffer sb;
							
							ByteArrayOutputStream bos=new ByteArrayOutputStream();
							
							
							byte[] buff=new byte[1024];
							sz = is.read(buff);
							bos.write(buff, 0, sz);
							String cmd = new String(bos.toByteArray(),"utf-8");
							
							Intent intent=new Intent();
							intent.setAction(BTService.RecvInBroadcast);
							intent.putExtra("cmd", cmd);
							intent.putExtra("address", address);
							BTService.this.getBaseContext().sendBroadcast(intent);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
		};
		tListen.start();
		
		
	}
	
	private void sendTo(String address,byte[] data,int offset,int count) throws Exception{
		
		BtSocket s = getSocket(address);
		OutputStream os=s.getOutputStream();
		os.write(data,offset,count);
	}
	
	
}
