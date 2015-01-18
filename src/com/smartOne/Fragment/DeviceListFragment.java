package com.smartOne.Fragment;

import it.gerdavax.easybluetooth.RemoteDevice;

import com.smartOne.Activity.MainActivity;
import com.smartOne.Adapter.DeviceAdapter;
import com.smartOne.Services.BTService;
import com.smartOne.Services.BTService.ServiceGotListener;
import com.smartOne.korobot.R;








import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListFragment extends Fragment {

	final DeviceAdapter adapter=new DeviceAdapter(){
		@Override
		public View getView(final RemoteDevice device, View convertView,
				ViewGroup arg2) {
			// TODO Auto-generated method stub
			LinearLayout ll=null;
			

			if(convertView==null)
			{
				ll=(LinearLayout) LayoutInflater.from(DeviceListFragment.this.getActivity()).inflate(R.layout.list_view01_item, null);
				convertView = ll;
			}
			else{
				ll = (LinearLayout)convertView;
			}
			
			TextView addressTextView = (TextView) ll.findViewById(R.id.lv01_address);
			TextView nameTextView =  (TextView) ll.findViewById(R.id.lv01_name);
			final LinearLayout llHeader = (LinearLayout)ll.findViewById(R.id.lv01_header);
			final LinearLayout llCtrl = (LinearLayout)ll.findViewById(R.id.lv01_ctrl);
			final Button btnCar = (Button)ll.findViewById(R.id.lv01_car);
			
			String address = device.getAddress();
			String name = device.getFriendlyName();
			
			
			addressTextView.setText(address);
			nameTextView.setText((name==null) ? "NONAME" : name);
			
			
			llHeader.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(llCtrl.getVisibility()==llCtrl.GONE){
						llCtrl.setVisibility(llCtrl.VISIBLE);
					}
					else{
						llCtrl.setVisibility(llCtrl.GONE);
					}
				}
				
				
			});
			
			btnCar.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					BTService.getInstance(v.getContext(),new ServiceGotListener(){
//
//						@Override
//						public void gotOneInstanceService(BTService service) {
//							// TODO Auto-generated method stub
//							service.connectTo(device.getAddress());
//						}
//						
//					});
					//DeviceListFragment.this.getActivity().
					Intent intent = new Intent();
					intent.setAction(MainActivity.ACTION);
					intent.putExtra("what", MainActivity.ACTION_CAR_CONTROL);
					intent.putExtra("address", device.getAddress());
					DeviceListFragment.this.getActivity().sendBroadcast(intent);
					
				}
				
			});
			
			return ll;
		}
	};
	
	
	private void updateAdapter(BTService service){
		adapter.devices.clear();
		for(RemoteDevice device:service.getDevices().values()){
			adapter.devices.add(device);
		}
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
	{
		
		final View view = inflater.inflate(R.layout.device_list, container, false); 	
		final ListView lvDevices = (ListView) view.findViewById(R.id.lv_Devices); 
		lvDevices.setAdapter(adapter);
		
		BTService.getInstance(this.getActivity(), new ServiceGotListener(){
			BroadcastReceiver bcr;
			@Override
			public void gotOneInstanceService(final BTService service) {
				updateAdapter(service);
				
				bcr=new BroadcastReceiver(){

					@Override
					public void onReceive(Context arg0, Intent arg1) {
						// TODO Auto-generated method stub
						if(arg1.getAction()==BTService.ScanCompleteBroadcast){
							updateAdapter(service);
						}
					}
					
					
				};
				IntentFilter filter=new IntentFilter();
				filter.addAction(BTService.ScanCompleteBroadcast);
				DeviceListFragment.this.getActivity().registerReceiver(bcr, filter);
				
			}

			@Override
			protected void finalize() throws Throwable {
				// TODO Auto-generated method stub
				//DeviceListFragment.this.getActivity().unregisterReceiver(bcr);
				super.finalize();
			}
			
			
		});
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

}
