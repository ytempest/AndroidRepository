package com.ytempest.daydayantis.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.ytempest.daydayantis.R;

import java.util.List;

public class MapAroundSearchActivity extends AppCompatActivity {
    private static final String TAG = "MapAroundSearchActivity";
    private final static String MARKER_INFO = "marker_info";


    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MapAroundSearchActivity.MapLocationListener mMapLocationListener;
    private LatLng mCurrentMarker;
    private int mMarkerHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 这个方法要在setContentView 方法之前
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map_around_search);

        initMap();


    }

    private void initMap() {

        mMapView = findViewById(R.id.map_view);

        mLocationClient = new LocationClient(getApplicationContext());
        initLocationOption();
        mMapLocationListener = new MapAroundSearchActivity.MapLocationListener();
        mLocationClient.registerLocationListener(mMapLocationListener);
        mLocationClient.start();

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(new MapAroundSearchActivity.MapMarkerClickListener());
        // 设置地图的缩放级别
        MapStatus build = new MapStatus.Builder().zoom(15).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(build));

    }

    private void initLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        int span = 1000;
        option.setScanSpan(span);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
    }

    private class MapLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null) {


                // 定位一次成功之后不需要再次定位
                mLocationClient.unRegisterLocationListener(mMapLocationListener);
                mLocationClient.stop();


                // 1.把地图移动到当前自己定位的位置
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng currentLocation = new LatLng(latitude, longitude);
                MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLng(currentLocation);
                mBaiduMap.setMapStatus(statusUpdate);

                // 2.在自己的位置添加一个覆盖物
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_worker);
                mMarkerHeight = descriptor.getBitmap().getHeight();
                OverlayOptions overlayOptions = new MarkerOptions().position(currentLocation).icon(descriptor);
                Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);

                // 3.利用bundle 把值传给Marker
                Bundle bundle = new Bundle();
                MarkerInfo markerInfo = new MarkerInfo();
                markerInfo.mAddress = location.getAddrStr();
                markerInfo.mLatLng = currentLocation;
                bundle.putSerializable(MARKER_INFO, markerInfo);
                marker.setExtraInfo(bundle);

                mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (mCurrentMarker.latitude != latLng.latitude
                                && mCurrentMarker.longitude != latLng.longitude) {
                            mBaiduMap.hideInfoWindow();
                        }
                    }

                    @Override
                    public boolean onMapPoiClick(MapPoi mapPoi) {
                        return false;
                    }
                });

                // 4.定位成功之后才能开启检索
                startSearch(currentLocation);
            }
        }
    }

    private void startSearch(LatLng currentLocation) {
        // 第一步，创建POI检索实例
        PoiSearch poiSearch = PoiSearch.newInstance();

        // 第二步，创建POI检索监听者；
        OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult != null) {
                    List<PoiInfo> allPoi = poiResult.getAllPoi();
                    if (allPoi != null) {
                        if (allPoi.size() > 0) {
                            for (PoiInfo poiInfo : allPoi) {
                                LatLng location = poiInfo.location;
                                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.maker_green);
                                OverlayOptions options = new MarkerOptions()
                                        .position(location).icon(descriptor);

                                Marker marker = (Marker) mBaiduMap.addOverlay(options);
                                String name = poiInfo.name;
                                String city = poiInfo.city;
                                String address = poiInfo.address;
                                String phoneNum = poiInfo.phoneNum;
                                boolean hasCaterDetails = poiInfo.hasCaterDetails;
                                String uid = poiInfo.uid;
                                String info = name + "\n"
                                        + city + "\n"
                                        + address + "\n"
                                        + phoneNum + "\n"
                                        + uid + "\n"
                                        + hasCaterDetails;

                                // 利用bundle 把值传给Marker
                                Bundle bundle = new Bundle();
                                MarkerInfo markerInfo = new MarkerInfo();
                                markerInfo.mAddress = info;
                                markerInfo.mLatLng = location;
                                bundle.putSerializable(MARKER_INFO, markerInfo);
                                marker.setExtraInfo(bundle);
                            }
                        }
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                Log.e(TAG, "onGetPoiResult: poiDetailResult --> " + poiDetailResult);
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                Log.e(TAG, "onGetPoiResult: poiIndoorResult --> " + poiIndoorResult);
            }
        };

        // 第三步，设置POI检索监听者；
        poiSearch.setOnGetPoiSearchResultListener(listener);

        // 第四步，发起检索请求；
        PoiNearbySearchOption option = new PoiNearbySearchOption();
        option.keyword("美食").location(currentLocation)
                .pageNum(10).radius(7000).sortType(PoiSortType.distance_from_near_to_far);
        poiSearch.searchNearby(option);

        // 第五步，释放POI检索实例；
//        poiSearch.destroy();
    }

    private class MapMarkerClickListener implements BaiduMap.OnMarkerClickListener {

        private View mView;

        // 点击覆盖物的回调方法
        @Override
        public boolean onMarkerClick(Marker marker) {
            MarkerInfo markerInfo = (MarkerInfo) marker.getExtraInfo().get(MARKER_INFO);
            if (mView == null) {
                // 显示一个位置的弹出框  InfoWindow
                mView = View.inflate(MapAroundSearchActivity.this, R.layout.ui_map_info_window, null);
            }
            InfoWindow infoWindow = new InfoWindow(mView, markerInfo.mLatLng, -mMarkerHeight);
            TextView textView = mView.findViewById(R.id.address);
            textView.setText(markerInfo.mAddress);
            mBaiduMap.showInfoWindow(infoWindow);

            mCurrentMarker = markerInfo.mLatLng;
            return false;
        }
    }
}

