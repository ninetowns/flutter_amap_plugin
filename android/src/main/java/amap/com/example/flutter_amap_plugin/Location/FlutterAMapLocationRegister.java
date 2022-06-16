package amap.com.example.flutter_amap_plugin.Location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.amap.api.location.AMapLocationClient;

import amap.com.example.flutter_amap_plugin.FlutterAmapPlugin;
import io.flutter.app.FlutterPluginRegistry;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Created by fengshun
 * Create Date 2019-06-06 16:56
 * amap.com.example.flutter_amap_plugin.Location
 */
public class FlutterAMapLocationRegister implements MethodChannel.MethodCallHandler {
    public static final String LOCATION_CHANNEL_NAME = "plugin/amap/location";
    @SuppressLint("StaticFieldLeak")
    static AMapLocationClient mLocationClient = null;
    public Activity activity;
    public Context context;

    public FlutterAMapLocationRegister(Activity activity,Context context){
        this.activity = activity;
        this.context = context;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        try {
            AMapLocationClient.updatePrivacyAgree(context,true);
            AMapLocationClient.updatePrivacyShow(context,true,true);
            mLocationClient = new AMapLocationClient(activity.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

