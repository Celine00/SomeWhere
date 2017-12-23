package crystal.somewhere;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, LocationSource, AMapLocationListener {
    private MapView mMapView = null;    //地图控件
    private AMap aMap = null;      //地图对象
    private AMapLocationClient mLocationClient = null;      //定位发起端
    private AMapLocationClientOption mLocationOption = null;       //定位参数
    private OnLocationChangedListener mListener = null;     //定位监听器
    private boolean isFirstLoc = true;  //是否只显示一次定位信息和用户重新定位
    private double latitude, longitude;
    private Marker curShowWindowMarket;
    private InfoWinAdapter adapter;

    private mDataBase mDataBase;
    private SQLiteDatabase database;

    private MoveImageView image;
    private ImageButton add, delete, update;
    private EditText name_edit, description_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initLocation();

        //SQLite数据库
        mDataBase = new mDataBase(this);
        database = mDataBase.getReadableDatabase();

        //显示地图
        mMapView.onCreate(savedInstanceState);

        //获取地图对象
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //显示定位按钮，默认添加缩放控件
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);

        //aMap绑定监听器
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnInfoWindowClickListener(this);

        //自定义marker的信息框infoWindow
        adapter = new InfoWinAdapter(this);
        aMap.setInfoWindowAdapter(adapter);

        //设置定位的小图标,默认蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        aMap.setMyLocationStyle(myLocationStyle);

        //添加marker事件
        addClick();
    }

    //初始化控件
    private void initView() {
        mMapView = (MapView) findViewById(R.id.Map);
        image = (MoveImageView) findViewById(R.id.image);
        add = (ImageButton)findViewById(R.id.add);
        delete = (ImageButton)findViewById(R.id.delete);
        update = (ImageButton)findViewById(R.id.update);
    }

    //开始定位
    private void initLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //高精度定位
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //返回定位信息
        mLocationOption.setNeedAddress(true);
        //设置定位间隔
        mLocationOption.setInterval(2000);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    //定位回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAddress();//获取地址
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }
            } else {
                //显示错误信息：ErrCode是错误码，errInfo是错误信息
                Log.e("AmapError", "location Error, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (curShowWindowMarket != null) {
            curShowWindowMarket.hideInfoWindow();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        double latitude1 = marker.getPosition().latitude;
        double longitude1 = marker.getPosition().longitude;
        ArrayList<String> result = mDataBase.query(latitude1, longitude1);
        if (result != null && result.size() > 0) {
            marker.setTitle(result.get(0));
            marker.setSnippet(result.get(1));
        }
        curShowWindowMarket = marker;
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(getApplicationContext(), "跳转detail界面", Toast.LENGTH_SHORT).show();
        marker.hideInfoWindow();
    }

    private void addClick() {
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取marker在屏幕上的横、纵坐标
                int[] location = new int[2];
                image.getLocationOnScreen(location);
                Point point = new Point(location[0]+75, location[1]+65);    //坐标->经纬度的差值
                LatLng latLng = aMap.getProjection().fromScreenLocation(point);
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                //某个位置添加marker
                final MarkerOptions otMarkerOptions = new MarkerOptions();
                otMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
                otMarkerOptions.position(latLng);
                otMarkerOptions.draggable(false);        //marker不可拖动
                final Marker marker = aMap.addMarker(otMarkerOptions);

                //弹出自定义对话框
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View view_in = inflater.inflate(R.layout.dialog, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(view_in);

                name_edit = view_in.findViewById(R.id.name_edit);
                description_edit = view_in.findViewById(R.id.description_edit);

                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newName = "";
                        String newDescription = "";
                        if (!TextUtils.equals(name_edit.getText().toString(), "")) {
                            newName = name_edit.getText().toString();
                            newDescription = description_edit.getText().toString();
                            mDataBase.insert(latitude, longitude, newName, newDescription);
                        }
                        else {
                            marker.setVisible(false);
                        }
                    }
                });
                builder.setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        marker.setVisible(false);
                    }
                });
                builder.create().show();
            }
        });
    }
}

