package com.example.gpsgame;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.*;
import com.mapbox.maps.plugin.LocationPuck;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    MapView mapView;
    Point UserLocation = null;
    PointAnnotationManager pointAnnotationManager;
    PointAnnotation select_mob;
    Random rnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        ImageButton geoButton = (ImageButton) findViewById(R.id.GeoButton);
        ImageButton userButton = (ImageButton) findViewById(R.id.UserButton);
        AnnotationPlugin annotationApi = AnnotationPluginImplKt.getAnnotations(mapView);
        pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationApi, new AnnotationConfig());
        rnd = new Random();
        geoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationComponentUtils.getLocationComponent(mapView).addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            }
        });
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PlayerInfoDialog().show(getSupportFragmentManager(), "UserInfo");
            }
        });
        if(PermissionsManager.areLocationPermissionsGranted(this))
        {
            onMapReady();
        }
        else{
            getLocationPermissions();
        }
    }

    void getLocationPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED  ){
            requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
            return ;
        }
        onMapReady();
    }

    //Проверка на то, что игрок подвинул карту
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        public void onMoveBegin(@NotNull MoveGestureDetector detector) {
            onCameraTrackingDismissed();
        }

        public boolean onMove(@NotNull MoveGestureDetector detector) {
            return false;
        }

        public void onMoveEnd(@NotNull MoveGestureDetector detector) { }};

    private void onCameraTrackingDismissed(){
        LocationComponentUtils.getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        GesturesUtils.getGestures(mapView).removeOnMoveListener(onMoveListener);
    }

    //Постановка камеры в место нахождения игрока

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener()
    {
        public final void onIndicatorPositionChanged(Point it) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(it).build());
            UserLocation = it;
            GesturesUtils.getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(it));
        }
    };

    public void startBattle(PointAnnotation select_mob){
        Intent in = new Intent(MainActivity.this, BattleActivity.class);
        in.putExtra("mob", select_mob.getTextField());
        pointAnnotationManager.delete(select_mob);
        startActivityForResult(in, 1);
    }

    //расчет дистанции между двумя точками
    public static double distance(double startLat, double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));
        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);
        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c; // <-- d
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int lvl = sharedPreferences.getInt("Level", 0);
                int xp = sharedPreferences.getInt("Xp", 0);
                if(data.getBooleanExtra("Victory", true)){
                    xp += data.getIntExtra("xp", 0);
                    while (xp >= (40 + 20 * lvl)){
                        xp -= 40 + 20 * lvl;
                        lvl++;
                    }
                }
                else{
                    if(lvl >= 1){
                        lvl--;
                    }
                    xp = 0;
                }
                editor.putInt("Level", lvl);
                editor.putInt("Xp", xp);
                editor.commit();
                break;
        }
    }

    //Создание 100 врагов в отдельном потоке
    class Summoner extends AsyncTask<Void, Point, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Point position = null;
            while(position == null){
                position = UserLocation;
            }
            for(int i = 0; i < 101; i++) {
                List<Double> crd = position.coordinates();
                Point pt;
                if (rnd.nextBoolean()) {
                    if (rnd.nextBoolean())
                        pt = Point.fromLngLat(crd.get(0) + rnd.nextDouble() * 1e-2, crd.get(1) + rnd.nextDouble() * 1e-2);
                    else
                        pt = Point.fromLngLat(crd.get(0) + rnd.nextDouble() * 1e-2, crd.get(1) - rnd.nextDouble() * 1e-2);
                } else {
                    if (rnd.nextBoolean())
                        pt = Point.fromLngLat(crd.get(0) - rnd.nextDouble() * 1e-2, crd.get(1) + rnd.nextDouble() * 1e-2);
                    else
                        pt = Point.fromLngLat(crd.get(0) - rnd.nextDouble() * 1e-2, crd.get(1) - rnd.nextDouble() * 1e-2);
                }
                Log.d("LOCATION", pt.coordinates().toString());
                publishProgress(pt);
            }
            return null;
        }

        protected void onProgressUpdate(Point... pt) {
            super.onProgressUpdate(pt);
            switch (rnd.nextInt(10)) {
                case 0:
                case 3:
                case 6:
                    addEnemy(pt[0], "red_slime");
                    break;
                case 1:
                case 4:
                case 7:
                    addEnemy(pt[0], "blue_slime");
                    break;
                case 2:
                case 5:
                case 8:
                    addEnemy(pt[0], "green_slime");
                    break;
                case 9:
                    if(rnd.nextBoolean())
                        addEnemy(pt[0], "wolf1");
                    else
                        addEnemy(pt[0], "wolf2");
                    break;
            }
        }
    }

    //Создание врага
    public void addEnemy(Point pt, String type){
        Bitmap sprite;
        switch (type) {
            case "red_slime":
                sprite = bitmapFromDrawableRes(this, R.drawable.red_slime);
                break;
            case "blue_slime":
                sprite = bitmapFromDrawableRes(this, R.drawable.blue_slime);
                break;
            case "green_slime":
                sprite = bitmapFromDrawableRes(this, R.drawable.green_slime);
                break;
            case "wolf1":
                sprite = bitmapFromDrawableRes(this, R.drawable.wolf);
                break;
            case "wolf2":
                sprite = bitmapFromDrawableRes(this, R.drawable.wolfa);
                break;
            default:
                return;
        }
        addAnnotationToMap(pt, sprite, type);
    }


    public void addAnnotationToMap(Point pt, Bitmap sprite, String text){
        if(sprite != null){
            pointAnnotationManager.addClickListener(new OnPointAnnotationClickListener() {
                @Override
                public boolean onAnnotationClick(@NonNull PointAnnotation pointAnnotation) {
                    if(distance(UserLocation.coordinates().get(0), UserLocation.coordinates().get(1), pointAnnotation.getPoint().coordinates().get(0), pointAnnotation.getPoint().coordinates().get(1)) <= 0.065){

                        select_mob = pointAnnotation;
                        ToBattleDialog tbd = new ToBattleDialog(pointAnnotation);
                        tbd.show(getSupportFragmentManager(), "toBattle");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Враг слишком далеко!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withPoint(pt).withIconImage(sprite).withTextField(text).withTextOpacity(0d).withIconSize(0.6f);
            pointAnnotationManager.create(pointAnnotationOptions);
        }
    }



    //Настройка карты

    private void onMapReady() {
        mapView.getMapboxMap().setCamera(
                new CameraOptions.Builder()
                        .zoom(18.0)
                        .build());
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initLocationComponent();
                setupGesturesListener();
                Summoner summoner = new Summoner();
                summoner.execute();
            }
        });
    }

    private void setupGesturesListener() {
        GesturesUtils.getGestures(mapView).addOnMoveListener(onMoveListener);
    }

    private void initLocationComponent(){
        LocationComponentPlugin locationComponentPlugin = LocationComponentUtils.getLocationComponent(mapView);
        locationComponentPlugin.updateSettings((Function1) new Function1() {
            @Override
            public Object invoke(Object o) {
                this.invoke((LocationComponentSettings) o);
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull LocationComponentSettings locationComponentSettings) {
                locationComponentSettings.setEnabled(true);
                locationComponentSettings.setLocationPuck((LocationPuck)new LocationPuck2D(AppCompatResources.getDrawable((Context) MainActivity.this, R.drawable.ic_player) ,AppCompatResources.getDrawable((Context) MainActivity.this, R.drawable.mapbox_user_top_icon), AppCompatResources.getDrawable((Context) MainActivity.this, R.drawable.mapbox_user_icon_shadow), null, 1f));

            }
        });
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
    }

    //Заготовка спрайтов для отображения на карте

    private final Bitmap bitmapFromDrawableRes(Context context, @DrawableRes int resourceId) {
        return this.convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId));
    }

    private final Bitmap convertDrawableToBitmap(Drawable sourceDrawable) {
        if (sourceDrawable == null) {
            return null;
        }
        else{
            Bitmap bt;
            if(sourceDrawable instanceof BitmapDrawable){
                bt = ((BitmapDrawable)sourceDrawable).getBitmap();
            }
            else{
                Drawable.ConstantState constantState = sourceDrawable.getConstantState();
                if(constantState == null){
                    return null;
                }
                Drawable drawable = constantState.newDrawable().mutate();
                bt = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bt);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }
            return bt;
        }
    }







    protected void onDestroy(){
        super.onDestroy();
        LocationComponentUtils.getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        GesturesUtils.getGestures(mapView).removeOnMoveListener(onMoveListener);
    }
}