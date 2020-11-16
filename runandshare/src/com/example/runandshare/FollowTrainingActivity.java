package com.example.runandshare;

import android.support.v7.app.AppCompatCallback;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;

public class FollowTrainingActivity extends android.support.v4.app.FragmentActivity
		implements OnClickListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, AppCompatCallback {

	private Chronometer chronometer;
	private ImageButton startPauseChronometerButton;
	private ImageButton stopChronometerButton;
	private Long elapsedTime = (long) 0;
	private String estado = "inactivo";
	private TextView distanceTextView;
	private TextView velocityTextView;

	private Handler handler;
	private Runnable runnable;

	private LocationManager locManager;
	private LocationListener locListener;

	private SupportMapFragment mapFragment;
	private Double mLat, mLng, mAlt;
	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private Location locationStart;
	private Location locationFinish;

	private Date dateStart;
	private Date dateFinish;
	private float distance;
	private long diffInMs;
	private long diffInSec;
	private Button chooseTrainingNameButton;
	private EditText chooseTrainingNameEditText;
	private String chooseTrainingName;
	private float velocity;

	public static User user;
	public static Training trainingToLoad;
	public static Training trainingInCurse;

	private Object lock;
	private String trainingName;
	private List<LatLng> points;
	private String[] trainingDatas;
	private Marker startTraining;
	private Marker finishTraining;

	private Button follow;
	private TextView averageSpeed1;
	private TextView totalTime1;
	private TextView totalDistance1;
	private TextView trainingNameFollowed;

	private TextView averageSpeed2;
	private TextView totalTime2;
	private TextView totalDistance2;
	private TextView trainingNameFinished;
	private TextView totalDistanceFinished;
	private TextView totalTimeFinished;
	private TextView averageSpeedFinished;
	private Button viewStats;
	private Button share;
	private int toLoad;
	private ShareDialog shareDialog;
	private ImageButton menuButton;

	private final static int INSERT_FREQUENCY = 5000;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_view_followed_training);

		FacebookSdk.sdkInitialize(getApplicationContext());
		menuButton = (ImageButton) findViewById(R.id.menuButton);
		menuButton.setOnClickListener(this);
		CallbackManager callBackManager = CallbackManager.Factory.create();
		shareDialog = new ShareDialog(this);
		shareDialog.registerCallback(callBackManager, new FacebookCallback<Sharer.Result>() {

			@Override
			public void onError(FacebookException error) {
				// TODO Auto-generated method stub

			}

			public void onSuccess(com.facebook.share.Sharer.Result result) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub

			}

		});

		toLoad = 0;
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		// Create a map
		mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.viewFollowedTrainingMap));
		mMap = mapFragment.getMap();

		// Enable location
		mMap.setMyLocationEnabled(true);
		// Hide the zoom controls
		mMap.getUiSettings().setZoomControlsEnabled(false);

		trainingToLoad = new Training();
		trainingInCurse = new Training();
		lock = new Object();
		user = new User();
		trainingDatas = new String[3];

		loadTraining();

		follow = (Button) findViewById(R.id.followButton);
		averageSpeed1 = (TextView) findViewById(R.id.averageSpeedFollowed);
		totalDistance1 = (TextView) findViewById(R.id.totalDistanceFollowed);
		totalTime1 = (TextView) findViewById(R.id.totalTimeFollowed);
		trainingNameFollowed = (TextView) findViewById(R.id.trainingNameFollowed);

		follow.setOnClickListener(this);
		new LoadTrainingData().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		trainingNameFollowed.setText(trainingToLoad.getTrainingName());
		totalDistance1.setText("Distancia: \n" + trainingDatas[0] + " km");
		totalTime1.setText("Tiempo: \n" + trainingDatas[1]);
		averageSpeed1.setText("V. Media: \n" + trainingDatas[2] + " km/h");

		new LoadUser().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		startLocalization();

		points = new ArrayList<LatLng>();
		new LoadPoints().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		drawTraining();

		MarkerOptions markerStart = new MarkerOptions().position(points.get(0))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_training)).anchor(0.5f, 0.5f);
		startTraining = mMap.addMarker(markerStart);

		MarkerOptions markerFinish = new MarkerOptions().position(points.get(points.size() - 1))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag)).anchor(0.5f, 0.5f);
		finishTraining = mMap.addMarker(markerFinish);
	}

	private void drawTraining() {

		for (int i = 0; i < points.size() - 1; i++) {
			LatLng src = points.get(i);
			LatLng dest = points.get(i + 1);

			Polyline line = mMap
					.addPolyline(
							new PolylineOptions()
									.add(new LatLng(src.latitude, src.longitude),
											new LatLng(dest.latitude, dest.longitude))
									.width(10).color(Color.RED).geodesic(true));
		}
	}

	private void loadTraining() {
		if (SearchTrainingActivity.training_1.getLoad()) {
			trainingToLoad.setTrainingName(SearchTrainingActivity.training_1.getTrainingName());
			return;
		}
		if (SearchTrainingActivity.training_2.getLoad()) {
			trainingToLoad.setTrainingName(SearchTrainingActivity.training_2.getTrainingName());
			return;
		}
		if (SearchTrainingActivity.training_3.getLoad()) {
			trainingToLoad.setTrainingName(SearchTrainingActivity.training_3.getTrainingName());
			return;
		}
		if (SearchTrainingActivity.training_4.getLoad()) {
			trainingToLoad.setTrainingName(SearchTrainingActivity.training_4.getTrainingName());
			return;
		}
		if (SearchTrainingActivity.training_5.getLoad()) {
			trainingToLoad.setTrainingName(SearchTrainingActivity.training_5.getTrainingName());
			return;
		}
		if (SearchTrainingActivity.searchTraining.getLoad()) {
			trainingToLoad.setTrainingName(SearchTrainingActivity.searchTraining.getTrainingName());
			return;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menuButton:
			Intent menuIntent = new Intent().setClass(FollowTrainingActivity.this, NavigationActivity.class);
			startActivity(menuIntent);
			break;
		case R.id.followButton:
			setContentView(R.layout.activity_start_training);
			menuButton = (ImageButton) findViewById(R.id.menuButton);
			menuButton.setOnClickListener(this);

			chooseTrainingNameButton = (Button) findViewById(R.id.chooseTrainingNameButton);
			chooseTrainingNameEditText = (EditText) findViewById(R.id.chooseTrainingNameEditText);

			chooseTrainingNameButton.setOnClickListener(this);

			mLocationClient = new LocationClient(this, this, this);
			mLocationClient.connect();
			// Create a map
			mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1));
			mMap = mapFragment.getMap();

			// Enable location
			mMap.setMyLocationEnabled(true);
			// Hide the zoom controls
			mMap.getUiSettings().setZoomControlsEnabled(false);
			drawTraining();
			MarkerOptions markerStart = new MarkerOptions().position(points.get(0))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_training)).anchor(0.5f, 0.5f);
			startTraining = mMap.addMarker(markerStart);

			MarkerOptions markerFinish = new MarkerOptions().position(points.get(points.size() - 1))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag)).anchor(0.5f, 0.5f);
			finishTraining = mMap.addMarker(markerFinish);
			break;
		case R.id.chooseTrainingNameButton:
			chooseTrainingName = chooseTrainingNameEditText.getText().toString();
			if (chooseTrainingName.equals("")) {
				chooseTrainingNameEditText.setText("Indique un nombre");
				break;
			}

			if (chooseTrainingName.indexOf(' ') != -1) {
				chooseTrainingNameEditText.setText("Espacios no permitidos");
				break;
			}
			new CheckTrainingName().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (!trainingToLoad.getCount().equals("0")) {
				chooseTrainingNameEditText.setText("En uso, escoja otro");
				break;
			}

			trainingName = chooseTrainingNameEditText.getText().toString();

			new AddTraining().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			new AddTrainingTable().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			trainingInCurse.setTrainingName(chooseTrainingName);

			new SetActualTraining().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			new SetFollowedTraining().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			setContentView(R.layout.activity_follow_training);
			menuButton = (ImageButton) findViewById(R.id.menuButton);
			menuButton.setOnClickListener(this);

			startPauseChronometerButton = (ImageButton) findViewById(R.id.startPauseChronometerButton);
			stopChronometerButton = (ImageButton) findViewById(R.id.stopChronometerButton);
			distanceTextView = (TextView) findViewById(R.id.distanceTextView);
			velocityTextView = (TextView) findViewById(R.id.velocityTextView);

			velocityTextView.setText("0 km/h");
			distanceTextView.setText("0 km");

			averageSpeed2 = (TextView) findViewById(R.id.averageSpeedFollow);
			totalDistance2 = (TextView) findViewById(R.id.totalDistanceFollow);
			totalTime2 = (TextView) findViewById(R.id.totalTimeFollow);

			totalDistance2.setText("Distancia: \n" + trainingDatas[0] + " km");
			totalTime2.setText("Tiempo: " + trainingDatas[1]);
			averageSpeed2.setText("V. Media: \n" + trainingDatas[2] + " km/h");

			startPauseChronometerButton.setOnClickListener(this);
			stopChronometerButton.setOnClickListener(this);

			startPauseChronometerButton.setImageResource(R.drawable.play);

			handler = new Handler();
			runnable = new Runnable() {
				@Override
				public void run() {

					locationFinish = mLocationClient.getLastLocation();

					if (locationStart.getLatitude() == locationFinish.getLatitude()
							&& (locationStart.getLongitude() == locationFinish.getLongitude())) {
						distance += 0;
					} else {
						distance += (float) ((locationStart.distanceTo(locationFinish)) / 1000);
					}

					new InsertPosition().execute();
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					dateFinish = new Date();
					diffInMs = dateFinish.getTime() - dateStart.getTime();
					diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
					velocity = (float) (distance / diffInSec);
					velocity = (float) (Math.rint(velocity * 100) / 100);
					distance = (float) (Math.rint(distance * 100) / 100);
					velocityTextView.setText(String.valueOf(velocity) + " km/h");
					distanceTextView.setText(String.valueOf(distance) + " km");

					locationStart = locationFinish;

					handler.postDelayed(this, INSERT_FREQUENCY);
				}
			};

			chronometer = (Chronometer) findViewById(R.id.chronometer);

			mLocationClient = new LocationClient(this, this, this);
			// Create a map
			mLocationClient.connect();
			mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

			mMap = mapFragment.getMap();

			// Enable location
			mMap.setMyLocationEnabled(true);
			// Hide the zoom controls
			mMap.getUiSettings().setZoomControlsEnabled(false);

			drawTraining();

			MarkerOptions markerS = new MarkerOptions().position(points.get(0))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_training)).anchor(0.5f, 0.5f);
			startTraining = mMap.addMarker(markerS);

			MarkerOptions markerF = new MarkerOptions().position(points.get(points.size() - 1))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag)).anchor(0.5f, 0.5f);
			finishTraining = mMap.addMarker(markerF);

			break;
		case R.id.startPauseChronometerButton:
			if (estado == "inactivo") {
				new AddTraining().execute();
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				new AddTrainingTable().execute();
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				chronometer.setBase(SystemClock.elapsedRealtime());
				chronometer.start();
				estado = "activo";
				startPauseChronometerButton.setImageResource(R.drawable.pause);
				startLocalization();
				dateStart = new Date();
				dateFinish = new Date();
				diffInMs = dateFinish.getTime() - dateStart.getTime();
				diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
				locationFinish = mLocationClient.getLastLocation();

				locationStart = locationFinish;
				new InsertPosition().execute();
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				handler.postDelayed(runnable, INSERT_FREQUENCY);
				break;
			}
			if (estado == "activo") {
				elapsedTime = SystemClock.elapsedRealtime();
				chronometer.stop();
				estado = "pausado";
				startPauseChronometerButton.setImageResource(R.drawable.play);
				break;
			} else {
				chronometer.setBase(chronometer.getBase() + SystemClock.elapsedRealtime() - elapsedTime);
				chronometer.start();
				estado = "activo";
				startPauseChronometerButton.setImageResource(R.drawable.pause);
			}
			break;

		case R.id.stopChronometerButton:
			chronometer.stop();
			chronometer.setBase(SystemClock.elapsedRealtime());
			startPauseChronometerButton.setImageResource(R.drawable.play);
			estado = "inactivo";
			elapsedTime = SystemClock.elapsedRealtime();
			handler.removeCallbacks(runnable);

			new InsertPosition().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			setContentView(R.layout.activity_finish_training);
			menuButton = (ImageButton) findViewById(R.id.menuButton);
			menuButton.setOnClickListener(this);

			toLoad = 1;
			mLocationClient = new LocationClient(this, this, this);
			mLocationClient.connect();
			// Create a map
			mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.finishedTrainingMap));
			mMap = mapFragment.getMap();

			// Enable location
			mMap.setMyLocationEnabled(true);
			// Hide the zoom controls
			mMap.getUiSettings().setZoomControlsEnabled(false);

			trainingNameFinished = (TextView) findViewById(R.id.trainingNameFinished);
			averageSpeedFinished = (TextView) findViewById(R.id.averageSpeedFinished);
			totalDistanceFinished = (TextView) findViewById(R.id.totalDistanceFinished);
			totalTimeFinished = (TextView) findViewById(R.id.totalTimeFinished);

			viewStats = (Button) findViewById(R.id.statsButton);
			share = (Button) findViewById(R.id.share);

			viewStats.setOnClickListener(this);
			share.setOnClickListener(this);

			trainingDatas = new String[3];

			new LoadTrainingData().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			trainingNameFinished.setText(chooseTrainingName);
			totalDistanceFinished.setText("Distancia: \n" + trainingDatas[0] + " km");
			totalTimeFinished.setText("Tiempo: \n" + trainingDatas[1]);
			averageSpeedFinished.setText("V. Media: \n" + trainingDatas[2] + " km/h");

			points = new ArrayList<LatLng>();
			new LoadPoints().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			drawTraining();
			MarkerOptions marker1 = new MarkerOptions().position(points.get(0))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_training)).anchor(0.5f, 0.5f);
			startTraining = mMap.addMarker(marker1);

			MarkerOptions marker2 = new MarkerOptions().position(points.get(points.size() - 1))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_flag)).anchor(0.5f, 0.5f);
			finishTraining = mMap.addMarker(marker2);
			break;

		case R.id.share:
			if (ShareDialog.canShow(ShareLinkContent.class)) {

				ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentTitle("Run&Share")
						.setContentDescription("He realizado el entrenamiento " + trainingInCurse.getTrainingName()
								+ " de " + trainingDatas[0] + " km en " + trainingDatas[1] + " con un ritmo de "
								+ trainingDatas[2] + " km/h")
						.setContentUrl(Uri.parse("http://IP_SERVIDOR/images/logo.png"))
						.setImageUrl(Uri
								.parse("http://www.noticiassin.com/wp-content/uploads/2015/11/hollywood-district-in-portland-Google-Maps.jpg"))
						.build();
				shareDialog.show(linkContent);
			}
			break;
		case R.id.statsButton:

			Intent statsIntent = new Intent().setClass(FollowTrainingActivity.this, SpeedChartActivity.class);
			startActivity(statsIntent);
			break;
		default:
			break;
		}

	}

	// Clase asíncrona utilizada para llamar al servicio web loadUser
	class LoadUser extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			loadUser();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web loadUser, donde se carga el
	// nombre del usuario
	private void loadUser() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/loadUser.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("idandroid",
					Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID)));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 1; i--) {
					user.setUserName(datas[datas.length - 1].trim());
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web loadPoints
	class LoadPoints extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			loadPoints();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web loadPoints, donde se cargan los
	// puntos del entrenamiento
	private void loadPoints() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/loadPoints.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", trainingToLoad.getTrainingName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("<br>");
			if (!(datas.length == 0)) {
				for (int i = 1; i < datas.length; i++) {
					String[] sections = datas[i].split("/");
					LatLng aux = new LatLng(Double.parseDouble(sections[0]), Double.parseDouble(sections[1]));
					points.add(aux);

				}
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startLocalization() {
		// Get a reference of LocationManager
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Get the last position
		Location loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		mostrarPosicion(loc);
		locListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				mostrarPosicion(location);
			}

			public void onProviderDisabled(String provider) {
				// lblEstado.setText("Provider OFF");
			}

			public void onProviderEnabled(String provider) {
				// lblEstado.setText("Provider ON ");
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {

				// Log.i("", "Provider Status: " + status);
				// lblEstado.setText("Provider Status: " + status);
			}
		};
		// Update the position every INSERT_FREQUENCY seconds
		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INSERT_FREQUENCY, 0, locListener);
	}

	private void mostrarPosicion(Location loc) {
		if (loc != null) {

			Log.i("", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
		}
	}

	public void onConnected(Bundle dataBundle) {
		// Get the latitude and the longitude
		locationStart = mLocationClient.getLastLocation();
		while (locationStart == null) {
			locationStart = mLocationClient.getLastLocation();
		}

		mLat = locationStart.getLatitude();
		mLng = locationStart.getLongitude();
		mAlt = locationStart.getAltitude();

		// Construct a LatLng object
		LatLng latLng = new LatLng(mLat, mLng);
		// Camera focuses on our position
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		mMap.animateCamera(cameraUpdate);
	}

	private boolean isGooglePlayServicesAvailable() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);
			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				// errorFragment.show(getSupportFragmentManager(), "Location
				// Updates");
			}
			return false;
		}
	}

	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getApplicationContext(), "Sorry. Location services not available to you", Toast.LENGTH_LONG)
					.show();
		}
	}

	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		if (isGooglePlayServicesAvailable()) {
			// mLocationClient.connect();
		}
	}

	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onSupportActionModeFinished(ActionMode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSupportActionModeStarted(ActionMode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public ActionMode onWindowStartingSupportActionMode(Callback arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	// Clase asíncrona utilizada para llamar al servicio web addTrainingTable
	class AddTrainingTable extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			addTrainingTable();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web addTrainingTable, donde se añade
	// una tabla a la base de datos con el nombre del entrenamiento
	private void addTrainingTable() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/addTrainingTable.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", trainingName));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

	@Override
	public void onBackPressed() {
	}

	// Clase asíncrona utilizada para llamar al servicio web addTraining
	class AddTraining extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			addTraining();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web addTraining, donde se añade a la
	// tabla de entrenamientos el nuevo entrenamiento
	private void addTraining() {
		try {
			Date dNow = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
			String date = ft.format(dNow);
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/addTraining.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(4);
			pairs.add(new BasicNameValuePair("trainingname", trainingName));
			pairs.add(new BasicNameValuePair("username", user.getUserName()));
			pairs.add(new BasicNameValuePair("date", date));
			pairs.add(new BasicNameValuePair("visits", "0"));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web insertPosition
	private class InsertPosition extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			insertPosition();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web insertPosition, donde se añade a
	// la tabla del entrenamiento la posición
	private void insertPosition() {

		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/insertPosition.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(7);
			pairs.add(new BasicNameValuePair("trainingname", trainingName));
			pairs.add(new BasicNameValuePair("lat", String.valueOf(mLat)));
			pairs.add(new BasicNameValuePair("lng", String.valueOf(mLng)));
			pairs.add(new BasicNameValuePair("alt", String.valueOf(mAlt)));
			pairs.add(new BasicNameValuePair("distance", String.valueOf(distance)));
			pairs.add(new BasicNameValuePair("time", String.valueOf(diffInSec)));
			pairs.add(new BasicNameValuePair("velocity", String.valueOf(velocity)));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web checkTrainingName
	class CheckTrainingName extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			checkTrainingName();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web checkTrainingName, donde se
	// comprueba el nombre del entrenamiento
	private void checkTrainingName() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/checkUserName.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", chooseTrainingName));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 1; i--) {
					trainingToLoad.setCount(datas[datas.length - 1].trim());
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Clase asíncrona utilizada para llamar al servicio web loadTrainingData
	class LoadTrainingData extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			loadTrainingData();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web loadTrainingData
	private void loadTrainingData() {
		String request = "";
		String trainingname = trainingToLoad.getTrainingName();
		if (toLoad == 1) {
			trainingname = chooseTrainingName;
		}
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/loadTrainingData.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", trainingname));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			int j = 2;
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 3; i--) {
					trainingDatas[j] = datas[i];
					j--;

				}
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web setActualTraining
	class SetActualTraining extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			setActualTraining();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web setActualTraining
	private void setActualTraining() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/setActualTraining.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
			pairs.add(new BasicNameValuePair("actualtraining", trainingInCurse.getTrainingName()));
			pairs.add(new BasicNameValuePair("username", user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web setFollowedTraining
	class SetFollowedTraining extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			setFollowedTraining();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web setFollowedTraining, que asigna
	// al usuario el entrenamiento que está siguiendo
	private void setFollowedTraining() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/setFollowedTraining.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
			pairs.add(new BasicNameValuePair("followedtraining", trainingToLoad.getTrainingName()));
			pairs.add(new BasicNameValuePair("username", user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		}
	}
}
