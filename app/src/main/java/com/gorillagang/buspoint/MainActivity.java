package com.gorillagang.buspoint;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gorillagang.buspoint.data.Journey;
import com.gorillagang.buspoint.data.OverpassApiResponse;
import com.gorillagang.buspoint.data.Route;
import com.gorillagang.buspoint.data.Stop;
import com.gorillagang.buspoint.ui.account.LoginActivity;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.DirectionsWaypoint;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
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
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.graphics.Color.parseColor;
import static com.mapbox.core.constants.Constants.PRECISION_6;
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
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
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
    private static final int PLACEPICKER_ACTIVITY_CODE = 4;
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
    private final ArrayList<Journey> journeyList = new ArrayList<>();
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
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String fromLoc;
    private String toLoc;

    //    private String serverIpAddress = "192.168.0.101:8000"; // Development Address
    private String serverIpAddress = "buspoint.pythonanywhere.com"; // Deployment Address;

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
                    style.addSource(
                            new GeoJsonSource(ROUTE_LINE_SOURCE_ID, new GeoJsonOptions().withLineMetrics(true)));
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
            /*case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;*/
            case R.id.action_show_journeys:
                Intent intent = new Intent(MainActivity.this, JourneyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("journeyList", journeyList);
                intent.putExtras(bundle);
                startActivity(intent);
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
            case R.id.action_feedback:
                Intent placePickerActivity = new PlacePicker.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(PlacePickerOptions.builder()
                                .statingCameraPosition(new CameraPosition.Builder()
                                        .target(new LatLng(getLastKnownLocation().getLatitude(),
                                                getLastKnownLocation().getLongitude()))
                                        .zoom(16).build())
                                .build())
                        .build(this);
                startActivityForResult(placePickerActivity, PLACEPICKER_ACTIVITY_CODE);
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

            case R.id.action_about:
                showInformationDialog(
                        getString(R.string.about_buspoint),
                        getString(R.string.about_us)
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Location getLastKnownLocation() {
        if (locationComponent != null) {
            return locationComponent.getLastKnownLocation();
        } else {
            String message = getString(R.string.enable_location_component_msg);
            String title = getString(R.string.enable_location_component_hdr);
            showInformationDialog(title, message);
        }
        return null;
    }

    private void showInformationDialog(String title, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
        dialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            dialog.cancel();
        });
        dialogBuilder.create().show();
    }

    private void showFeedbackDialog(LatLng latLng) {
        EditText feedbackComment = new EditText(getApplicationContext());
        int d = (int) getResources().getDimension(R.dimen.standard_margin);
        feedbackComment.setPadding(d, d, d, d);
        feedbackComment.setHint("Enter your comment here");
        feedbackComment.setText("Location: ["
                + latLng.getLatitude()
                + ","
                + latLng.getLongitude()
                + "]. Name: "
        );
        feedbackComment.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Feedback")
                .setView(feedbackComment)
                .setMessage("Describe the name of the stop you want to be added");
        dialogBuilder.setPositiveButton("Send", (dialog, which) -> {
            sendFeedback(feedbackComment.getText().toString());
        });
        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });
        dialogBuilder.create().show();
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

    private void sendFeedback(String feedback) {
        String uname = "anonymous";
        if (user != null) {
            uname = user.getUid();
        }

        String url = Uri.parse("http://" + serverIpAddress + "/api/feedback")
                .buildUpon()
                .build().toString();

        String finalUname = uname;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    String message = "Your feedback is being processed. \nThe stop will be verified" +
                            " and added if it exists. Thank you.";
                    showInformationDialog("Feedback Sent", message);
                },
                error -> {
                    String message = "Sorry, seems we are having difficulties sending your feedback";
                    showInformationDialog("Feedback Failed", message);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> postData = new HashMap<>();
                postData.put("feedback", feedback);
                postData.put("user", finalUname);
                return postData;
            }
        };
        requestQueue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (navigationMapRoute != null) {
                    navigationMapRoute.updateRouteVisibilityTo(false);
                    navigationMapRoute.removeRoute();
                }
                hideJorneyInfo();
                if (destinationNearestStopMarker != null) {
                    mapboxMap.removeMarker(destinationNearestStopMarker);
                    destinationNearestStopMarker = null;
                }
                if (sourceNearestStopMarker != null) {
                    mapboxMap.removeMarker(sourceNearestStopMarker);
                    sourceNearestStopMarker = null;
                }

                if (midStopMarker != null) {
                    mapboxMap.removeMarker(midStopMarker);
                    midStopMarker = null;
                }

                removeGradient();

                Place place = Autocomplete.getPlaceFromIntent(data);
                toLoc = place.getName();

                List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
                Task<FindCurrentPlaceResponse> placeResponse =
                        Places.createClient(getApplicationContext()).findCurrentPlace(request);
                placeResponse.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        fromLoc = response.getPlaceLikelihoods().get(0).getPlace().getName();
                    } else {
                        fromLoc = "None";
                    }
                });

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

        if (requestCode == PLACEPICKER_ACTIVITY_CODE) {
            if (resultCode == RESULT_OK) {
                CarmenFeature feature = PlacePicker.getPlace(data);
                if (feature != null) {
                    showFeedbackDialog(new LatLng(feature.center().latitude(),
                            feature.center().longitude()));
                }
            }
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
        ProgressBar progressInfoBar = findViewById(R.id.progress_info_bar);
        clearWaypointsBtn = findViewById(R.id.button_clear_waypoints);
        waypoints = new ArrayList<>();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        CoordinatorLayout journeySheet = findViewById(R.id.journey_sheet);
        journeyCardView = findViewById(R.id.card_journey_details);

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
        getMyLocationBtn.setOnClickListener(v -> {
            if (locationComponent == null) {
                String message = getString(R.string.enable_location_component_msg);
                String title = getString(R.string.enable_location_component_hdr);
                showInformationDialog(title, message);
                return;
            }
            if (locationComponent.isLocationComponentActivated() && getLastKnownLocation() != null) {
                animateCamera(new LatLng(
                        getLastKnownLocation().getLatitude(),
                        getLastKnownLocation().getLongitude()));
            } else {
                Toast.makeText(
                        this,
                        "Please enable the location services",
                        Toast.LENGTH_LONG).show();
                enableLocationComponent(mapboxMap.getStyle());
            }
        });

        clearWaypointsBtn.setOnClickListener(v -> {
            if (waypoints != null && waypoints.size() > 0) {
                waypoints.clear();
            }
            navigationMapRoute.updateRouteVisibilityTo(false);
            currentRoute = null;
            if (destinationNearestStopMarker != null)
                mapboxMap.removeMarker(destinationNearestStopMarker);
            if (sourceNearestStopMarker != null)
                mapboxMap.removeMarker(sourceNearestStopMarker);
            if (midStopMarker != null)
                mapboxMap.removeMarker(midStopMarker);
            startJourneyBtn.setVisibility(View.GONE);
            clearWaypointsBtn.setVisibility(View.GONE);
            removeGradient();
            hideJorneyInfo();
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

        hideBottomSheet();
    }

    private void hideBottomSheet() {
        journeySheetBehaviour = BottomSheetBehavior.from(journeyCardView);
        journeySheetBehaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull @NotNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull @NotNull View bottomSheet, float slideOffset) {
                showJourneySheetBtn.setRotation(slideOffset * 180);
            }
        });
        journeySheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void getRoute(@NonNull Point origin, @NonNull Point destination) {
        if (waypoints.size() < 0) return;

        String url = Uri.parse("http://" + serverIpAddress + "/api/get-route")
                .buildUpon()
                .appendQueryParameter(
                        "start_lat",
                        String.valueOf(sourceNearestStopMarker.getPosition().getLatitude()))
                .appendQueryParameter(
                        "start_lon",
                        String.valueOf(sourceNearestStopMarker.getPosition().getLongitude()))
                .appendQueryParameter(
                        "final_lat",
                        String.valueOf(destinationNearestStopMarker.getPosition().getLatitude()))
                .appendQueryParameter(
                        "final_lon",
                        String.valueOf(destinationNearestStopMarker.getPosition().getLongitude()))
                .appendQueryParameter("from", fromLoc)
                .appendQueryParameter("to", toLoc)
                .build().toString();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url, null,
                response -> {
                    showProgressInfo("Drawing the route");
                    Log.i(TAG, "Request: " + url);
                    Log.i(TAG, "Response: " + String.valueOf(response));
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                    Journey journey = gson.fromJson(response.toString(), Journey.class);
                    journeyList.add(journey);
                    showJourneySheetBtn.setVisibility(View.VISIBLE);
                    showJourneyInfo();
                    calculateRoute();
                    Log.i(TAG, "TEST Journey: " + journey.getDateTime().toString());
                    hideProgressInfo();
                },
                error -> {
                    Log.i(TAG, error.toString());
                    showInformationDialog(
                            getResources().getString(R.string.problems_calculating_route));
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, 20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        requestQueue.add(request);
    }

    private void removeGradient() {
        mapboxMap.getStyle(style -> {
            GeoJsonSource lineLayerSource = style.getSourceAs(ROUTE_LINE_SOURCE_ID);
            if (lineLayerSource != null) {
                Log.i(TAG, "LineSource not null");
                style.removeSource(ROUTE_LINE_SOURCE_ID);
                Layer lineLayer = style.getLayer(ROUTE_LAYER_ID);

                if (lineLayer != null) {
                    lineLayer.setProperties(visibility(NONE));
                }
            } else {
                Log.i(TAG, "LineSource is null");
            }
        });

    }

    private void startNavigationActivity() {
        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                .shouldSimulateRoute(simulateRoute)
                .directionsRoute(currentRoute)
                .waynameChipEnabled(true)
                .build();
        NavigationLauncher.startNavigation(MainActivity.this, options);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showJourneyInfo() {
        Journey journey = journeyList.get(journeyList.size() - 1);
        Route fromRoute = journey.getRoutes().get(0);

        ((TextView) journeyCardView.findViewById(R.id.journey_from_to))
                .setText(journey.getFromDescription() + " to " + journey.getToDescription());
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd/MM hh:mm");
        Date date = journey.getDateTime();
        ((TextView) journeyCardView.findViewById(R.id.journey_date_short))
                .setText(dateFormat.format(date));

        Spannable fromRouteDescription = new SpannableString(" " + fromRoute.getName() + " ");
        int strLen = fromRouteDescription.length();
        fromRouteDescription.setSpan(
                new BackgroundColorSpan(Color.parseColor(fromRoute.getFirstStripe())),
                0, (int) Math.floor(strLen / 2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        fromRouteDescription.setSpan(
                new BackgroundColorSpan(Color.parseColor(fromRoute.getLastStripe())),
                (int) Math.floor(strLen / 2), strLen, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        ((TextView) journeyCardView.findViewById(R.id.from_route_description))
                .setText(fromRouteDescription);
        ((TextView) journeyCardView.findViewById(R.id.from_stop_description))
                .setText(journey.getStartStop().getName());

        if (journey.getRoutes().size() > 1) {
            Route toRoute = journey.getRoutes().get(1);
            Spannable toRouteDescription = new SpannableString(" " + toRoute.getName() + " ");
            strLen = toRouteDescription.length();
            toRouteDescription.setSpan(
                    new BackgroundColorSpan(Color.parseColor(toRoute.getFirstStripe())),
                    0, (int) Math.floor(strLen / 2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            toRouteDescription.setSpan(
                    new BackgroundColorSpan(Color.parseColor(toRoute.getLastStripe())),
                    (int) Math.floor(strLen / 2), strLen, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            ((TextView) journeyCardView.findViewById(R.id.mid_price_description)).setText("500 Tshs.");
            ((TextView) journeyCardView.findViewById(R.id.mid_route_description))
                    .setText(toRouteDescription);
            ((TextView) journeyCardView.findViewById(R.id.to_price_description))
                    .setText("500 Tshs.");
            ((TextView) journeyCardView.findViewById(R.id.to_stop_description))
                    .setText(journey.getFinalStop().getName());
        } else {
            ((TextView) journeyCardView.findViewById(R.id.to_price_description))
                    .setText("500 Tshs.");
            ((TextView) journeyCardView.findViewById(R.id.to_stop_description))
                    .setText(journey.getFinalStop().getName());
        }

        ((TextView) journeyCardView.findViewById(R.id.journey_total_price))
                .setText(journey.getCost() + " TShs.");

        if (journey.getMidStop() != null) {
            Stop midStop = journey.getMidStop();
            Bitmap b = getBitmap(getApplicationContext(), R.drawable.ic_mapbox_marker_icon_yellow);
            MarkerOptions midStopMarkerOptions = new MarkerOptions()
                    .setIcon(IconFactory.getInstance(getApplicationContext()).fromBitmap(b))
                    .setTitle(midStop.getName())
                    .setPosition(new LatLng(midStop.getLat(), midStop.getLon()));
            midStopMarker = mapboxMap.addMarker(midStopMarkerOptions);

            journeyCardView.findViewById(R.id.mid_route_container)
                    .setVisibility(View.VISIBLE);
            journeyCardView.findViewById(R.id.midroute_arrow)
                    .setVisibility(View.VISIBLE);
            ((TextView) journeyCardView.findViewById(R.id.mid_stop_description))
                    .setText(midStop.getName());
            journeyCardView.findViewById(R.id.mid_route_container)
                    .setOnClickListener(v -> {
                        animateCamera(new LatLng(midStop.getLat(), midStop.getLon()));
                    });
        } else {
            journeyCardView.findViewById(R.id.mid_route_container)
                    .setVisibility(View.GONE);
            journeyCardView.findViewById(R.id.midroute_arrow)
                    .setVisibility(View.GONE);
        }

        journeyCardView.findViewById(R.id.from_route_container)
                .setOnClickListener(v -> {
                    animateCamera(new LatLng(journey.getStartStop().getLat(),
                            journey.getStartStop().getLon()));
                });
        journeyCardView.findViewById(R.id.to_route_container)
                .setOnClickListener(v -> {
                    animateCamera(new LatLng(journey.getFinalStop().getLat(),
                            journey.getFinalStop().getLon()));
                });

        showBottomSheet();
    }

    private void calculateRoute() {
        Journey journey = journeyList.get(journeyList.size() - 1);
        List<Stop> routingStops = journey.getRoutingStops();
        Stop startStop = journey.getStartStop();
        Stop finalStop = journey.getFinalStop();
        Stop midStop = journey.getMidStop();

        // Building the list of computed line geometry points
        List<Point> points = new ArrayList<>();
        for (Stop s : journey.getRoutingStops()) {
            Point p = Point.fromLngLat(s.getLon(), s.getLat());
            points.add(p);
        }

        showProgressInfo(getString(R.string.drawing_route));

        // Map Navigation Request
        NavigationRoute.Builder builder = NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .alternatives(false)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .continueStraight(true);

        for (int i = 0; i < routingStops.size(); i++) {
            builder.addWaypoint(points.get(i));
        }
        builder.origin(Point.fromLngLat(startStop.getLon(), startStop.getLat()))
                .destination(Point.fromLngLat(finalStop.getLon(), finalStop.getLat()));

        builder.build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    showInformationDialog("Routing", "We are having network problems in drawing your route");
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }

                currentRoute = response.body().routes().get(0);
                // Draw the route on the map
                if (navigationMapRoute != null) {
                    navigationMapRoute.removeRoute();
                } else {
                    navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                }
                navigationMapRoute.addRoute(currentRoute);

                removeGradient();

                mapboxMap.getStyle(style -> {
                    GeoJsonSource lineLayerSource = style.getSourceAs(ROUTE_LINE_SOURCE_ID);
                    if (lineLayerSource != null) {
                        Log.i(TAG, "LineSource not null");
                        LineString lineString =
                                LineString.fromPolyline(
                                        currentRoute.geometry(),
                                        PRECISION_6
                                );
                        lineLayerSource.setGeoJson(Feature.fromGeometry(lineString));
                        Layer lineLayer = style.getLayer(ROUTE_LAYER_ID);
                        if (lineLayer != null) {
                            lineLayer.setProperties(visibility(VISIBLE));
                        }
                    } else {
                        Log.i(TAG, "LineSource is null");
                    }
                });

                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder()
                        .include(new LatLng(startStop.getLat(), startStop.getLon()));
                if (midStop != null) {
                    boundsBuilder.include(new LatLng(midStop.getLat(), midStop.getLon()));
                }
                boundsBuilder.include(new LatLng(finalStop.getLat(), finalStop.getLon()));
                animateCameraToBounds(boundsBuilder.build());

                clearWaypointsBtn.setVisibility(View.VISIBLE);
                startJourneyBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });

        /*MapboxMapMatching matchingClient = MapboxMapMatching.builder()
                .accessToken(Mapbox.getAccessToken())
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .coordinates(points)
                .annotations(DirectionsCriteria.ANNOTATION_DURATION, DirectionsCriteria.ANNOTATION_DISTANCE)
                .roundaboutExits(true)
                .overview(DirectionsCriteria.OVERVIEW_FALSE)
                .steps(true)
                .build();
        matchingClient.enqueueCall(new Callback<MapMatchingResponse>() {
            @Override
            public void onResponse(Call<MapMatchingResponse> call, Response<MapMatchingResponse> response) {
                if (response.isSuccessful()){
                    if (response.body() == null) {
                        Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                        return;
                    } else if (response.body().matchings().size() < 1) {
                        Log.e(TAG, "No routes found");
                        return;
                    }

                    showProgressInfo("Line Matching for Drawing route successful");
                    Log.i(TAG, "Found Matching: " + response.message());

                    currentRoute = response.body().matchings().get(0).toDirectionRoute();
                    // Draw the route on the map
                    if (navigationMapRoute != null) {
                        navigationMapRoute.removeRoute();
                    } else {
                        navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                    }
                    navigationMapRoute.addRoute(currentRoute);
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder()
                            .include(new LatLng(startStop.getLat(), startStop.getLon()));
                    if (midStop != null) {
                        boundsBuilder.include(new LatLng(midStop.getLat(), midStop.getLon()));
                    }
                    boundsBuilder.include(new LatLng(finalStop.getLat(), finalStop.getLon()));
                    animateCameraToBounds(boundsBuilder.build());

                    clearWaypointsBtn.setVisibility(View.VISIBLE);
                    startJourneyBtn.setVisibility(View.VISIBLE);
                } else {
                    Log.i(TAG, "Failed to get matchings: " + response.message());
                }
                hideProgressInfo();
            }

            @Override
            public void onFailure(Call<MapMatchingResponse> call, Throwable t) {
                Log.i(TAG, t.getLocalizedMessage());
            }
        });*/
    }

    private void showBottomSheet() {
        journeySheetBehaviour = BottomSheetBehavior.from(journeyCardView);
        if (journeySheetBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            journeySheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            journeySheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void hideJorneyInfo() {
        journeyCardView.findViewById(R.id.mid_route_container)
                .setVisibility(View.GONE);
        journeyCardView.findViewById(R.id.midroute_arrow)
                .setVisibility(View.GONE);
        ((TextView) journeyCardView.findViewById(R.id.mid_route_description))
                .setText(R.string.board_off_here);
        hideBottomSheet();
        showJourneySheetBtn.setVisibility(View.GONE);
    }

    private void showInformationDialog(String string) {
    }

    private void showProgressInfo(String info) {
        progressInfo.setVisibility(View.VISIBLE);
        progressInfoText.setText(info);
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
                        error -> {
                            Log.d("Error%s", String.valueOf(error));
                            String message = "Seems we are having problems finding a stop near you. " +
                                    "\nMaybe try a different search term.";
                            showInformationDialog(getString(R.string.stop_not_found), message);
                            hideProgressInfo();
                        }
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
                        error -> {
                            String message = "Seems we are having network problems finding a stop near your destination. " +
                                    "\nPlease try searching again.";
                            showInformationDialog(getString(R.string.network_problems), message);
                            hideJorneyInfo();
                            Log.d("Error%s", String.valueOf(error));
                        }
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
                destinationMarkerLayer.setProperties(visibility(VISIBLE));
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(Context context, int drawableId) {
        Log.e(TAG, "getBitmap: 2");
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        Log.e(TAG, "getBitmap: 1");
        return bitmap;
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
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                            IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);

                            MarkerOptions options = new MarkerOptions()
                                    .position(new LatLng(e.lat, e.lon))
                                    .title(e.tags.name);
                            if (isSource) {
                                if (fromLoc.equals("None")) {
                                    fromLoc = options.getTitle();
                                }
                                Bitmap b = getBitmap(getApplicationContext(), R.drawable.ic_mapbox_marker_icon_green);
                                Log.d(TAG, b.toString());
                                Icon icon = iconFactory.fromBitmap(b);
                                options.setIcon(icon);
                                sourceNearestStopMarker = mapboxMap.addMarker(options);
                                animateCamera(new LatLng(e.lat, e.lon));
                            } else {
                                destinationNearestStopMarker = mapboxMap.addMarker(options);
                                showProgressInfo(getString(R.string.calculating_shortest_route));
//                                hideProgressInfo();
                                Log.i(TAG, "Getting the route");
                                getRoute(origin, destination);
                            }
//                            setNearestStopMarker(markerLatLng, isSource);
                        } else {
                            String message = "Oops! We seem to be having connectivity issues.\n" +
                                    "Please retry your search again";
                            showInformationDialog(getString(R.string.network_problem), message);
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
        Log.i(TAG, "Clicked: " + point);
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