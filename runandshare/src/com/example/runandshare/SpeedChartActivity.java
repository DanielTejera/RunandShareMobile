package com.example.runandshare;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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

public class SpeedChartActivity extends Activity
		implements OnChartValueSelectedListener, OnClickListener, android.view.View.OnClickListener {

	// Class fields
	private LineChart lineChart;
	private Object lock;
	private ArrayList<Entry> entries;
	private ArrayList<Entry> entriesFollowed;
	private ArrayList<String> labels;
	private ImageButton menuButton;
	private User user;
	private ImageButton speedChart;
	private ImageButton profileChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_speed_chart);

		user = new User();
		lock = new Object();
		lineChart = (LineChart) findViewById(R.id.chart);
		entries = new ArrayList<Entry>();
		entriesFollowed = new ArrayList<Entry>();
		labels = new ArrayList<String>();

		new LoadUser().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		new LoadUserDatas().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		menuButton = (ImageButton) findViewById(R.id.menuButton);
		speedChart = (ImageButton) findViewById(R.id.speedChartButton);
		profileChart = (ImageButton) findViewById(R.id.profileChartButton);
		menuButton.setOnClickListener(this);
		speedChart.setClickable(false);
		profileChart.setOnClickListener(this);

		printProgress();

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menuButton:
			Intent intent = new Intent(SpeedChartActivity.this, NavigationActivity.class);
			startActivity(intent);
			break;
		case R.id.profileChartButton:
			Intent profileChartIntent = new Intent().setClass(SpeedChartActivity.this, AltitudeChartActivity.class);
			startActivity(profileChartIntent);
			break;
		default:
			break;
		}

	}

	private void printProgress() {
		new GetSpeedValues().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		LineDataSet dataset = new LineDataSet(entries, "Velocidad");
		dataset.setColor(Color.WHITE);
		dataset.setLineWidth(2f);
		LineData data = new LineData(labels, dataset);
		if (!user.getFollowedTraining().equals("null")) {

			new GetFollowedSpeedValues().execute();
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			LineDataSet dataset2 = new LineDataSet(entriesFollowed, "Velocidad Original");
			dataset2.setColor(Color.GREEN);
			dataset2.setLineWidth(2f);
			data.addDataSet(dataset2);
			lineChart.setData(data);
		} else {
			lineChart.setData(data);
		}

		lineChart.setDescription("Velocidad media");
	}

	// Clase asíncrona utilizada para llamar al servicio web getSpeedValues
	private class GetSpeedValues extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			getSpeedValues();
			synchronized (lock) {
				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web getSpeedValues donde cargamos los
	// datos de velocidad del entrenamiento
	private void getSpeedValues() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/getSpeedValues.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("table", user.getActualTraining()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("<br>");
			if (!(datas.length == 0)) {
				for (int i = 1; i < datas.length; i++) {
					String[] sections = datas[i].split("/");
					labels.add(sections[0]);
					float value = Float.parseFloat(sections[1]);
					entries.add(new Entry(value, i - 1));
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

	// Clase asíncrona utilizada para llamar al servicio web
	// getFollowedSpeedValues
	private class GetFollowedSpeedValues extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			getFollowedSpeedValues();
			synchronized (lock) {
				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web getFollowedSpeedValues donde
	// recibimos los datos de la velocidad del entrenamiento que el usuario está
	// siguiendo
	private void getFollowedSpeedValues() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/getSpeedValues.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("table", user.getFollowedTraining()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("<br>");
			if (!(datas.length == 0)) {
				for (int i = 1; i < datas.length; i++) {
					String[] sections = datas[i].split("/");
					labels.add(sections[0]);
					float value = Float.parseFloat(sections[1]);
					entries.add(new Entry(value, i - 1));
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

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub

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

	// Método donde se ejecuta el servicio web loadUser, donde cargamos el
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

	// Clase asíncrona utilizada para llamar al servicio web loadUserDatas
	class LoadUserDatas extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			loadUserDatas();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}

	}

	// Método donde se ejecuta el servicio web loadUserDatas donde se cargan los
	// datos del usuario
	private void loadUserDatas() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/loadUserDatas.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("/");
			if (!(datas.length == 0)) {
				for (int i = datas.length - 1; i >= datas.length - 2; i--) {
					user.setFollowedTraining(datas[datas.length - 1].trim());
					user.setActualTraining(datas[datas.length - 2].trim());

					Log.i("entrenamiento que sigo", user.getFollowedTraining());
					Log.i("entrenamiento que estoy haciendo", user.getActualTraining());
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

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub

	}

}
