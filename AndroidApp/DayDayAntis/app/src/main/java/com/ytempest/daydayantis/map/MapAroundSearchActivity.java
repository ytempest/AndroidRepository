package com.ytempest.daydayantis.map;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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
import com.ytempest.baselibrary.ioc.CheckNet;
import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.daydayantis.R;
import com.ytempest.framelibrary.base.BaseSkinActivity;

import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class MapAroundSearchActivity extends BaseSkinActivity {
    private static final String TAG = "MapAroundSearchActivity";
    private final static String TYPE_MARK = "type_mark";
    private final static int CURRENT_LOCATION_MARK = 100;
    private final static int SEARCH_POINT_MARK = 200;
    private final static String CURRENT_LOCATION_INFO = "current_location_info";
    private final static String SEARCH_POINT_INFO = "search_point_info";


    @ViewById(R.id.map_view)
    private MapView mMapView;
    @ViewById(R.id.et_key_word)
    private EditText mKeyWordEt;
    @ViewById(R.id.et_num)
    private EditText mSearchnumEt;
    @ViewById(R.id.et_radius)
    private EditText mSearchRadiusEt;
    @ViewById(R.id.bt_search)
    private Button mSearchBt;

    private BaiduMap mBaiduMap;
    /**
     * 管理定位的对象
     */
    private LocationClient mLocationClient;
    private MapAroundSearchActivity.MapLocationListener mMapLocationListener;
    private LatLng mCurrentMarker;
    private int mMarkerHeight;
    private LatLng mCurrentLocation;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_map_around_search;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        // 定位到当前位置
        initLocation();
        mBaiduMap = mMapView.getMap();
        // 设置覆盖物的点击事件
        mBaiduMap.setOnMarkerClickListener(new MapMarkerClickListener());
        // 设置地图的缩放级别
        MapStatus build = new MapStatus.Builder().zoom(14.8f).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(build));

        // 给地图设置点击事件
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 如果点击点的经纬度和显示的覆盖物的经纬度不一样就隐藏覆盖物的信息框
                if (mCurrentMarker.latitude != latLng.latitude
                        && mCurrentMarker.longitude != latLng.longitude) {
                    mBaiduMap.hideInfoWindow();
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                showToastShort("名称：" + mapPoi.getName() + "\n" + "经纬度：" + mapPoi.getPosition());
                return false;
            }
        });

/*        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor));*/


    }

    @CheckNet
    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        option.setCoorType("bd09ll");

        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 设置定位的频率，单位毫秒
        int span = 1000;
        option.setScanSpan(span);
        option.setIsNeedAddress(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
        mMapLocationListener = new MapLocationListener();
        // 注册位置监听器
        mLocationClient.registerLocationListener(mMapLocationListener);
        // 开启定位
        mLocationClient.start();
    }


    /**
     * @author ytempest
     *         Description：
     */
    private class MapLocationListener extends BDAbstractLocationListener {
        /**
         * 定位信息的回调方法
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null) {
                // 定位一次成功之后不需要再次定位，这时注销监听器
                mLocationClient.unRegisterLocationListener(mMapLocationListener);
                // 停止定位
                mLocationClient.stop();

                // 1.把地图移动到当前自己定位的位置
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                mCurrentLocation = new LatLng(latitude, longitude);
                mCurrentMarker = mCurrentLocation;
                MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLng(mCurrentLocation);
                mBaiduMap.setMapStatus(statusUpdate);

                // 2.在自己的位置添加一个覆盖物
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_worker);
                mMarkerHeight = descriptor.getBitmap().getHeight() - 30;
                OverlayOptions overlayOptions = new MarkerOptions().position(mCurrentLocation).icon(descriptor);
                Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);

                // 3.利用bundle 把值传给Marker
                Bundle bundle = new Bundle();
                MarkerInfo markerInfo = new MarkerInfo();
                markerInfo.mInfo = location.getAddrStr();
                markerInfo.mLatLng = mCurrentLocation;
                bundle.putSerializable(CURRENT_LOCATION_INFO, markerInfo);
                // 设置覆盖物的类型
                bundle.putInt(TYPE_MARK, CURRENT_LOCATION_MARK);
                marker.setExtraInfo(bundle);
            }
        }
    }


    /**
     * @author ytempest
     *         Description：
     */
    private class MapMarkerClickListener implements BaiduMap.OnMarkerClickListener {

        private View mInfoView;
        private TextView mInfoText;

        /**
         * 点击覆盖物的回调方法
         */
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (mInfoView == null) {
                // 显示一个位置的弹出框  InfoWindow
                mInfoView = View.inflate(MapAroundSearchActivity.this, R.layout.ui_map_info_window, null);
                mInfoText = mInfoView.findViewById(R.id.tv_info);
            }
            int typeMark = marker.getExtraInfo().getInt(TYPE_MARK);
            switch (typeMark) {
                case CURRENT_LOCATION_MARK: {
                    MarkerInfo markerInfo = (MarkerInfo) marker.getExtraInfo().get(CURRENT_LOCATION_INFO);
                    if (markerInfo != null) {
                        mInfoText.setText(markerInfo.mInfo);
                        InfoWindow infoWindow = new InfoWindow(mInfoView, markerInfo.mLatLng, -mMarkerHeight);
                        mBaiduMap.showInfoWindow(infoWindow);
                        mCurrentMarker = markerInfo.mLatLng;
                    }
                    break;
                }

                case SEARCH_POINT_MARK: {
                    MarkerInfo markerInfo = (MarkerInfo) marker.getExtraInfo().get(SEARCH_POINT_INFO);
                    if (markerInfo != null) {
                        mInfoText.setText(markerInfo.mInfo);
                        InfoWindow infoWindow = new InfoWindow(mInfoView, markerInfo.mLatLng, -mMarkerHeight);
                        mBaiduMap.showInfoWindow(infoWindow);
                        mCurrentMarker = markerInfo.mLatLng;
                    }
                    break;
                }

                default:
                    break;

            }
            return false;
        }
    }

    @OnClick(R.id.bt_search)
    private void onSearchClick(View view) {
        String keyword = mKeyWordEt.getText().toString().trim();
        String searchNum = mSearchnumEt.getText().toString().trim();
        String searchRadius = mSearchRadiusEt.getText().toString().trim();
        if (!TextUtils.isEmpty(keyword)
                &&!TextUtils.isEmpty(searchNum)
                &&!TextUtils.isEmpty(searchRadius)) {
            startSearch(mCurrentLocation, keyword, Integer.parseInt(searchNum), Integer.parseInt(searchRadius));
        } else {
            showToastShort("请输入所有数据");
        }
    }

    private void startSearch(LatLng currentLocation, String keyword,int searchNum, int searchRadius) {
        if (currentLocation == null) {
            return;
        }
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
                            Log.e(TAG, "onGetPoiResult: allPoi.size --> " + allPoi.size());
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
                                String info = name + "\n"
                                        + city + "\n"
                                        + address + "\n"
                                        + phoneNum;

                                // 利用bundle 把值传给Marker
                                Bundle bundle = new Bundle();
                                MarkerInfo markerInfo = new MarkerInfo();
                                markerInfo.mInfo = info;
                                markerInfo.mLatLng = location;
                                bundle.putSerializable(SEARCH_POINT_INFO, markerInfo);
                                // 设置覆盖物的类型
                                bundle.putInt(TYPE_MARK, SEARCH_POINT_MARK);
                                marker.setExtraInfo(bundle);
                            }
                        }
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            }
        };

        // 第三步，设置POI检索监听者；
        poiSearch.setOnGetPoiSearchResultListener(listener);

        // 第四步，发起检索请求；
        PoiNearbySearchOption option = new PoiNearbySearchOption();
        option.keyword(keyword).location(currentLocation)
                .pageNum(searchNum).radius(searchRadius).sortType(PoiSortType.distance_from_near_to_far);
        poiSearch.searchNearby(option);

        // 第五步，释放POI检索实例；
        poiSearch.destroy();
    }
}

