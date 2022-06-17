package amap.com.example.flutter_amap_plugin.Search;

import android.app.Activity;
import android.content.Context;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amap.com.example.flutter_amap_plugin.FlutterAmapPlugin;
import amap.com.example.flutter_amap_plugin.Map.Coordinate;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class FlutterAMapConvertRegister implements MethodChannel.MethodCallHandler, GeocodeSearch.OnGeocodeSearchListener,PoiSearch.OnPoiSearchListener {
    public static final String SEARCH_CONVERT_CHANNEL_NAME = "plugin/amap/search/convert";
    private GeocodeSearch geocoderSearch;

    public Activity activity;
    public Context context;

    private PoiSearch poiSearch = null;
    private MethodChannel.Result resultCallback = null;

    public FlutterAMapConvertRegister(Activity activity,Context context){
        this.activity = activity;
        this.context = context;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        try {
            geocoderSearch = new GeocodeSearch(context);
            geocoderSearch.setOnGeocodeSearchListener(this);
            if (methodCall.method.equals("geoToCoordinate")) {
                if (methodCall.arguments instanceof String) {
                    geoConvertToCoordinate(methodCall.arguments.toString());
                }
            } else if (methodCall.method.equals("coordinateToGeo")) {
                if (methodCall.arguments instanceof String) {
                    Coordinate model = new Gson().fromJson(methodCall.arguments.toString(), Coordinate.class);
                    coordinateConvertToGeo(model);
                }
            } else if (methodCall.method.equals("distance")) {
                Map params = (Map)methodCall.arguments;
                LatLng start = new LatLng((double) params.get("startLat"),(double) params.get("startLng"));
                LatLng end = new LatLng((double) params.get("endLat"),(double) params.get("endLng"));
                float distance = AMapUtils.calculateLineDistance(start,end);
                result.success(distance);
            } else if (methodCall.method.equals("searchKeyword")) {
                Map params = (Map)methodCall.arguments;
                String keyword = (String) params.get("keyword");
                String city = (String) params.get("city");
                PoiSearch.Query query = new PoiSearch.Query(keyword,"",city);
                query.setPageSize(35);
                query.setPageNum(1);
                poiSearch = new PoiSearch(context,query);
                poiSearch.setOnPoiSearchListener(this);
                poiSearch.searchPOIAsyn();
                resultCallback =result;
            }
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    private void geoConvertToCoordinate(String address) {
        GeocodeQuery query = new GeocodeQuery(address, "");
        geocoderSearch.getFromLocationNameAsyn(query);
    }

    private void coordinateConvertToGeo(Coordinate coordinate) {
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(coordinate.latitude, coordinate.longitude), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 1000) {
            Map<String, Object> map = new HashMap<>();
            map.put("address", regeocodeResult.getRegeocodeAddress().getFormatAddress());
            FlutterAmapPlugin.convertChannel.invokeMethod("onCoordinateToGeo", map);
        } else {
            FlutterAmapPlugin.locChannel.invokeMethod("onConvertError",
                    "坐标转地址错误:{" + i + " - 未发现有效地址}");
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        if (i == 1000 && geocodeResult.getGeocodeAddressList().size() > 0) {
            LatLonPoint latLonPoint = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
            Map<String, Object> map = new HashMap<>();
            map.put("lat", latLonPoint.getLatitude());
            map.put("lon", latLonPoint.getLongitude());
            FlutterAmapPlugin.convertChannel.invokeMethod("onGeoToCoordinate", map);

        } else {
            FlutterAmapPlugin.locChannel.invokeMethod("onConvertError",
                    "地址转坐标错误:{" + i + " - 未发现有效地址}");
        }

    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        List<Map> pois = new ArrayList<>();

        for (int j = 0; j < poiResult.getPois().size(); j++) {
            PoiItem item = poiResult.getPois().get(j);
            Map poiMap = new HashMap<>();
            poiMap.put("adcode",item.getAdCode());
            poiMap.put("address",item.getSnippet());//地址
            poiMap.put("businessArea",item.getBusinessArea());
            poiMap.put("city",item.getCityName());
            poiMap.put("citycode",item.getCityCode());
            poiMap.put("direction",item.getDirection());
            poiMap.put("distance",item.getDistance());
            poiMap.put("district",item.getAdName());//行政区划名称
            poiMap.put("email",item.getEmail());
            poiMap.put("gridcode","");// 安卓api中未找到
            poiMap.put("hasIndoorMap",item.isIndoorMap());

            LatLonPoint point = item.getLatLonPoint();
            Map pointMap = new HashMap<>();
            pointMap.put("latitude",point.getLatitude());
            pointMap.put("longitude",point.getLongitude());

            poiMap.put("location",pointMap);
            poiMap.put("name",item.getTitle());
            poiMap.put("parkingType",item.getParkingType());
            poiMap.put("pcode",item.getProvinceCode());
            pois.add(poiMap);
        }
        resultCallback.success(pois);
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
