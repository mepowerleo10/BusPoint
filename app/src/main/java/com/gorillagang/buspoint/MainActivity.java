package com.gorillagang.buspoint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.gorillagang.buspoint.data.Journey;
import com.gorillagang.buspoint.data.OverpassApiResponse;
import com.gorillagang.buspoint.data.Stop;
import com.gorillagang.buspoint.ui.account.LoginActivity;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.DirectionsWaypoint;
import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.graphics.Color.parseColor;
import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lineProgress;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineGradient;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

@SuppressWarnings("SpellCheckingInspection")
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        PermissionsListener, MapboxMap.OnMapLongClickListener, MapboxMap.OnMapClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DESTINATION_MARKER = "DESTINATION_MARKER";
    private static final String DESTINATION_MARKER_SOURCE = "DESTINATION_MARKER_SOURCE";
    private static final String DESTINATION_MARKER_LAYER = "DESTINATION_MARKER_LAYER";
    private static final String DESTINATION_INFO_LAYER = "DESTINATION_INFO_LAYER";
    private static final String PROPERTY_SELECTED = "SELECTED";
    private static final String DESTINATION_STOP_MARKER_SOURCE = "DESTINATION_STOP_MARKER_SOURCE";
    private static final String DESTINATION_STOP_MARKER_LAYER = "DESTINATION_STOP_MARKER_LAYER";
    private static final String SOURCE_STOP_MARKER_LAYER = "SOURCE_STOP_MARKER_LAYER";
    private static final String SOURCE_STOP_MARKER_SOURCE = "SOURCE_STOP_MARKER_SOURCE";
    private static final String ROUTE_LAYER_ID = "ROUTE_LAYER_ID";
    private static final String ROUTE_LINE_SOURCE_ID = "ROUTE_LINE_SOURCE_ID";
    private static final Float ROUTE_LINE_WIDTH = 6f;
    private static final String ORIGIN_COLOR = "#2096F3";
    private static final String DESTINATION_COLOR = "#F84D4D";
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private static final double DEFAULT_CAMERA_ZOOM = 16;
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 2;
    private static final LatLng DEFAULT_REGION_BOUND_ONE = new LatLng(35.09455866, -7.07152802);
    private static final LatLng DEFAULT_REGION_BOUND_TWO = new LatLng(36.2628098, -5.40889002);
    private final MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    public MapboxMap mapboxMap;
    private Toolbar toolbar;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    private Layer destinationMarkerLayer;
    private Marker destinationNearestStopMarker;
    private Marker sourceNearestStopMarker;
    private final List<Journey> journeyList = new ArrayList<>();
    private Point origin;
    private Point destination;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private boolean simulateRoute;
    private SharedPreferences sharedPreferences;
    private Menu mOptionsMenu;
    private CardView journeyCardView;
    private BottomSheetBehavior journeySheetBehaviour;
    private List<Point> waypoints;
    private Button clearWaypointsBtn;
    private Button startJourneyBtn;
    private Button getMyLocationBtn;
    private Marker midStopMarker;
    private Button showJourneySheetBtn;

    private CardView progressInfo;
    private TextView progressInfoText;
    private ProgressBar progressInfoBar;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private CoordinatorLayout journeySheet;
    private String serverIpAddress = "192.168.0.101";

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;
        //noinspection SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection
        mapboxMap.setStyle(
                new Style
                        .Builder()
                        .fromUri("mapbox://styles/mepowerleo10/ckpg3ymi300ue18mrll8ehbyk"),
                style -> {
                    enableLocationComponent(style);
                    initSources(style);
                });
    }

    @SuppressLint({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.getLocationComponent();
            LocationComponentActivationOptions options = LocationComponentActivationOptions
                    .builder(this, loadedMapStyle)
                    .useDefaultLocationEngine(false)
                    .build();
            // Activate location locationComponent with options
            locationComponent.activateLocationComponent(options);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initSources(@NonNull Style loadedStyle) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_mapbox_marker_icon_blue, null);
        Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
        loadedStyle.addImage(DESTINATION_MARKER, mBitmap);
        loadedStyle.addSource(new GeoJsonSource(DESTINATION_MARKER_SOURCE));
        loadedStyle.addLayer(new SymbolLayer(DESTINATION_MARKER_LAYER,
                DESTINATION_MARKER_SOURCE)
                .withProperties(
                        iconImage(DESTINATION_MARKER),
                        visibility(NONE),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true)
                ));
        loadedStyle.addLayer(new SymbolLayer(DESTINATION_INFO_LAYER,
                DESTINATION_MARKER_SOURCE)
                .withProperties(
                        iconImage("{name}"),
                        iconAnchor(ICON_ANCHOR_BOTTOM),
                        iconAllowOverlap(true),
                        iconOffset(new Float[]{-2f, -28f})
                )
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true)))
        );

        loadedStyle.addLayer(new LineLayer(ROUTE_LAYER_ID,
                ROUTE_LINE_SOURCE_ID).withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_MITER),
                lineWidth(ROUTE_LINE_WIDTH),
                lineGradient(interpolate(
                        linear(),
                        lineProgress(),
                        stop(0f, color(parseColor(ORIGIN_COLOR))),
                        stop(1f, color(parseColor(DESTINATION_COLOR)))
                ))
        ));
    }

    @SuppressLint({"MissingPermission"})
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest request =
                new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                        .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        mOptionsMenu = menu;
        updateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    private void updateOptionsMenu() {
        if (user != null) {
            mOptionsMenu.findItem(R.id.action_login).setVisible(false);
            mOptionsMenu.findItem(R.id.action_account).setVisible(true);
            mOptionsMenu.findItem(R.id.action_logout).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                return true;
            case R.id.action_login:
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(i, LOGIN_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.action_logout:
                if (user != null) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setIcon(R.drawable.ic_baseline_account_circle_24)
                            .setTitle("Log Out")
                            .setMessage(getString(R.string.sure_to_logout)
                                    + user.getDisplayName() + "?")
                            .setPositiveButton(R.string.logout, (dialog1, which) -> {
                                mAuth.signOut();
                                recreate();
                                LatLng point = new LatLng(getLastKnownLocation().getLatitude(),
                                        getLastKnownLocation().getLongitude());
                                animateCamera(point);
                                updateOptionsMenu();
                            })
                            .setNegativeButton(getString(R.string.cancel), (dialog1, which) -> {

                            })
                            .create();
                    dialog.show();

                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_search:
                searchPlace();
                return true;
            case R.id.action_simulate_route:
                if (item.isChecked()) {
                    item.setChecked(false);
                    simulateRoute = false;
                    Toast.makeText(this, getString(R.string.route_sim_disabled), Toast.LENGTH_SHORT).show();
                } else {
                    item.setChecked(true);
                    simulateRoute = true;
                    Toast.makeText(this, getString(R.string.route_sim_enabled), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_ip_addr:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle("Change the Server IP Address");
                EditText ipField = new EditText(getApplicationContext());
                ipField.setInputType(InputType.TYPE_CLASS_TEXT);
                ipField.setText(serverIpAddress);
                dialogBuilder.setView(ipField);
                dialogBuilder.setPositiveButton("Change", (dialog, which) -> {
                    serverIpAddress = ipField.getText().toString();
                });
                dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                });
                dialogBuilder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Location getLastKnownLocation() {
        if (locationComponent != null) {
            return locationComponent.getLastKnownLocation();
        }
        return null;
    }

    private void animateCamera(LatLng latLng) {
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .zoom(DEFAULT_CAMERA_ZOOM)
                .tilt(30)
                .build();
        mapboxMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(position),
                CAMERA_ANIMATION_DURATION
        );
    }

    private void searchPlace() {
        List<Place.Field> fields =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        RectangularBounds bounds = RectangularBounds.newInstance(
                new com.google.android.gms.maps.model.LatLng(
                        DEFAULT_REGION_BOUND_ONE.getLatitude(), DEFAULT_REGION_BOUND_ONE.getLongitude()
                ),
                new com.google.android.gms.maps.model.LatLng(
                        DEFAULT_REGION_BOUND_TWO.getLatitude(), DEFAULT_REGION_BOUND_TWO.getLongitude()
                )
        );

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("TZ")
                .setLocationBias(bounds)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (destinationNearestStopMarker != null) {
                    mapboxMap.removeMarker(destinationNearestStopMarker);
                    destinationNearestStopMarker = null;
                }
                if (sourceNearestStopMarker != null) {
                    mapboxMap.removeMarker(sourceNearestStopMarker);
                    sourceNearestStopMarker = null;
                }
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng point = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                LatLng source = new LatLng(getLastKnownLocation().getLatitude(),
                        getLastKnownLocation().getLongitude());
                origin = Point.fromLngLat(source.getLongitude(), source.getLatitude());
                destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                waypoints.add(origin);
                setDestinationMarker(
                        destination,
                        mapboxMap.getStyle());
                getStopNearMe(source, point); // get the nearest stop at the source

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Timber.tag(TAG).i(status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Timber.tag(TAG).i(getString(R.string.user_canceled_op));
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.places_api_key));
        }

        PlacesClient placesClient = Places.createClient(this);

        sharedPreferences = this.getSharedPreferences(
                getString(R.string.preference_file),
                Context.MODE_PRIVATE
        );

        mapView = findViewById(R.id.mapView);
        progressInfo = findViewById(R.id.progress_info);
        progressInfoText = findViewById(R.id.progress_info_text);
        progressInfoBar = findViewById(R.id.progress_info_bar);
        clearWaypointsBtn = findViewById(R.id.button_clear_waypoints);
        waypoints = new ArrayList<>();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        journeySheet = findViewById(R.id.journey_sheet);
        journeyCardView = findViewById(R.id.card_journey_details);
        hideBottomSheet();

        startJourneyBtn = findViewById(R.id.button_start_journey);
        startJourneyBtn.setOnClickListener(v -> {
            if (currentRoute != null) {
                startNavigationActivity();
            } else if (destinationNearestStopMarker != null && sourceNearestStopMarker != null) {
                getRoute(origin, destination);
            } else {
                Toast.makeText(this,
                        "Oops! Seems we can't get the route.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        startJourneyBtn.setOnLongClickListener(v -> {
            if (journeySheetBehaviour.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                journeySheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                return true;
            }

            return false;
        });

        getMyLocationBtn = findViewById(R.id.button_get_my_location);
        getMyLocationBtn.setOnClickListener(v -> animateCamera(new LatLng(getLastKnownLocation().getLatitude(),
                getLastKnownLocation().getLongitude())));

        clearWaypointsBtn.setOnClickListener(v -> {
            if (waypoints != null && waypoints.size() > 0) {
                waypoints.clear();
            }
            navigationMapRoute.updateRouteVisibilityTo(false);
            currentRoute = null;
            if (destination != null) {
            }
            mapboxMap.removeMarker(destinationNearestStopMarker);
            mapboxMap.removeMarker(sourceNearestStopMarker);
            startJourneyBtn.setVisibility(View.GONE);
            clearWaypointsBtn.setVisibility(View.GONE);
        });

        journeySheet = findViewById(R.id.journey_sheet);
        showJourneySheetBtn = findViewById(R.id.button_open_journey_sheet);
        showJourneySheetBtn.setOnClickListener(v -> {
            showBottomSheet();
        });

        requestQueue = Volley.newRequestQueue(this);
        requestQueue.addRequestFinishedListener(request -> {
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    private void hideBottomSheet() {
        journeySheetBehaviour = BottomSheetBehavior.from(journeyCardView);
        if (journeySheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            journeySheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void startNavigationActivity() {
        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                .shouldSimulateRoute(simulateRoute)
                .directionsRoute(currentRoute)
                .waynameChipEnabled(true)
                .build();
        NavigationLauncher.startNavigation(MainActivity.this, options);
    }

    private void getRoute(@NonNull Point origin, @NonNull Point destination) {
        if (waypoints.size() < 0) return;

        String url = Uri.parse("http://" + serverIpAddress + ":8000/api/get-route")
                .buildUpon()
                .appendQueryParameter("start_lat", String.valueOf(sourceNearestStopMarker.getPosition().getLatitude()))
                .appendQueryParameter("start_lon", String.valueOf(sourceNearestStopMarker.getPosition().getLongitude()))
                .appendQueryParameter("final_lat", String.valueOf(destinationNearestStopMarker.getPosition().getLatitude()))
                .appendQueryParameter("final_lon", String.valueOf(destinationNearestStopMarker.getPosition().getLongitude()))
                .build().toString();
        hideJorneyInfo();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url, null,
                response -> {
                    Log.i(TAG, String.valueOf(response));
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                    Journey journey = gson.fromJson(response.toString(), Journey.class);
                    journeyList.add(journey);
                    showJourneySheetBtn.setVisibility(View.VISIBLE);
                    showJourneyInfo();
//                    calculateRoute();
                    Log.i(TAG, "TEST Journey: " + journey.getDateTime().toString());
                    hideProgressInfo();
                },
                error -> {
                    Log.i(TAG, error.toString());
                    progressInfoText.setText(R.string.problems_calculating_route);
                    progressInfo.setVisibility(View.GONE);
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        requestQueue.add(request);
    }

    private void showBottomSheet() {
//        journeySheet.setVisibility(View.VISIBLE);
        journeySheetBehaviour = BottomSheetBehavior.from(journeyCardView);
        if (journeySheetBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            journeySheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void hideJorneyInfo() {
        ((View) journeyCardView.findViewById(R.id.mid_route_container))
                .setVisibility(View.GONE);
        ((View) journeyCardView.findViewById(R.id.midroute_arrow))
                .setVisibility(View.GONE);
        ((TextView) journeyCardView.findViewById(R.id.mid_route_description))
                .setText(R.string.board_off_here);
        hideBottomSheet();
        showJourneySheetBtn.setVisibility(View.VISIBLE);
    }

    private void showJourneyInfo() {
        Journey journey = journeyList.get(journeyList.size() - 1);
        ((TextView) journeyCardView.findViewById(R.id.from_route_description))
                .setText(journey.getRoutes().get(0).getName());
        ((TextView) journeyCardView.findViewById(R.id.from_stop_description))
                .setText(journey.getStartStop().getName());
        ((TextView) journeyCardView.findViewById(R.id.to_route_description))
                .setText(journey.getRoutes().get(1).getName());
        ((TextView) journeyCardView.findViewById(R.id.to_stop_description))
                .setText(journey.getFinalStop().getName());
        ((TextView) journeyCardView.findViewById(R.id.journey_total_price))
                .setText(journey.getCost() + " TShs.");

        if (journey.getMidStop() != null) {
            Stop midStop = journey.getMidStop();
            MarkerOptions midStopMarkerOptions = new MarkerOptions()
                    .setTitle(midStop.getName())
                    .setPosition(new LatLng(midStop.getLat(), midStop.getLon()));
            midStopMarker = mapboxMap.addMarker(midStopMarkerOptions);

            ((View) journeyCardView.findViewById(R.id.mid_route_container))
                    .setVisibility(View.VISIBLE);
            ((View) journeyCardView.findViewById(R.id.midroute_arrow))
                    .setVisibility(View.VISIBLE);
            ((TextView) journeyCardView.findViewById(R.id.mid_route_description))
                    .setText(R.string.board_off_here);
            ((TextView) journeyCardView.findViewById(R.id.mid_stop_description))
                    .setText(midStop.getName());
            ((View) journeyCardView.findViewById(R.id.mid_route_container))
                    .setOnClickListener(v -> {
                        animateCamera(new LatLng(midStop.getLat(), midStop.getLon()));
                    });
        }

        ((View) journeyCardView.findViewById(R.id.from_route_container))
                .setOnClickListener(v -> {
                    animateCamera(new LatLng(journey.getStartStop().getLat(),
                            journey.getStartStop().getLon()));
                });
        ((View) journeyCardView.findViewById(R.id.to_route_container))
                .setOnClickListener(v -> {
                    animateCamera(new LatLng(journey.getFinalStop().getLat(),
                            journey.getFinalStop().getLon()));
                });

        showBottomSheet();
    }

    private void calculateRoute() {
        MapboxDirections.Builder builder = MapboxDirections.builder();
        List<Stop> busStops = journeyList.get(journeyList.size() - 1).getRoutingStops();
        List<Point> midPoints = new ArrayList<>();
        for (Stop s : busStops) {
            midPoints.add(Point.fromLngLat(s.getLon(), s.getLat()));
        }
        builder.addWaypoint(origin);
        for (int i = 0; i < midPoints.size(); i++) {
            builder.addWaypoint(midPoints.get(i));
        }
        builder.addWaypoint(destination);
        builder.profile(DirectionsCriteria.PROFILE_DRIVING);
        builder.profile(DirectionsCriteria.OVERVIEW_FULL);
        builder.accessToken(Mapbox.getAccessToken());
        builder.build().enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    Log.e(TAG, response.message());
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                } else {
                    currentRoute = response.body().routes().get(0);
                    navigationMapRoute.addRoute(currentRoute);
                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(new LatLng(origin.latitude(), origin.longitude()))
                            .include(new LatLng(destination.latitude(), destination.longitude()))
                            .build();
                    animateCameraToBounds(bounds);
                }

// Retrieve the directions route from the API response
                currentRoute = response.body().routes().get(0);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    private void animateCameraToBounds(LatLngBounds bounds) {
        CameraPosition position = mapboxMap.getCameraForLatLngBounds(bounds);
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),
                CAMERA_ANIMATION_DURATION);
    }

    private void hideProgressInfo() {
        progressInfo.setVisibility(View.GONE);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (mOptionsMenu != null) {
            updateOptionsMenu();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getStopNearMe(LatLng point, LatLng destination) {
        @SuppressLint("DefaultLocale") String uri = Uri.parse("https://overpass-api.de/api/interpreter")
                .buildUpon()
                .appendQueryParameter("data",
                        String.format(getString(R.string.overpass_api_query), 1100,
                                point.getLatitude(), point.getLongitude()))
                .build().toString();
        Log.i(TAG, String.format("URI: %s", uri));
        showProgressInfo(getString(R.string.finding_nearest_bus_stop));
        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        uri, null,
                        response -> {
                            Log.i(TAG, "Call Responded");
                            Gson gson = new Gson();
                            OverpassApiResponse apiResponse = gson.fromJson(response.toString(), OverpassApiResponse.class);
                            findShortestDistance(apiResponse.elements, point, true);
                            getStopNearDestination(destination);
                        },
                        error -> Log.d("Error%s", String.valueOf(error))
                );
        requestQueue.add(request);
    }

    private void getStopNearDestination(LatLng point) {
        @SuppressLint("DefaultLocale") String uri = Uri.parse("https://overpass-api.de/api/interpreter")
                .buildUpon()
                .appendQueryParameter("data",
                        String.format(getString(R.string.overpass_api_query), 1100,
                                point.getLatitude(), point.getLongitude()))
                .build().toString();
        Log.i(TAG, String.format("URI: %s", uri));
        showProgressInfo(getString(R.string.finding_destination_bus_stop));
        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        uri, null,
                        response -> {
                            Log.i(TAG, "Call Responded");
                            Gson gson = new Gson();
                            OverpassApiResponse apiResponse = gson.fromJson(response.toString(), OverpassApiResponse.class);
                            findShortestDistance(apiResponse.elements, point, false);
                        },
                        error -> Log.d("Error%s", String.valueOf(error))
                );
        requestQueue.add(request);
    }

    private void setDestinationMarker(@NonNull Point destination, @NonNull Style loadedStyle) {
        if (loadedStyle.getLayer(DESTINATION_MARKER_LAYER) != null) {
            GeoJsonSource source = loadedStyle.getSourceAs(DESTINATION_MARKER_SOURCE);
            if (source != null) {
                source.setGeoJson(destination);
            }
            destinationMarkerLayer = loadedStyle.getLayer(DESTINATION_MARKER_LAYER);
            if (destinationMarkerLayer != null) {
                destinationMarkerLayer.setProperties(visibility(Property.VISIBLE));
                animateCamera(new LatLng(destination.latitude(), destination.longitude()));
            }
        }
    }

    private void getNearestStop(@NonNull LatLng point, boolean isSource) {
        @SuppressLint("DefaultLocale") String uri = Uri.parse("https://overpass-api.de/api/interpreter")
                .buildUpon()
                .appendQueryParameter("data",
                        String.format(getString(R.string.overpass_api_query), 1100,
                                point.getLatitude(), point.getLongitude()))
                .build().toString();
        Log.i(TAG, String.format("URI: %s", uri));
        if (isSource) {
            showProgressInfo(getString(R.string.finding_nearest_bus_stop));
        } else {
            showProgressInfo(getString(R.string.finding_destination_bus_stop));
        }
        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        uri, null,
                        response -> {
                            Log.i(TAG, "Call Responded");
                            Gson gson = new Gson();
                            OverpassApiResponse apiResponse = gson.fromJson(response.toString(), OverpassApiResponse.class);
                            findShortestDistance(apiResponse.elements, point, isSource);
                        },
                        error -> Log.d("Error%s", String.valueOf(error))
                );
        requestQueue.add(request);
    }

    private void showProgressInfo(String message) {
        progressInfoText.setText(message);
        progressInfo.setVisibility(View.VISIBLE);
    }

    private void findShortestDistance(List<OverpassApiResponse.QueryElement> results,
                                      LatLng point,
                                      boolean isSource) {
        Log.i(TAG, "Calculating Shortest Distance");
        if (results != null) {
            List<Point> coordinates = new ArrayList<>();
            for (OverpassApiResponse.QueryElement e : results) {
                coordinates.add(Point.fromLngLat(e.lon, e.lat));
            }
            coordinates.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            if (coordinates.size() <= 1) {
                progressInfo.setVisibility(View.GONE);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
                dialogBuilder.setTitle(getString(R.string.couldnt_find_stops_near_destination));
                return;
            }
            MapboxMatrix matrixApiClient = MapboxMatrix.builder()
                    .accessToken(Mapbox.getAccessToken())
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .coordinates(coordinates)
                    .addAnnotations(DirectionsCriteria.ANNOTATION_DISTANCE)
                    .build();
            matrixApiClient.enqueueCall(new Callback<MatrixResponse>() {
                @Override
                public void onResponse(Call<MatrixResponse> call,
                                       Response<MatrixResponse> response) {
                    if (response.body() != null) {
                        int len = response.body().distances().size();
                        if (len > 0) {
                            Double[] distances = response.body().distances().get(len - 1);
                            List<DirectionsWaypoint> points = response.body().destinations();
                            DirectionsWaypoint shortest = points.get(0);
                            double lastShortest = distances[0];
                            int i = 0;
                            int shortestIndex = 0;
                            for (Double d : distances) {
                                if (d > 0.0 && lastShortest > d) {
                                    shortest = points.get(i);
                                    lastShortest = d;
                                    shortestIndex = i;

                                    Log.i(TAG, shortest.location().toString() + " is shortest");
                                }
                                i++;
                            }
                            LatLng markerLatLng = new LatLng(
                                    shortest.location().latitude(),
                                    shortest.location().longitude()
                            );
                            Point p = Point.fromLngLat(markerLatLng.getLongitude(),
                                    markerLatLng.getLatitude());
                            waypoints.add(p);
                            Log.i(TAG, "Point: " + p.toString());
                            OverpassApiResponse.QueryElement e = results.get(shortestIndex);
                            MarkerOptions options = new MarkerOptions()
                                    .position(new LatLng(e.lat, e.lon))
                                    .title(e.tags.name);
                            if (isSource) {
                                sourceNearestStopMarker = mapboxMap.addMarker(options);
                                animateCamera(new LatLng(e.lat, e.lon));
                            } else {
                                //noinspection SpellCheckingInspection
                                options.snippet("500Tshs");
                                destinationNearestStopMarker = mapboxMap.addMarker(options);
                                showProgressInfo(getString(R.string.calculating_shortest_route));
//                                hideProgressInfo();
                                Log.i(TAG, "Getting the route");
                                getRoute(origin, destination);
                            }
//                            setNearestStopMarker(markerLatLng, isSource);
                        } else {
                            Log.i(TAG, "No Matrix returned");
                        }
                    }
                }

                @Override
                public void onFailure(Call<MatrixResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private void setNearestStopMarker(@NonNull LatLng latLng, boolean isSource) {
        @SuppressLint("DefaultLocale") String uri =
                Uri.parse("https://overpass-api.de/api/interpreter")
                        .buildUpon()
                        .appendQueryParameter("data",
                                String.format(getString(R.string.overpass_api_query), 10,
                                        latLng.getLatitude(), latLng.getLongitude()))
                        .build().toString();
        Log.i(TAG, String.format("URI: %s", uri));
        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        uri, null,
                        response -> {
                            Gson gson = new Gson();
                            OverpassApiResponse apiResponse = gson.fromJson(response.toString(),
                                    OverpassApiResponse.class);
                            for (OverpassApiResponse.QueryElement e : apiResponse.elements) {
                                MarkerOptions options = new MarkerOptions()
                                        .position(new LatLng(e.lat, e.lon))
                                        .title(e.tags.name);
                                if (isSource) {
                                    sourceNearestStopMarker = mapboxMap.addMarker(options);
                                    animateCamera(latLng);
                                } else {
                                    //noinspection SpellCheckingInspection
                                    options.snippet("500Tshs");
                                    destinationNearestStopMarker = mapboxMap.addMarker(options);
                                    showProgressInfo(getString(R.string.calculating_shortest_route));
                                    getRoute(origin, destination);
                                }
                            }
                        },
                        error -> Log.i("Error%s", String.valueOf(error))
                );
        requestQueue.add(request);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.userlocation, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {

            }
        } else {
            Toast.makeText(this, R.string.userlocation_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public boolean onMapClick(@NonNull @NotNull LatLng point) {
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel);
        if (features.size() > 0) {
            Feature feature = features.get(0);
            if (feature.properties() != null) {
                for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
                    Timber.tag(TAG).i(String.format("%s = %s", entry.getKey(), entry.getValue()));
                }
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}