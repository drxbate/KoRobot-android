package com.smartOne.Activity;

import java.util.LinkedList;
import java.util.Queue;

import com.smartOne.Fragment.CarFragment;
import com.smartOne.Fragment.DeviceListFragment;
import com.smartOne.Services.BTService;
import com.smartOne.korobot.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class MainActivity extends FragmentActivity {

	public static final String ACTION="DO_ACTION";
	
	public static final int ACTION_DEVICE_PICK=0x01;
	public static final int ACTION_CAR_CONTROL=0x02;
	
	FragmentManager fm=null;
	Queue<Integer> currentAction=new LinkedList<Integer>();
	
	private BroadcastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fm = this.getSupportFragmentManager();
			
		
		doAction(ACTION_DEVICE_PICK,null);	
		
		receiver=new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
			
				String action = intent.getAction();
				if(action == BTService.DeviceSearchingBroadcast){
					Log.d("BTService","Searching...");
				}
				else if(action==ACTION){
					Bundle bundle=intent.getExtras();
					int what = bundle.getInt("what");
					MainActivity.this.doAction(what,bundle);
				}
			}
			
		};
		
		IntentFilter filter=new IntentFilter();
		filter.addAction(BTService.DeviceSearchingBroadcast);
		filter.addAction(ACTION);
		this.registerReceiver(receiver, filter);
	}
	
	private void doAction(int action,Bundle args){
		FragmentTransaction trans = fm.beginTransaction();
		Fragment f=null;
		if(action==ACTION_DEVICE_PICK){
			f = new DeviceListFragment();
		}
		else if(action==ACTION_CAR_CONTROL){
			f = new CarFragment(args);
		}
		
		if(currentAction.size()==0){
			fm.beginTransaction().add(R.id.llCenter,f, "").commit();
		}
		else{
			fm.beginTransaction().replace(R.id.llCenter,f, "").commit();
		}
		
		currentAction.add(action);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}


}
