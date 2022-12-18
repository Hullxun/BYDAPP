package com.byd.user.helloworld;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.bydauto.ac.AbsBYDAutoAcListener;
import android.hardware.bydauto.ac.BYDAutoAcDevice;
import android.hardware.bydauto.bodywork.BYDAutoBodyworkDevice;
import android.hardware.bydauto.sensor.BYDAutoSensorDevice;
import android.hardware.bydauto.speed.AbsBYDAutoSpeedListener;
import android.hardware.bydauto.speed.BYDAutoSpeedDevice;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.hardware.bydauto.ac.BYDAutoAcDevice.getInstance;

public class MainActivity extends AppCompatActivity {

    private Context context=this;
    private BYDAutoBodyworkDevice mBodyworkDevice = null;
    private BYDAutoAcDevice mAcDevice = null; //空调类
    private BYDAutoSpeedDevice speedDevice=null;//车速类
    private ImageButton refreshVinBtn, refreshStatusBtn;
    private TextView mVin, statusTV, mainTemperatureTV, deputyTemperatureTV, windLevelTV,textspeed;
    private Switch switchAcSwitch, separateSwitch, ventilationSwitch,defrostSwitch,autoSwitch;
    private RadioGroup areaGroup;
    private RadioButton globalRb, mainRb, deputyRb;
    private EditText temperatureED, windLevelED;
    private Button temperatureSetBtn, windLevelSetBtn;
    private int area = BYDAutoAcDevice.AC_TEMPERATURE_MAIN_DEPUTY;
    private  TextView windLevel;//显示空调挡位
    private Button bt_getLightLevel;
    private TextView tv_lightLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVin = (TextView) findViewById(R.id.vinCode);
        //主动刷新车架号Vin
        refreshVinBtn = (ImageButton)findViewById(R.id.refreshVinBtn);

        //动态权限必须在获取类实例之前调用，动态申请车身状态类动态权限
        requestBodyworkCommonPermission();

        refreshVinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBodyworkDevice != null) {          //先判断该对象是否已经实例化
                    //通过车身状态获得车架号接口获得车机的车架号
                    String vin = mBodyworkDevice.getAutoVIN();
                    mVin.setText(vin);
                }
            }
        });
        //显示区域控件
        statusTV = (TextView) findViewById(R.id.statusTV);  //实时外界温度
        mainTemperatureTV = (TextView) findViewById(R.id.mainTemperatureTV);
        deputyTemperatureTV = (TextView) findViewById(R.id.deputyTemperatureTV);
        windLevelTV = (TextView) findViewById(R.id.windLevel);
        refreshStatusBtn = (ImageButton) findViewById(R.id.refreshStatusBtn);
        refreshStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取主驾温度
                int mainTemperature = mAcDevice.getTemprature(BYDAutoAcDevice.AC_TEMPERATURE_MAIN);
                mainTemperatureTV.setText(String.valueOf(mainTemperature));
                //获取副驾温度
                int deputyTemperature = mAcDevice.getTemprature(BYDAutoAcDevice.AC_TEMPERATURE_DEPUTY);
                deputyTemperatureTV.setText(String.valueOf(deputyTemperature));
                //获取风速
                int mainWindLevel = mAcDevice.getAcWindLevel();
                windLevelTV.setText(String.valueOf(mainWindLevel));
            }
        });
        //控制区域控件
        switchAcSwitch = (Switch) findViewById(R.id.switchAcSwitch);
        separateSwitch = (Switch) findViewById(R.id.separateSwitch);
        ventilationSwitch = (Switch) findViewById(R.id.ventilationSwitch);
        defrostSwitch = (Switch) findViewById(R.id.defrostSwitch);
        autoSwitch = (Switch) findViewById(R.id.autoSwitch);
        //空调开关
        switchAcSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    mAcDevice.start(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE);
                }else {
                    mAcDevice.stop(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE);
                }
                int state = mAcDevice.getAcStartState();
                String stateValue = (state==BYDAutoAcDevice.AC_POWER_ON?"空调打开":"空调关闭");
                Log.d("state",stateValue);
            }
        });
        //分控开关
        separateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    mAcDevice.setAcTemperatureControlMode(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_TEMPCTRL_SEPARATE_ON);
                }else {
                    mAcDevice.setAcTemperatureControlMode(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_TEMPCTRL_SEPARATE_OFF);
                }
                int code = mAcDevice.getAcTemperatureControlMode();
                Log.d("code",code+"");
            }
        });
        //通风开关
        ventilationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    mAcDevice.setAcVentilationState(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_VENTILATION_STATE_ON);
                }else {
                    mAcDevice.setAcVentilationState(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_VENTILATION_STATE_OFF);
                }
                int code = mAcDevice.getAcVentilationState();
                Log.d("code",code+"");
            }
        });
        //前除霜开关
        defrostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    mAcDevice.setAcDefrostState(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_DEFROST_AREA_FRONT,
                            BYDAutoAcDevice.AC_DEFROST_STATE_ON);
                }else {
                    mAcDevice.setAcDefrostState(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_DEFROST_AREA_FRONT,
                            BYDAutoAcDevice.AC_DEFROST_STATE_OFF);
                }
                int code = mAcDevice.getAcDefrostState(BYDAutoAcDevice.AC_DEFROST_AREA_FRONT);
                Log.d("code",code+"");
            }
        });
        //自动手动开关
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    mAcDevice.setAcControlMode(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_CTRLMODE_AUTO);
                }else {
                    mAcDevice.setAcControlMode(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,
                            BYDAutoAcDevice.AC_CTRLMODE_MANUAL);
                }
                int code = mAcDevice.getAcControlMode();
                Log.d("code",code+"");
            }
        });
        areaGroup = (RadioGroup) findViewById(R.id.areaGroup);
        globalRb = (RadioButton) findViewById(R.id.globalRb);
        mainRb = (RadioButton) findViewById(R.id.mainRb);
        deputyRb = (RadioButton) findViewById(R.id.deputyRb);
        areaGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                String areaValue = "设置全局";
                if(globalRb.getId() == checkedId){
                    area = BYDAutoAcDevice.AC_TEMPERATURE_MAIN_DEPUTY;
                    areaValue = "设置全局";
                }
                if(mainRb.getId() == checkedId){
                    area = BYDAutoAcDevice.AC_TEMPERATURE_MAIN;
                    areaValue = "设置主驾";
                }
                if(deputyRb.getId() == checkedId){
                    area = BYDAutoAcDevice.AC_TEMPERATURE_DEPUTY;
                    areaValue = "设置副驾";
                }
                Log.d("area",areaValue);
            }
        });
        temperatureED = (EditText) findViewById(R.id.temperatureED);
        windLevelED = (EditText) findViewById(R.id.windLevelED);
        temperatureSetBtn = (Button) findViewById(R.id.temperatureSetBtn);
        windLevelSetBtn = (Button) findViewById(R.id.windLevelSetBtn);
        temperatureSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = 17;
                try{
                    value = Integer.valueOf(temperatureED.getText().toString()).intValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
                int code = mAcDevice.setAcTemperature(area,value,
                        BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,BYDAutoAcDevice.AC_TEMPERATURE_UNIT_OC);
                Log.d("code",code+"");
            }
        });
        windLevelSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = 0;
                try{
                    value = Integer.valueOf(windLevelED.getText().toString()).intValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
                int code = mAcDevice.setAcWindLevel(BYDAutoAcDevice.AC_CTRL_SOURCE_VOICE,value);
                Log.d("code",code+"");
            }
        });

        if (mBodyworkDevice != null) {
            //通过车身状态获得车架号接口获得车机的车架号
            String vin = mBodyworkDevice.getAutoVIN();
            mVin.setText(vin);
        }

        if (mAcDevice != null) {
            //通过空调类的设置风量档位接口设置一个风量档位
            mAcDevice.setAcWindLevel(BYDAutoAcDevice.AC_CTRL_SOURCE_UI_KEY, BYDAutoAcDevice.AC_WINDLEVEL_3);
            //通过空调类的获得风量档位接口获得当前风量档位，获得的值应该设置的值
            Integer acwindlevel = mAcDevice.getAcWindLevel();
            windLevelTV.setText(String.valueOf(acwindlevel));
        }

        //获取光照强度
        bt_getLightLevel=(Button)findViewById(R.id.button_get_light_level);
        tv_lightLevel=(TextView)findViewById(R.id.text_light_level);
        bt_getLightLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int light_level = BYDAutoSensorDevice.getInstance(MainActivity.this).getLightIntensity();
                Log.d("光照等级：",Integer.toString(light_level));
                tv_lightLevel.setText(String.valueOf(light_level));
            }
        });
        //BT按钮
        Button kt =(Button)findViewById(R.id.KT);
        kt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BYDAutoSpeedDevice bydAutoSpeedDevice = BYDAutoSpeedDevice.getInstance(context);
                    double seed =bydAutoSpeedDevice.getCurrentSpeed();
                    textspeed.setText((int)seed);
                }catch(Exception e) {

                }

            }
        });
    }



    //实现外界温度的监听接口
    private final AbsBYDAutoAcListener mAbsBYDAutoAcListener = new AbsBYDAutoAcListener() {
        @Override
        public void onTemperatureChanged(int area, int value) {
            super.onTemperatureChanged(area, value);
            Log.d("tag","area = "+ area+", value = "+value);
            if(area == BYDAutoAcDevice.AC_TEMPERATURE_OUT){
                statusTV.setText(value+"");
                //registerListener(c)
                //
            }
        }
    };
    //实现实时车速的监听接口
    private final  AbsBYDAutoSpeedListener absBYDAutoSpeedListener =new AbsBYDAutoSpeedListener()   {
        @Override
        public void  onSpeedChanged(double value){
            super.onSpeedChanged(value);

            textspeed.setText((int)value);
            Log.d("speed"," value = "+value);
        }
       // BYDAutoSpeedDevice bydAutoSpeedDevice = BYDAutoSpeedDevice.getInstance(this);
       // double seed =bydAutoSpeedDevice.getCurrentSpeed();


    };

    @Override
    protected void onStart() {
        super.onStart();
        if(mAcDevice!=null){

            mAcDevice.registerListener(mAbsBYDAutoAcListener);
        }
        if (speedDevice!=null){
            Log.d("tag","speedDevice 开启监听");
            speedDevice.registerListener(absBYDAutoSpeedListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAcDevice!=null){
            Log.d("tag","2222");
            mAcDevice.unregisterListener(mAbsBYDAutoAcListener);
        }
    }

    //申请车身状态类的动态权限
    public void requestBodyworkCommonPermission() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.BYDAUTO_BODYWORK_COMMON)
                != PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.BYDAUTO_BODYWORK_COMMON)) {
                Toast.makeText(this, "之前拒绝了该权限申请，需要清除应用数据，重新申请，否则不能使用！", Toast.LENGTH_SHORT).show();
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BYDAUTO_BODYWORK_COMMON,}, 1);
            }
        } else {
            // 已获得车身状态类的动态权限，可以获取实例
            if (mBodyworkDevice == null) {
                //获取车身状态类实例
                mBodyworkDevice = BYDAutoBodyworkDevice.getInstance(this);

                //通过车身状态获得车架号接口获得车机的车架号
                String vin = mBodyworkDevice.getAutoVIN();
                mVin.setText(vin);
            }

            if (mAcDevice == null) {
                //获取空调类实例
                mAcDevice = BYDAutoAcDevice.getInstance(this);

               //通过空调类的设置风量档位接口设置一个风量档位
                mAcDevice.setAcWindLevel(BYDAutoAcDevice.AC_CTRL_SOURCE_UI_KEY, BYDAutoAcDevice.AC_WINDLEVEL_3);
                //通过空调类的获得风量档位接口获得当前风量档位，获得的值应该设置的值
               // Integer acwindlevel = mAcDevice.getAcWindLevel();
               // windLevel.setText(String.valueOf(acwindlevel));
            }
            if (speedDevice==null){
                Log.d("TAG", "onRequestPermissionsResult: speed device = null");
                //获取车速类实例
                speedDevice =BYDAutoSpeedDevice.getInstance(this);




            }
        }
    }

    //申请空调类的动态权限
    public void requestAcCommonPermission() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.BYDAUTO_AC_COMMON)
                != PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BYDAUTO_AC_COMMON)) {
                Toast.makeText(this, "之前拒绝了该权限申请，需要清除应用数据，重新申请，否则不能使用！", Toast.LENGTH_SHORT).show();
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BYDAUTO_AC_COMMON,}, 1);
            }
        } else {
            // 已获得空调类的动态权限，可以获取实例
            if (mAcDevice == null) {
                //获取空调类实例
                mAcDevice = BYDAutoAcDevice.getInstance(this);

                //通过空调类的设置风量档位接口设置一个风量档位
                mAcDevice.setAcWindLevel(BYDAutoAcDevice.AC_CTRL_SOURCE_UI_KEY, BYDAutoAcDevice.AC_WINDLEVEL_3);
                //通过空调类的获得风量档位接口获得当前风量档位，获得的值应该设置的值
                Integer acwindlevel = mAcDevice.getAcWindLevel();
                windLevelTV.setText(String.valueOf(acwindlevel));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                    if (permissions[i].equals( android.Manifest.permission.BYDAUTO_BODYWORK_COMMON)) {
                        if (mBodyworkDevice == null) {
                            //获取车身状态类实例
                            mBodyworkDevice = BYDAutoBodyworkDevice.getInstance(this);

                            //通过车身状态获得车架号接口获得车机的车架号
                            String vin = mBodyworkDevice.getAutoVIN();
                            mVin.setText(vin);

                            //动态权限必须在获取类实例之前调用，动态申请空调类动态权限
                            requestAcCommonPermission();
                        }
                    } else if (permissions[i].equals( android.Manifest.permission.BYDAUTO_AC_COMMON)) {
                        if (mAcDevice == null) {
                            //获取空调类实例
                            mAcDevice = getInstance(this);

                            //通过空调类的设置风量档位接口设置一个风量档位
                            mAcDevice.setAcWindLevel(BYDAutoAcDevice.AC_CTRL_SOURCE_UI_KEY, BYDAutoAcDevice.AC_WINDLEVEL_3);
                            //通过空调类的获得风量档位接口获得当前风量档位，获得的值应该设置的值
                            Integer acwindlevel = mAcDevice.getAcWindLevel();
                            windLevelTV.setText(String.valueOf(acwindlevel));
                            //这里注册空调温度监听
                            mAcDevice.registerListener(mAbsBYDAutoAcListener);
                        }

                    }
                } else {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
