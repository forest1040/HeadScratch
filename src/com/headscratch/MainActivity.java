package com.headscratch;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import java.util.UUID;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.os.Build;

@SuppressLint("NewApi")
public class MainActivity extends YouTubeFailureRecoveryActivity implements
	View.OnClickListener,
	CompoundButton.OnCheckedChangeListener,
	YouTubePlayer.OnFullscreenListener,
	BluetoothAdapter.LeScanCallback {

	private static final int PORTRAIT_ORIENTATION = Build.VERSION.SDK_INT < 9
	  ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
	  : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
	
	private LinearLayout baseLayout;
	private YouTubePlayerView playerView;
	private YouTubePlayer player;
	private Button fullscreenButton;
	private CompoundButton checkbox;
	private View otherViews;
	
	private boolean fullscreen;
	
	/********** Blue Tooth *********/
    /** BLE 機器スキャンタイムアウト (ミリ秒) */
    private static final long SCAN_PERIOD = 10000;
    /** 検索機器の機器名 */
    private static final String DEVICE_NAME = "SensorTag";
    /** 対象のサービスUUID */
    private static final String DEVICE_BUTTON_SENSOR_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    /** 対象のキャラクタリスティックUUID */
    private static final String DEVICE_BUTTON_SENSOR_CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    /** キャラクタリスティック設定UUID */
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
 
    private static final String TAG = "BLESample";
    private BleStatus mStatus = BleStatus.DISCONNECTED;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private TextView mStatusText;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Activity act = this;
		
		setContentView(R.layout.activity_main);
		baseLayout = (LinearLayout) findViewById(R.id.layout);
		playerView = (YouTubePlayerView) findViewById(R.id.player);
		fullscreenButton = (Button) findViewById(R.id.fullscreen_button);
		checkbox = (CompoundButton) findViewById(R.id.landscape_fullscreen_checkbox);
		otherViews = findViewById(R.id.other_views);
		
		checkbox.setOnCheckedChangeListener(this);
		// You can use your own button to switch to fullscreen too
		fullscreenButton.setOnClickListener(this);
		
		playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);
		
		
		
		/********** Blue Tooth *********/
        mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
 
        findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
                
                /* */
                //player.cueVideo("avP5d16wEp0");
                //player.cueVideo("JW7y1gzWISA");
                
            }
        });
        findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });
        findViewById(R.id.full_sc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	//Intent i = new Intent(act, VideoWallDemoActivity.class);
            	Intent i = new Intent(act, FullActivity.class);
            	startActivity(i);
            	
            	/* fullscreen */
            	
//            	fullscreen = true;
//            	player.setFullscreenControlFlags(1);
//            	player.setFullscreen(fullscreen);
            	
//        		LinearLayout.LayoutParams playerParams =
//        			    (LinearLayout.LayoutParams) playerView.getLayoutParams();
//			  playerParams.width = LayoutParams.MATCH_PARENT;
//			  playerParams.height = LayoutParams.MATCH_PARENT;
//		      baseLayout.setOrientation(LinearLayout.HORIZONTAL);
//			  otherViews.setVisibility(View.GONE);
            }
        });
 
        mStatusText = (TextView)findViewById(R.id.text_status);
 
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mStatusText.setText(((BleStatus) msg.obj).name());
            }
        };
		
		
		doLayout();
	}
	
	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
			boolean wasRestored) {
		this.player = player;
		setControlsEnabled();
		// Specify that we want to handle fullscreen behavior ourselves.
		player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
		player.setOnFullscreenListener(this);
		if (!wasRestored) {
			//player.cueVideo("3rHEovs2RpY");
			
			//player.loadPlaylist("PLy5ID8dpVBmiCt6JcdSesIxalwbfl_nmZ");
			
			player.cueVideo("zr6ZI3HFJpU");
		}
	}
	
	@Override
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return playerView;
	}
	
	@Override
	public void onClick(View v) {
		player.setFullscreen(!fullscreen);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int controlFlags = player.getFullscreenControlFlags();
		if (isChecked) {
		  // If you use the FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE, your activity's normal UI
		  // should never be laid out in landscape mode (since the video will be fullscreen whenever the
		  // activity is in landscape orientation). Therefore you should set the activity's requested
		  // orientation to portrait. Typically you would do this in your AndroidManifest.xml, we do it
		  // programmatically here since this activity demos fullscreen behavior both with and without
		  // this flag).
		  setRequestedOrientation(PORTRAIT_ORIENTATION);
		  controlFlags |= YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
		} else {
		  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		  controlFlags &= ~YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
		}
		player.setFullscreenControlFlags(controlFlags);
		}
		
	private void doLayout() {
		LinearLayout.LayoutParams playerParams =
		    (LinearLayout.LayoutParams) playerView.getLayoutParams();

		if (fullscreen) {
		  // When in fullscreen, the visibility of all other views than the player should be set to
		  // GONE and the player should be laid out across the whole screen.
		  playerParams.width = LayoutParams.MATCH_PARENT;
		  playerParams.height = LayoutParams.MATCH_PARENT;
		
		  otherViews.setVisibility(View.GONE);
		} else {
		  // This layout is up to you - this is just a simple example (vertically stacked boxes in
		  // portrait, horizontally stacked in landscape).
		  otherViews.setVisibility(View.VISIBLE);
		  ViewGroup.LayoutParams otherViewsParams = otherViews.getLayoutParams();
		  if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		    playerParams.width = otherViewsParams.width = 0;
		    playerParams.height = WRAP_CONTENT;
		    otherViewsParams.height = MATCH_PARENT;
		    playerParams.weight = 1;
		    baseLayout.setOrientation(LinearLayout.HORIZONTAL);
		  } else {
		    playerParams.width = otherViewsParams.width = MATCH_PARENT;
		    playerParams.height = WRAP_CONTENT;
		    playerParams.weight = 0;
		    otherViewsParams.height = 0;
		    baseLayout.setOrientation(LinearLayout.VERTICAL);
		  }
		  setControlsEnabled();
		}
	}
	
	private void setControlsEnabled() {
		checkbox.setEnabled(player != null
		    && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
		fullscreenButton.setEnabled(player != null);
	}
	
	@Override
	public void onFullscreen(boolean isFullscreen) {
		fullscreen = isFullscreen;
		doLayout();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		doLayout();
	}

	
	/********** Blue Tooth *********/
    /** BLE機器を検索する */
    private void connect() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(MainActivity.this);
                if (BleStatus.SCANNING.equals(mStatus)) {
                    setStatus(BleStatus.SCAN_FAILED);
                }
            }
        }, SCAN_PERIOD);
 
        mBluetoothAdapter.stopLeScan(this);
        mBluetoothAdapter.startLeScan(this);
        setStatus(BleStatus.SCANNING);
    }
 
    /** BLE 機器との接続を解除する */
    private void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            setStatus(BleStatus.CLOSED);
        }
    }
 
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(TAG, "device found: " + device.getName());
        if (DEVICE_NAME.equals(device.getName())) {
            setStatus(BleStatus.DEVICE_FOUND);
 
            // 省電力のためスキャンを停止する
            mBluetoothAdapter.stopLeScan(this);
 
            // GATT接続を試みる
            mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
        }
    }
 
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange: " + status + " -> " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // GATTへ接続成功
                // サービスを検索する
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // GATT通信から切断された
                setStatus(BleStatus.DISCONNECTED);
                mBluetoothGatt = null;
            }
        }
 
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString(DEVICE_BUTTON_SENSOR_SERVICE_UUID));
                if (service == null) {
                    // サービスが見つからなかった
                    setStatus(BleStatus.SERVICE_NOT_FOUND);
                } else {
                    // サービスを見つけた
                    setStatus(BleStatus.SERVICE_FOUND);
 
                    BluetoothGattCharacteristic characteristic =
                            service.getCharacteristic(UUID.fromString(DEVICE_BUTTON_SENSOR_CHARACTERISTIC_UUID));
 
                    if (characteristic == null) {
                        // キャラクタリスティックが見つからなかった
                        setStatus(BleStatus.CHARACTERISTIC_NOT_FOUND);
                    } else {
                        // キャラクタリスティックを見つけた
 
                        // Notification を要求する
                        boolean registered = gatt.setCharacteristicNotification(characteristic, true);
 
                        // Characteristic の Notification 有効化
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
 
                        if (registered) {
                            // Characteristics通知設定完了
                            setStatus(BleStatus.NOTIFICATION_REGISTERED);
                        } else {
                            setStatus(BleStatus.NOTIFICATION_REGISTER_FAILED);
                        }
                    }
                }
            }
        }
 
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // READ成功
            }
        }
 
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
            // Characteristicの値更新通知
 
            if (DEVICE_BUTTON_SENSOR_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
                Byte value = characteristic.getValue()[0];
                boolean left = (0 < (value & 0x02));
                boolean right = (0 < (value & 0x01));
                updateButtonState(left, right);
            }
        }
    };
 
    private void updateButtonState(final boolean left, final boolean right) {
    	final Activity act = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //View leftView = findViewById(R.id.left);
                //View rightView = findViewById(R.id.right);
                //leftView.setBackgroundColor( (left ? Color.BLUE : Color.TRANSPARENT) );
                //rightView.setBackgroundColor( (right ? Color.BLUE : Color.TRANSPARENT) );
            
                if (left) {
                	//player.loadVideo("JW7y1gzWISA");
                	//player.play();
                	
                	//Intent i = new Intent(act, VideoWallDemoActivity.class);
                	Intent i = new Intent(act, FullActivity.class);
                	startActivity(i);

                }
                if (right) {
                	player.loadVideo("raRHUG0PkQU");
                	//player.cueVideo("raRHUG0PkQU");
                	//player.play();
                	//Intent i = new Intent(act, VideoWallDemoActivity.class);
                	//startActivity(i);
                }
                
            }
        });
    }
 
    private void setStatus(BleStatus status) {
        mStatus = status;
        mHandler.sendMessage(status.message());
    }
 
    private enum BleStatus {
        DISCONNECTED,
        SCANNING,
        SCAN_FAILED,
        DEVICE_FOUND,
        SERVICE_NOT_FOUND,
        SERVICE_FOUND,
        CHARACTERISTIC_NOT_FOUND,
        NOTIFICATION_REGISTERED,
        NOTIFICATION_REGISTER_FAILED,
        CLOSED
        ;
        public Message message() {
            Message message = new Message();
            message.obj = this;
            return message;
        }
    }
}

