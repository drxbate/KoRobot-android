package com.smartOne.Fragment;

import com.smartOne.Services.BTService;
import com.smartOne.Services.BTService.ServiceGotListener;
import com.smartOne.korobot.R;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CarFragment extends Fragment implements SensorEventListener {

	SensorManager sensorMgr=null;
	Sensor sensor=null;
	String address=null;
	int speed=5;
	TextView tvXR,tvYR,tvZR,tvSpeed;
	int state=0;
	final int STATE_FORWARD=0;
	final int STATE_BACKWARD=1;
	final int STATE_STOP=2;
	final int STATE_ROTATE_LEFT=3;
	final int STATE_ROTATE_RIGHT=4;
	View.OnClickListener upclick=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			speed+=(speed==9?0:1);
			String cmd = ""+speed;
			CarFragment.this.sendCommand(cmd);
			tvSpeed.setText("Current Speed:"+speed);
		}
		
	};
	
	View.OnClickListener downclick=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			speed+=(speed==1?0:-1);
			String cmd = ""+speed;
			CarFragment.this.sendCommand(cmd);
			tvSpeed.setText("Current Speed:"+speed);
		}
		
	};
	
	View.OnClickListener goaheadclick=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			state=STATE_FORWARD;
			CarFragment.this.sendCommand("i");
		}
		
	};
	View.OnClickListener stopclick=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			state=STATE_STOP;
			CarFragment.this.sendCommand("0");
		
		}
		
	};
	
	View.OnClickListener backclick=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			state=STATE_BACKWARD;
			CarFragment.this.sendCommand("k");
		}
		
	};
	
	View.OnClickListener rotateLeftclick=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			state=STATE_ROTATE_LEFT;
			CarFragment.this.sendCommand("iu");
		}
		
	};
	
	View.OnClickListener rotateRightclick=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			state=STATE_ROTATE_RIGHT;
			CarFragment.this.sendCommand("io");
		}
		
	};
	
	public CarFragment(){
		
		
	}
	
	private void sendCommand(String cmd){
		Intent intent=new Intent();
		intent.setAction(BTService.SendOutBroadcast);
		intent.putExtra("address", address);
		intent.putExtra("data", cmd.getBytes());
		this.getActivity().sendBroadcast(intent);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Bundle args=savedInstanceState==null?this.getArguments():savedInstanceState;
		address = args.getString("address");
		View vw=inflater.inflate(R.layout.ll_car, container,false);
		
		Button btnUp=(Button) vw.findViewById(R.id.btn_up);
		btnUp.setOnClickListener(this.upclick);
		
		Button btnDown=(Button) vw.findViewById(R.id.btn_down);
		btnDown.setOnClickListener(this.downclick);
		
		Button btnStop=(Button) vw.findViewById(R.id.btn_stop);
		btnStop.setOnClickListener(this.stopclick);
		
		Button btnBack=(Button) vw.findViewById(R.id.btn_back);
		btnBack.setOnClickListener(this.backclick);
		
		Button btnGoAhead=(Button) vw.findViewById(R.id.btn_go_ahead);
		btnGoAhead.setOnClickListener(this.goaheadclick);
		
		Button btnRotateLeft=(Button) vw.findViewById(R.id.btn_RotateLeft);
		btnRotateLeft.setOnClickListener(this.rotateLeftclick);
		
		Button btnRotateRight=(Button) vw.findViewById(R.id.btn_RotateRight);
		btnRotateRight.setOnClickListener(this.rotateRightclick);
		
		tvSpeed=(TextView) vw.findViewById(R.id.tv_speed);
		
		tvXR = (TextView) vw.findViewById(R.id.tv_x_result);
		tvYR = (TextView) vw.findViewById(R.id.tv_y_result);
		tvZR = (TextView) vw.findViewById(R.id.tv_z_result);
		
		return vw;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		
		sensorMgr = (SensorManager) this.getActivity().getSystemService(this.getActivity().SENSOR_SERVICE);
		sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorMgr.registerListener(this, sensor,SensorManager.SENSOR_DELAY_GAME);
		
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		/*x>0 and x <10 0~180,x<0 and x>-10 180~360
		 * y>0 and y<10 left up right down
		 * z
		 * */
		if(state!=STATE_FORWARD&&state!=STATE_BACKWARD){
			//if speed==9 then keep-stop
			return;
		}
		float vals[]=event.values;
		int x = Math.round(vals[0]);
		int y = Math.round(vals[1]);
		int z = Math.round(vals[2]);
		
		tvXR.setText(""+x);
		tvYR.setText(""+y);
		tvZR.setText(""+z);
		
//		if(x<=5){
			if(y<-1)
			{
				this.sendCommand("j");
			}
			else if(y>1)
			{
				this.sendCommand("l");
			}
			else{
				this.sendCommand(state==STATE_FORWARD?"i":"k");
			}
//		}
//		else{
//			this.sendCommand("k");
//		}
		
		
		
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		sensorMgr.unregisterListener(this);
		
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState = this.getArguments();
		super.onSaveInstanceState(outState);
	}

	
}
