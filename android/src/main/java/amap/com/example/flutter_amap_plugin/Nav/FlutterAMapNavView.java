package amap.com.example.flutter_amap_plugin.Nav;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapException;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.NaviSetting;
import com.amap.api.navi.enums.BroadcastMode;
import com.amap.api.navi.enums.MapStyle;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
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
import com.amap.api.navi.model.RouteOverlayOptions;
import com.amap.api.navi.view.RouteOverLay;
import amap.com.example.flutter_amap_plugin.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;


public class FlutterAMapNavView implements PlatformView, MethodChannel.MethodCallHandler, Application.ActivityLifecycleCallbacks, AMapNaviListener, AMapNaviViewListener {
    private Context context;
    private MethodChannel navChannel;
    private Activity activity;
    private AMapNavModel mOptions;

    private AMapNaviView navView;
    private AMapNavi aMapNav;
    private View view;

    private int naviType=1;
    private int daynight=0;
    private List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    private List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    private List<NaviLatLng> mWayPointList = new ArrayList<NaviLatLng>();

    private boolean disposed = false;

    public static final String NAV_CHANNEL_NAME = "plugin/amap/nav";

    public FlutterAMapNavView(BinaryMessenger messenger, Context context, int id, Activity activity, AMapNavModel model) {

        this.context = context;
        this.activity = activity;
        this.mOptions = model;

        view = View.inflate(activity, R.layout.amap_nav, null);
        navView = view.findViewById(R.id.navi_view);
        this.initNav();
        navChannel = new MethodChannel(messenger, NAV_CHANNEL_NAME + "/" + id);
        navChannel.setMethodCallHandler(this);
    }

    void initNav() {
        try {
            NaviSetting.updatePrivacyShow(this.context, true, true);
            NaviSetting.updatePrivacyAgree(this.context, true);
            aMapNav = AMapNavi.getInstance(activity);
            aMapNav.setUseInnerVoice(true,false);
            aMapNav.addAMapNaviListener(this);
            aMapNav.setMultipleRouteNaviMode(false);
            aMapNav.setBroadcastMode(BroadcastMode.DETAIL);

            AMapNaviViewOptions options = configOptions();
            options.setLayoutVisible(true);
            options.setAutoLockCar(true);
            options.setLeaderLineEnabled(Color.argb(1,255,0,0));
            options.setAutoDisplayOverview(true);
            options.setNaviArrowVisible(true);
            options.setTrafficBarEnabled(true);
//            options.setAutoNaviViewNightMode(true);
            options.setSecondActionVisible(true);

            navView.onCreate(null);
            navView.setShowMode(1);
            navView.setAMapNaviViewListener(this);
            options.setRouteListButtonShow(true);
            navView.setRouteMarkerVisible(true,true,true);

            RouteOverlayOptions rOptions = new RouteOverlayOptions();
            rOptions.setLineWidth(120);
            options.setRouteOverlayOptions(rOptions);

            navView.setViewOptions(options);

        }catch (AMapException e){
            e.printStackTrace();
        }
    }

    private Bitmap getNewBitmap(Bitmap bitmap, int newWidth ,int newHeight){
        // 获得图片的宽高.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }

    private AMapNaviViewOptions configOptions() {
        AMapNaviViewOptions options = navView.getViewOptions();
        options.setScreenAlwaysBright(true);
        options.setTrafficInfoUpdateEnabled(true);
        options.setAfterRouteAutoGray(true);
        Bitmap bitmap=BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/car.png"));
        options.setCarBitmap(getNewBitmap(bitmap,360,360));
        if (this.mOptions != null) {
            options.setLayoutVisible(mOptions.showUIElements);
            options.setModeCrossDisplayShow(mOptions.showCrossImage);
            options.setTrafficLayerEnabled(mOptions.showTrafficButton);
            options.setTrafficBarEnabled(mOptions.showTrafficBar);
            options.setRouteListButtonShow(mOptions.showBrowseRouteButton);

        }
        options.setSettingMenuEnabled(true);
        return options;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        String method = methodCall.method;
        Map argments = (Map)methodCall.arguments;

        if (method.equals("startNav")) {
            List start = (List)argments.get("start");
            List end = (List)argments.get("end");
            List wapPoint = (List)argments.get("wapPoint");
            this.daynight = Integer.parseInt((String) argments.get("daynight"));
            this.setMapStyle(this.daynight);
            this.naviType =  (int)argments.get("naviType");
            NaviLatLng mEndLatlng = new NaviLatLng((double)end.get(0), (double)end.get(1));
            NaviLatLng mStartLatlng = new NaviLatLng((double)start.get(0), (double)start.get(1));
            sList.add(mStartLatlng);
            eList.add(mEndLatlng);

            for(int i =0;i<wapPoint.size();i++){
                List<Double> point = (List)wapPoint.get(i);
                mWayPointList.add(new NaviLatLng(point.get(0), point.get(1)));
            }
            int strategy = 0;
            try {
                strategy = aMapNav.strategyConvert(true, false, false, false, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            aMapNav.calculateDriveRoute(sList, eList, mWayPointList, strategy);
            result.success("success");
        }else if(method.equals("playTTS")) {
            String content = (String) argments.get("content");
            aMapNav.playTTS(content, true);
            result.success("success");
        }else if(method.equals("changeSpeekType")) {
            int type = Integer.parseInt((String)argments.get("speekType"));

            if (type == 0) {
                aMapNav.setBroadcastMode(BroadcastMode.DETAIL);
            } else if (type == 1) {
                aMapNav.setBroadcastMode(BroadcastMode.CONCISE);
            } else {
                aMapNav.stopSpeak();
            }
        }else if(method.equals("changeNaviMode")) {
            int mode = Integer.parseInt((String) argments.get("mode"));
            navView.setNaviMode(mode);
        }else if(method.equals("changeDayNight")){
            int daynight = Integer.parseInt((String) argments.get("daynight"));
            this.setMapStyle(daynight);
        }else if(method.equals("stopNavi")){
            aMapNav.stopNavi();
            aMapNav.destroy();
        }
    }


    private void setMapStyle(int type){
        if(navView==null){
            return;
        }
        AMapNaviViewOptions options = navView.getViewOptions();
        if(type==0){
            options.setMapStyle(MapStyle.AUTO,"");
        }else if(type==1){
            options.setMapStyle(MapStyle.DAY,"");
        }else{
            options.setMapStyle(MapStyle.NIGHT,"");
        }
        navView.setViewOptions(options);
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    /*
     * 生命周期
     * */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//        if (disposed || activity.hashCode() != registrar.activity().hashCode()) {
//            return;
//        }
        navView.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
//        if (disposed || activity.hashCode() != registrar.activity().hashCode()) {
//            return;
//        }
    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
//        if (disposed || activity.hashCode() != registrar.activity().hashCode()) {
//            return;
//        }
        navView.onResume();
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPrePaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
//        if (disposed || activity.hashCode() != registrar.activity().hashCode()) {
//            return;
//        }
        navView.onPause();
    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
//        if (disposed || activity.hashCode() != registrar.activity().hashCode()) {
//            return;
//        }
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//        if (disposed || activity.hashCode() != registrar.activity().hashCode()) {
//            return;
//        }
        navView.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
//        if (disposed || activity.hashCode() != registrar.activity().hashCode()) {
//            return;
//        }
        navView.onDestroy();
    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {

    }


    /*
     * AMapNaviListener
     * */

    @Override
    public void onInitNaviSuccess() {
        navChannel.invokeMethod("onInitNaviSuccess",null);
    }

    @Override
    public void onInitNaviFailure() {
        navChannel.invokeMethod("onInitNaviFailure",null);
    }

    @Override
    public void onStartNavi(int i) {
        navChannel.invokeMethod("onStartNavi",null);
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
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("type",2);
        navChannel.invokeMethod("onEndNavi",resultMap);
    }

    @Override
    public void onArriveDestination() {
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("type",1);
        navChannel.invokeMethod("onEndNavi",resultMap);
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        navChannel.invokeMethod("onCalculateRouteFailure",null);
    }

    @Override
    public void onReCalculateRouteForYaw() {
        navChannel.invokeMethod("onReCalculateRoute",null);
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        navChannel.invokeMethod("onReCalculateRoute",null);
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
        navChannel.invokeMethod("onCalculateRouteSuccess",null);
//        this.setMapStyle(daynight);
        aMapNav.startNavi(this.naviType);
    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {
        navChannel.invokeMethod("onCalculateRouteFailure",null);
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
        navChannel.invokeMethod("onNaviCancel", null);
    }

    @Override
    public boolean onNaviBackClick() {
//        navChannel.invokeMethod("close_nav", null);
        return false;
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
    public void onFlutterViewDetached() {

    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
//        navView.setAMapNaviViewListener(null);
//        navView.onDestroy();
//        aMapNav.removeAMapNaviListener(this);
        aMapNav.stopNavi();
        aMapNav.destroy();

        navChannel.setMethodCallHandler(null);
    }

    @Override
    public void onInputConnectionLocked() {

    }

    @Override
    public void onInputConnectionUnlocked() {

    }
}
