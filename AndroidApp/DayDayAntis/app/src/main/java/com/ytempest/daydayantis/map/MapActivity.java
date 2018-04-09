package com.ytempest.daydayantis.map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.ytempest.daydayantis.R;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";
    private final static String MARKER_INFO = "marker_info";


    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MapLocationListener mMapLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 这个方法要在setContentView 方法之前
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        initMap();


    }

    private void initMap() {

        mMapView = findViewById(R.id.map_view);

        mLocationClient = new LocationClient(getApplicationContext());
        initLocationOption();
        mMapLocationListener = new MapLocationListener();
        mLocationClient.registerLocationListener(mMapLocationListener);
        mLocationClient.start();

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(new MapMarkerClickListener());
        // 设置地图的缩放级别
        MapStatus build = new MapStatus.Builder().zoom(18).build();
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
                OverlayOptions overlayOptions = new MarkerOptions().position(currentLocation).icon(descriptor);
                Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);


                // 3.利用bundle 把值传给Marker
                Bundle bundle = new Bundle();
                MarkerInfo markerInfo = new MarkerInfo();
                markerInfo.mInfo = location.getAddrStr();
                markerInfo.mLatLng = currentLocation;
                bundle.putSerializable(MARKER_INFO, markerInfo);
                marker.setExtraInfo(bundle);
            }
        }
    }

    private class MapMarkerClickListener implements BaiduMap.OnMarkerClickListener {

        // 点击覆盖物的回调方法
        @Override
        public boolean onMarkerClick(Marker marker) {
            MarkerInfo markerInfo = (MarkerInfo) marker.getExtraInfo().get(MARKER_INFO);

            // 显示一个位置的弹出框  InfoWindow
            View view = View.inflate(MapActivity.this, R.layout.ui_map_info_window, null);
            InfoWindow infoWindow = new InfoWindow(view, markerInfo.mLatLng, -50);
            Log.e(TAG, "onMarkerClick: infoWindow --> " + infoWindow);
            TextView textView = view.findViewById(R.id.tv_info);
            textView.setText(markerInfo.mInfo);
            mBaiduMap.showInfoWindow(infoWindow);

            return false;
        }
    }
}
