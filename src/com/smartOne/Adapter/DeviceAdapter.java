package com.smartOne.Adapter;

import it.gerdavax.easybluetooth.RemoteDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.smartOne.Services.BTService;





import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DeviceAdapter extends BaseAdapter {

	
	public List<RemoteDevice> devices=new ArrayList<RemoteDevice>();

	public DeviceAdapter(){
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		Log.d("BTService","DeviceAdapter:getCount"+devices.size());
		return devices.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		Log.d("BTService","DeviceAdapter:GetItem");
		return devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		Log.d("BTService","DeviceAdapter:GetItemId");
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.d("BTService","DeviceAdapter:GetView");
		return getView(devices.get(position),convertView,parent);
	}
	
	public abstract View getView(final RemoteDevice device, View convertView, ViewGroup arg2);

}
