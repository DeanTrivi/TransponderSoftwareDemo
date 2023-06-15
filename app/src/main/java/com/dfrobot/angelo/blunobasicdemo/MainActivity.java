package com.dfrobot.angelo.blunobasicdemo;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;
	private Button buttonSerialSend;
	private Button buttonArm;
	private Button buttonDisarm;
	private Button buttonConnectionStatus;
	private Button buttonArmedStatus;
	private Button buttonEngagedStatus;
	private Button buttonAlarmStatus;
	private EditText serialSendText;
	private TextView serialReceivedText;
	private TextView serialStatusText;
	private ColorStateList redStateList;
	private ColorStateList greenStateList;
	private ColorStateList yellowStateList;
	private ColorStateList grayStateList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		request(1000, new OnPermissionsResult() {
			@Override
			public void OnSuccess() {
				Toast.makeText(MainActivity.this,"权限请求成功",Toast.LENGTH_SHORT).show();
			}

			@Override
			public void OnFail(List<String> noPermissions) {
				Toast.makeText(MainActivity.this,"权限请求失败",Toast.LENGTH_SHORT).show();
			}
		});

        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
        serialReceivedText.setVisibility(View.GONE);
		serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data
		serialSendText.setVisibility(View.GONE);

        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
        buttonSerialSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
				//System.out.println(serialSendText.getText().toString());
			}
		});
		buttonSerialSend.setVisibility(View.GONE);

        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
			}
		});

		// Find the "ARM" button
		buttonArm = findViewById(R.id.buttonArm);
		buttonArm.setVisibility(View.GONE);
		buttonArm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Send "arm" to the BLUNO
				serialSend("arm");
			}
		});

		// Find the "DISARM" button
		buttonDisarm = findViewById(R.id.buttonDisarm);
		buttonDisarm.setVisibility(View.GONE);
		buttonDisarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Send "disarm" to the BLUNO
				serialSend("disarm");
			}
		});

		buttonConnectionStatus = (Button) findViewById(R.id.buttonConnectionStatus);
		buttonArmedStatus = (Button) findViewById(R.id.buttonArmedStatus);
		buttonEngagedStatus = (Button) findViewById(R.id.buttonEngagedStatus);
		buttonAlarmStatus = (Button) findViewById(R.id.buttonAlarmStatus);

		redStateList = ColorStateList.valueOf(Color.RED);
		greenStateList = ColorStateList.valueOf(Color.GREEN);
		yellowStateList = ColorStateList.valueOf(Color.YELLOW);
		grayStateList = ColorStateList.valueOf(Color.GRAY);
	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConnectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
			buttonConnectionStatus.setBackgroundTintList(greenStateList);
			buttonConnectionStatus.setText("Connected");
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			buttonConnectionStatus.setBackgroundTintList(yellowStateList);
			buttonConnectionStatus.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
			buttonConnectionStatus.setBackgroundTintList(grayStateList);
			buttonConnectionStatus.setText("Disconnected");
			buttonArmedStatus.setBackgroundTintList(grayStateList);
			buttonArmedStatus.setText("Unarmed");
			buttonEngagedStatus.setBackgroundTintList(grayStateList);
			buttonEngagedStatus.setText("Unengaged");
			buttonAlarmStatus.setBackgroundTintList(grayStateList);
			buttonAlarmStatus.setText("No Alarms");
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {
		// append the text into the EditText
//		serialReceivedText.append(theString);
		serialStatusText = (TextView) findViewById(R.id.editText2);

		String displayString;
		if(!theString.contains("arm")) {
			displayString = "Status: " + theString;
		}
		else {
			displayString = serialStatusText.getText().toString();
		}

		// if the received string equals "TRIGGER_ALARM"
		if (theString.contains("TRIGGER_ALARM")) {
			// make the alarm button visible
			buttonDisarm.setVisibility(View.VISIBLE);
			serialStatusText.setText(displayString);

			buttonEngagedStatus.setBackgroundTintList(redStateList);
			buttonEngagedStatus.setText("Unengaged");


			// Get the current tint list from the button
			ColorStateList currentTintList = buttonAlarmStatus.getBackgroundTintList();

			// Check if the default color of the current tint list is the same as the red color
			if (currentTintList != null && currentTintList.getDefaultColor() == redStateList.getDefaultColor()) {
				// Define a new ColorStateList for the greyed-out red color
				// Replace "0x80FF0000" with the desired greyed-out red color in hexadecimal format
				ColorStateList greyRedStateList = ColorStateList.valueOf(Color.parseColor("#80FF0000"));
				buttonAlarmStatus.setBackgroundTintList(greyRedStateList);
			}
			else {
				buttonAlarmStatus.setBackgroundTintList(redStateList);
			}
			buttonAlarmStatus.setText("ALARM TRIGGERED");

		}
		else if (theString.contains("WAIT_FOR_ARM")) {
			buttonArm.setVisibility(View.VISIBLE);
			buttonDisarm.setVisibility(View.GONE);
			serialStatusText.setText(displayString);

			buttonArmedStatus.setBackgroundTintList(yellowStateList);
			buttonArmedStatus.setText("Ready To Arm");

			buttonEngagedStatus.setBackgroundTintList(grayStateList);
			buttonEngagedStatus.setText("Unengaged");

			buttonAlarmStatus.setBackgroundTintList(grayStateList);
			buttonAlarmStatus.setText("No Alarms");
		}
		else if (theString.contains("WAIT_SWITCH_ENGAGE")) {
			buttonArm.setVisibility(View.GONE);
			buttonDisarm.setVisibility(View.VISIBLE);
			serialStatusText.setText(displayString);

			buttonArmedStatus.setBackgroundTintList(greenStateList);
			buttonArmedStatus.setText("Armed");
			buttonEngagedStatus.setBackgroundTintList(yellowStateList);
			buttonEngagedStatus.setText("Unengaged");
		}
		else if (theString.contains("SWITCH_ARMED")) {
			buttonArm.setVisibility(View.GONE);
			buttonDisarm.setVisibility(View.VISIBLE);
			serialStatusText.setText(displayString);

			buttonEngagedStatus.setBackgroundTintList(greenStateList);
			buttonEngagedStatus.setText("Engaged");
			buttonAlarmStatus.setBackgroundTintList(greenStateList);
			buttonAlarmStatus.setText("No Alarms");
		}

		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
	}


}