package com.example.runandshare;

import android.support.v7.app.AppCompatCallback;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;

import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

public class ViewTrainingActivity extends android.support.v4.app.FragmentActivity
		implements OnClickListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, AppCompatCallback {

	private LocationManager locManager;
	private LocationListener locListener;

	private SupportMapFragment mapFragment;
	private Double mLat, mLng, mAlt;
	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private Location locationStart;

	private TextView trainingName;
	private TextView averageSpeed;
	private TextView totalTime;
	private TextView totalDistance;

	private Button viewStats;
	private Button share;
	public static User user;
	public static Training training;
	private ImageButton menuButton;

	private Object lock;

	private String[] trainingDatas;
	private List<LatLng> points;
	private Marker startTraining;
	private Marker finishTraining;
	private ShareDialog shareDialog;

	private final static int INSERT_FREQUENCY = 15000;

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
		setContentView(R.layout.activity_view_training);

		FacebookSdk.sdkInitialize(getApplicationContext());

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

		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		// Create a map
		mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.viewTrainingMap));
		mMap = mapFragment.getMap();

		// Enable location
		mMap.setMyLocationEnabled(true);
		// Hide the zoom controls
		mMap.getUiSettings().setZoomControlsEnabled(false);

		trainingName = (TextView) findViewById(R.id.trainingNameTextView);
		averageSpeed = (TextView) findViewById(R.id.averageSpeedTextView);
		totalDistance = (TextView) findViewById(R.id.totalDistanceTextView);
		totalTime = (TextView) findViewById(R.id.totalTimeTextView);

		viewStats = (Button) findViewById(R.id.statsButton);
		share = (Button) findViewById(R.id.share);
		menuButton = (ImageButton) findViewById(R.id.menuButton);

		viewStats.setOnClickListener(this);
		share.setOnClickListener(this);
		menuButton.setOnClickListener(this);

		training = new Training();
		lock = new Object();
		trainingDatas = new String[3];

		loadTraining();

		new LoadTrainingData().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		trainingName.setText(training.getTrainingName());
		totalDistance.setText("Distancia: \n" + trainingDatas[0] + " km");
		totalTime.setText("Tiempo: \n" + trainingDatas[1]);
		averageSpeed.setText("V. Media: \n" + trainingDatas[2] + " km/h");
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

		new SetActualTraining().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
		if (UserTrainingsActivity.training1.getLoad()) {
			training.setTrainingName(UserTrainingsActivity.training1.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training2.getLoad()) {
			training.setTrainingName(UserTrainingsActivity.training2.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training3.getLoad()) {
			training.setTrainingName(UserTrainingsActivity.training3.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training4.getLoad()) {
			training.setTrainingName(UserTrainingsActivity.training4.getTrainingName());
			return;
		}
		if (UserTrainingsActivity.training5.getLoad()) {
			training.setTrainingName(UserTrainingsActivity.training5.getTrainingName());
			return;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menuButton:
			Intent menuIntent = new Intent().setClass(ViewTrainingActivity.this, NavigationActivity.class);
			startActivity(menuIntent);
			break;
		case R.id.share:
			if (ShareDialog.canShow(ShareLinkContent.class)) {

				ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentTitle("Hello Facebook")
						.setContentDescription("The 'Hello Facebook' sample  showcases simple Facebook integration")
						.setContentUrl(Uri.parse("http://developers.facebook.com/android"))
						.setImageUrl(Uri
								.parse("https://fbcdn-dragon-a.akamaihd.net/hphotos-ak-xfa1/t39.2178-6/11057086_1577191859234204_214246289_n.png"))
						.build();

				shareDialog.show(linkContent);
			}
			break;
		case R.id.statsButton:
			Intent statsIntent = new Intent().setClass(ViewTrainingActivity.this, SpeedChartActivity.class);
			startActivity(statsIntent);
			break;
		default:
			break;
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

	// Método donde se ejecuta el servicio web loadTrainingData, donde se cargan
	// los datos reltivos al entrenamiento
	private void loadTrainingData() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/loadTrainingData.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", training.getTrainingName()));
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
			pairs.add(new BasicNameValuePair("trainingname", training.getTrainingName()));
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
			pairs.add(new BasicNameValuePair("actualtraining", training.getTrainingName()));
			pairs.add(new BasicNameValuePair("username", NavigationActivity.user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (ClientProtocolException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
		} catch (IOException e) {
			Log.i("HTTPCLIENT", e.getLocalizedMessage());
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

}
