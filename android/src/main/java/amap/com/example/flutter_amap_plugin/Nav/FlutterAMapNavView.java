package amap.com.example.flutter_amap_plugin.Nav;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amap.api.maps.AMapException;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.MapStyle;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapCarInfo;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import amap.com.example.flutter_amap_plugin.Map.Coordinate;
import amap.com.example.flutter_amap_plugin.R;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;

import static amap.com.example.flutter_amap_plugin.FlutterAmapPlugin.CREATED;
import static amap.com.example.flutter_amap_plugin.FlutterAmapPlugin.DESTROYED;
import static amap.com.example.flutter_amap_plugin.FlutterAmapPlugin.RESUMED;
import static amap.com.example.flutter_amap_plugin.FlutterAmapPlugin.STOPPED;

public class FlutterAMapNavView implements PlatformView, MethodChannel.MethodCallHandler, Application.ActivityLifecycleCallbacks, AMapNaviListener, AMapNaviViewListener {
    public static final String NAV_CHANNEL_NAME = "plugin/amap/nav";

    private Context context;
    private AtomicInteger atomicInteger;
    private MethodChannel navChannel;
    private Activity activity;
    private AMapNavModel mOptions;

    private AMapNaviView navView;
    private AMapNavi aMapNav;
    private View view;
    private Coordinate latlon;


    private boolean disposed = false;

    public FlutterAMapNavView(BinaryMessenger messenger, Context context, int id, Activity _activity, AMapNavModel model) {
        try {
            this.context = context;
//            this.atomicInteger = atomicInteger;
            this.mOptions = model;
            this.activity = _activity;

            navChannel = new MethodChannel(messenger, NAV_CHANNEL_NAME + "/" + id);
            navChannel.setMethodCallHandler(this);

            aMapNav = AMapNavi.getInstance(_activity);
            aMapNav.addAMapNaviListener(this);
            aMapNav = AMapNavi.getInstance(this.context);
//            aMapNav.setUseInnerVoice(true);

            view = View.inflate(_activity, R.layout.amap_nav, null);
            navView = view.findViewById(R.id.navi_view);

        }catch (AMapException e){

        }
    }

    void initNav() {
        AMapNaviViewOptions options = configOptions();
        options.setLayoutVisible(true);
        navView.onCreate(null);
        navView.setViewOptions(options);
        navView.setAMapNaviViewListener(this);
    }

    private AMapNaviViewOptions configOptions() {
        AMapNaviViewOptions options = navView.getViewOptions();
        if (this.mOptions != null) {
            options.setLayoutVisible(mOptions.showUIElements);
            options.setModeCrossDisplayShow(mOptions.showCrossImage);
            options.setTrafficLayerEnabled(mOptions.showTrafficButton);
            options.setTrafficBarEnabled(mOptions.showTrafficBar);
            options.setRouteListButtonShow(mOptions.showBrowseRouteButton);
            options.setSettingMenuEnabled(mOptions.showMoreButton);
            options.setAutoLockCar(true);
            options.setLeaderLineEnabled(Color.argb(1,255,0,0));
            options.setAutoDisplayOverview(true);
            options.setNaviArrowVisible(true);
            options.setSecondActionVisible(true);
            options.setMapStyle(MapStyle.AUTO,"");
        }
        return options;
    }

    public void setup() {
        switch (atomicInteger.get()) {
            case STOPPED:
                navView.onCreate(null);
                navView.onResume();
                navView.onPause();
                break;
            case RESUMED:
                navView.onCreate(null);
                navView.onResume();
                break;
            case CREATED:
                navView.onCreate(null);
                break;
            case DESTROYED:
                navView.setAMapNaviViewListener(null);
                navView.onDestroy();
                aMapNav.removeAMapNaviListener(this);
                aMapNav.stopNavi();
                aMapNav.destroy();
                break;
        }

    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (methodCall.method.equals("startNav")) {
            if (methodCall.arguments instanceof String) {
                Gson gson = new Gson();
                Coordinate model = new Coordinate();
                model = gson.fromJson(methodCall.arguments.toString(), Coordinate.class);
                latlon = model;
                initNav();
            }
        }
    }

    /*
     * 生命周期
     * */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (disposed || activity.hashCode() != this.activity.hashCode()) {
            return;
        }
        navView.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (disposed || activity.hashCode() !=this.activity.hashCode()) {
            return;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (disposed || activity.hashCode() != this.activity.hashCode()) {
            return;
        }
        navView.onResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (disposed || activity.hashCode() != this.activity.hashCode()) {
            return;
        }
        navView.onPause();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (disposed || activity.hashCode() != this.activity.hashCode()) {
            return;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (disposed || activity.hashCode() != this.activity.hashCode()) {
            return;
        }
        navView.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (disposed || activity.hashCode() != this.activity.hashCode()) {
            return;
        }
        navView.onDestroy();
    }


    /*
     * AMapNaviListener
     * */

    @Override
    public void onInitNaviSuccess() {
         NaviLatLng mEndLatlng = new NaviLatLng(40.084894,116.603039);
         NaviLatLng mStartLatlng = new NaviLatLng(39.825934,116.342972);
         final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
         final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
         List<NaviLatLng> mWayPointList = new ArrayList<NaviLatLng>();
         sList.add(mStartLatlng);
         eList.add(mEndLatlng);
         int strategy = 0;
         strategy = aMapNav.strategyConvert(true, false, false, false, false);
         AMapCarInfo carInfo = new AMapCarInfo();
         aMapNav.calculateDriveRoute(sList, eList, mWayPointList, strategy);
//        if (latlon != null) {
//            int strategy = 0;
//            try {
//                strategy = aMapNav.strategyConvert(true, false, false, false, false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
//            NaviLatLng mEndLatlng = new NaviLatLng(latlon.latitude, latlon.longitude);
//            eList.add(mEndLatlng);
//            aMapNav.calculateDriveRoute(eList, null, strategy);
//        }
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        aMapNav.startNavi(NaviType.GPS);
    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }

    /*
     * AMapNaviViewListener
     * */

    @Override
    public void onNaviSetting() {
        navChannel.invokeMethod("more_nav", null);
    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        navChannel.invokeMethod("close_nav", null);
        return true;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }

    @Override
    public View getView() {
        return this.view;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        navView.setAMapNaviViewListener(null);
        navView.onDestroy();
        aMapNav.removeAMapNaviListener(this);
        aMapNav.stopNavi();
        aMapNav.destroy();

        navChannel.setMethodCallHandler(null);
    }
}
