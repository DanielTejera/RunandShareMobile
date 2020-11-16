package com.example.runandshare;

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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class UserTrainingsActivity extends Activity implements OnClickListener {

	private TextView name1;
	private TextView name2;
	private TextView name3;
	private TextView name4;
	private TextView name5;

	private TextView date1;
	private TextView date2;
	private TextView date3;
	private TextView date4;
	private TextView date5;

	private ImageButton view1;
	private ImageButton view2;
	private ImageButton view3;
	private ImageButton view4;
	private ImageButton view5;

	private ImageButton edit1;
	private ImageButton edit2;
	private ImageButton edit3;
	private ImageButton edit4;
	private ImageButton edit5;

	private ImageButton delete1;
	private ImageButton delete2;
	private ImageButton delete3;
	private ImageButton delete4;
	private ImageButton delete5;

	private ImageButton prev;
	private ImageButton next;

	private ImageButton menuButton;

	public static Training training1;
	public static Training training2;
	public static Training training3;
	public static Training training4;
	public static Training training5;

	private Object lock;

	private String trainingToDelete;

	private List<String> userTrainingsName;
	private List<String> userTrainingsDate;

	private int control;
	private TextView noTrainings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_user_trainings);

		lock = new Object();
		userTrainingsName = new ArrayList<String>();
		userTrainingsDate = new ArrayList<String>();

		new LoadTrainings().execute();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		name1 = (TextView) findViewById(R.id.name1);
		name2 = (TextView) findViewById(R.id.name2);
		name3 = (TextView) findViewById(R.id.name3);
		name4 = (TextView) findViewById(R.id.name4);
		name5 = (TextView) findViewById(R.id.name5);

		date1 = (TextView) findViewById(R.id.date1);
		date2 = (TextView) findViewById(R.id.date2);
		date3 = (TextView) findViewById(R.id.date3);
		date4 = (TextView) findViewById(R.id.date4);
		date5 = (TextView) findViewById(R.id.date5);

		noTrainings = (TextView) findViewById(R.id.noTrainings);

		view1 = (ImageButton) findViewById(R.id.view1);
		view2 = (ImageButton) findViewById(R.id.view2);
		view3 = (ImageButton) findViewById(R.id.view3);
		view4 = (ImageButton) findViewById(R.id.view4);
		view5 = (ImageButton) findViewById(R.id.view5);

		view1.setOnClickListener(this);
		view2.setOnClickListener(this);
		view3.setOnClickListener(this);
		view4.setOnClickListener(this);
		view5.setOnClickListener(this);

		edit1 = (ImageButton) findViewById(R.id.edit1);
		edit2 = (ImageButton) findViewById(R.id.edit2);
		edit3 = (ImageButton) findViewById(R.id.edit3);
		edit4 = (ImageButton) findViewById(R.id.edit4);
		edit5 = (ImageButton) findViewById(R.id.edit5);

		edit1.setOnClickListener(this);
		edit2.setOnClickListener(this);
		edit3.setOnClickListener(this);
		edit4.setOnClickListener(this);
		edit5.setOnClickListener(this);

		delete1 = (ImageButton) findViewById(R.id.delete1);
		delete2 = (ImageButton) findViewById(R.id.delete2);
		delete3 = (ImageButton) findViewById(R.id.delete3);
		delete4 = (ImageButton) findViewById(R.id.delete4);
		delete5 = (ImageButton) findViewById(R.id.delete5);

		delete1.setOnClickListener(this);
		delete2.setOnClickListener(this);
		delete3.setOnClickListener(this);
		delete4.setOnClickListener(this);
		delete5.setOnClickListener(this);

		prev = (ImageButton) findViewById(R.id.prevTrainingsImageButton);
		next = (ImageButton) findViewById(R.id.nextTrainingsImageButton);
		menuButton = (ImageButton) findViewById(R.id.menuButton);

		prev.setOnClickListener(this);
		next.setOnClickListener(this);
		menuButton.setOnClickListener(this);

		training1 = new Training();
		training2 = new Training();
		training3 = new Training();
		training4 = new Training();
		training5 = new Training();

		prev.setClickable(false);

		firstLoad();

	}

	private void firstLoad() {
		switch (userTrainingsName.size()) {
		case 0:
			noTrainings.setText("No ha registrado ningún entrenamiento");

			view1.setVisibility(View.INVISIBLE);
			view2.setVisibility(View.INVISIBLE);
			view3.setVisibility(View.INVISIBLE);
			view4.setVisibility(View.INVISIBLE);
			view5.setVisibility(View.INVISIBLE);

			edit1.setVisibility(View.INVISIBLE);
			edit2.setVisibility(View.INVISIBLE);
			edit3.setVisibility(View.INVISIBLE);
			edit4.setVisibility(View.INVISIBLE);
			edit5.setVisibility(View.INVISIBLE);

			delete1.setVisibility(View.INVISIBLE);
			delete2.setVisibility(View.INVISIBLE);
			delete3.setVisibility(View.INVISIBLE);
			delete4.setVisibility(View.INVISIBLE);
			delete5.setVisibility(View.INVISIBLE);

			next.setClickable(false);
			break;

		case 1:
			name1.setText(userTrainingsName.get(0));
			date1.setText(userTrainingsDate.get(0));

			view2.setVisibility(View.INVISIBLE);
			view3.setVisibility(View.INVISIBLE);
			view4.setVisibility(View.INVISIBLE);
			view5.setVisibility(View.INVISIBLE);

			edit2.setVisibility(View.INVISIBLE);
			edit3.setVisibility(View.INVISIBLE);
			edit4.setVisibility(View.INVISIBLE);
			edit5.setVisibility(View.INVISIBLE);

			delete2.setVisibility(View.INVISIBLE);
			delete3.setVisibility(View.INVISIBLE);
			delete4.setVisibility(View.INVISIBLE);
			delete5.setVisibility(View.INVISIBLE);

			next.setClickable(false);
			break;
		case 2:
			name1.setText(userTrainingsName.get(0));
			name2.setText(userTrainingsName.get(1));
			date1.setText(userTrainingsDate.get(0));
			date2.setText(userTrainingsDate.get(1));

			view3.setVisibility(View.INVISIBLE);
			view4.setVisibility(View.INVISIBLE);
			view5.setVisibility(View.INVISIBLE);

			edit3.setVisibility(View.INVISIBLE);
			edit4.setVisibility(View.INVISIBLE);
			edit5.setVisibility(View.INVISIBLE);

			delete3.setVisibility(View.INVISIBLE);
			delete4.setVisibility(View.INVISIBLE);
			delete5.setVisibility(View.INVISIBLE);

			next.setClickable(false);
			break;
		case 3:
			name1.setText(userTrainingsName.get(0));
			name2.setText(userTrainingsName.get(1));
			name3.setText(userTrainingsName.get(2));
			date1.setText(userTrainingsDate.get(0));
			date2.setText(userTrainingsDate.get(1));
			date3.setText(userTrainingsDate.get(2));

			view4.setVisibility(View.INVISIBLE);
			view5.setVisibility(View.INVISIBLE);

			edit4.setVisibility(View.INVISIBLE);
			edit5.setVisibility(View.INVISIBLE);

			delete4.setVisibility(View.INVISIBLE);
			delete5.setVisibility(View.INVISIBLE);

			next.setClickable(false);
			break;
		case 4:
			name1.setText(userTrainingsName.get(0));
			name2.setText(userTrainingsName.get(1));
			name3.setText(userTrainingsName.get(2));
			name4.setText(userTrainingsName.get(3));
			date1.setText(userTrainingsDate.get(0));
			date2.setText(userTrainingsDate.get(1));
			date3.setText(userTrainingsDate.get(2));
			date4.setText(userTrainingsDate.get(3));

			view5.setVisibility(View.INVISIBLE);

			edit5.setVisibility(View.INVISIBLE);

			delete5.setVisibility(View.INVISIBLE);

			next.setClickable(false);
			break;
		case 5:
			name1.setText(userTrainingsName.get(0));
			name2.setText(userTrainingsName.get(1));
			name3.setText(userTrainingsName.get(2));
			name4.setText(userTrainingsName.get(3));
			name5.setText(userTrainingsName.get(4));
			date1.setText(userTrainingsDate.get(0));
			date2.setText(userTrainingsDate.get(1));
			date3.setText(userTrainingsDate.get(2));
			date4.setText(userTrainingsDate.get(3));
			date5.setText(userTrainingsDate.get(4));

			next.setClickable(false);
			break;
		default:
			name1.setText(userTrainingsName.get(0));
			name2.setText(userTrainingsName.get(1));
			name3.setText(userTrainingsName.get(2));
			name4.setText(userTrainingsName.get(3));
			name5.setText(userTrainingsName.get(4));
			date1.setText(userTrainingsDate.get(0));
			date2.setText(userTrainingsDate.get(1));
			date3.setText(userTrainingsDate.get(2));
			date4.setText(userTrainingsDate.get(3));
			date5.setText(userTrainingsDate.get(4));

			control = 5;
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menuButton:
			Intent menuIntent = new Intent().setClass(UserTrainingsActivity.this, NavigationActivity.class);
			startActivity(menuIntent);
			break;
		case R.id.view1:
			training1.setTrainingName((String) name1.getText());
			training1.setLoad(true);
			Intent viewIntent1 = new Intent().setClass(UserTrainingsActivity.this, ViewTrainingActivity.class);
			startActivity(viewIntent1);
			break;
		case R.id.view2:
			training2.setTrainingName((String) name2.getText());
			training2.setLoad(true);
			Intent viewIntent2 = new Intent().setClass(UserTrainingsActivity.this, ViewTrainingActivity.class);
			startActivity(viewIntent2);
			break;
		case R.id.view3:
			training3.setTrainingName((String) name3.getText());
			training3.setLoad(true);
			Intent viewIntent3 = new Intent().setClass(UserTrainingsActivity.this, ViewTrainingActivity.class);
			startActivity(viewIntent3);
			break;
		case R.id.view4:
			training4.setTrainingName((String) name4.getText());
			training4.setLoad(true);
			Intent viewIntent4 = new Intent().setClass(UserTrainingsActivity.this, ViewTrainingActivity.class);
			startActivity(viewIntent4);
			break;
		case R.id.view5:
			training5.setTrainingName((String) name5.getText());
			training5.setLoad(true);
			Intent viewIntent5 = new Intent().setClass(UserTrainingsActivity.this, ViewTrainingActivity.class);
			startActivity(viewIntent5);
			break;
		case R.id.edit1:
			training1.setTrainingName((String) name1.getText());
			training1.setEdit(true);
			Intent editIntent1 = new Intent().setClass(UserTrainingsActivity.this, EditTrainingActivity.class);
			startActivity(editIntent1);
			break;
		case R.id.edit2:
			training2.setTrainingName((String) name2.getText());
			training2.setEdit(true);
			Intent editIntent2 = new Intent().setClass(UserTrainingsActivity.this, EditTrainingActivity.class);
			startActivity(editIntent2);
			break;
		case R.id.edit3:
			training3.setTrainingName((String) name3.getText());
			training3.setEdit(true);
			Intent editIntent3 = new Intent().setClass(UserTrainingsActivity.this, EditTrainingActivity.class);
			startActivity(editIntent3);
			break;
		case R.id.edit4:
			training4.setTrainingName((String) name4.getText());
			training4.setEdit(true);
			Intent editIntent4 = new Intent().setClass(UserTrainingsActivity.this, EditTrainingActivity.class);
			startActivity(editIntent4);
			break;
		case R.id.edit5:
			training5.setTrainingName((String) name5.getText());
			training5.setEdit(true);
			Intent editIntent5 = new Intent().setClass(UserTrainingsActivity.this, EditTrainingActivity.class);
			startActivity(editIntent5);
			break;
		case R.id.delete1:
			trainingToDelete = (String) name1.getText();
			alertDeleteTraining();
			break;
		case R.id.delete2:
			trainingToDelete = (String) name2.getText();
			alertDeleteTraining();
			break;
		case R.id.delete3:
			trainingToDelete = (String) name3.getText();
			alertDeleteTraining();
			break;
		case R.id.delete4:
			trainingToDelete = (String) name4.getText();
			alertDeleteTraining();
			break;
		case R.id.delete5:
			trainingToDelete = (String) name5.getText();
			alertDeleteTraining();
			break;

		case R.id.prevTrainingsImageButton:
			switch (control) {
			case 5:
				name1.setText(userTrainingsName.get(control - 5));
				name2.setText(userTrainingsName.get(control - 4));
				name3.setText(userTrainingsName.get(control - 3));
				name4.setText(userTrainingsName.get(control - 2));
				name5.setText(userTrainingsName.get(control - 1));

				date1.setText(userTrainingsDate.get(control - 5));
				date2.setText(userTrainingsDate.get(control - 4));
				date3.setText(userTrainingsDate.get(control - 3));
				date4.setText(userTrainingsDate.get(control - 2));
				date5.setText(userTrainingsDate.get(control - 1));

				view1.setVisibility(View.VISIBLE);
				view2.setVisibility(View.VISIBLE);
				view3.setVisibility(View.VISIBLE);
				view4.setVisibility(View.VISIBLE);
				view5.setVisibility(View.VISIBLE);

				edit1.setVisibility(View.VISIBLE);
				edit2.setVisibility(View.VISIBLE);
				edit3.setVisibility(View.VISIBLE);
				edit4.setVisibility(View.VISIBLE);
				edit5.setVisibility(View.VISIBLE);

				delete1.setVisibility(View.VISIBLE);
				delete2.setVisibility(View.VISIBLE);
				delete3.setVisibility(View.VISIBLE);
				delete4.setVisibility(View.VISIBLE);
				delete5.setVisibility(View.VISIBLE);

				prev.setClickable(false);
				next.setClickable(true);
				break;
			default:
				name1.setText(userTrainingsName.get(control - 5));
				name2.setText(userTrainingsName.get(control - 4));
				name3.setText(userTrainingsName.get(control - 3));
				name4.setText(userTrainingsName.get(control - 2));
				name5.setText(userTrainingsName.get(control - 1));

				date1.setText(userTrainingsDate.get(control - 5));
				date2.setText(userTrainingsDate.get(control - 4));
				date3.setText(userTrainingsDate.get(control - 3));
				date4.setText(userTrainingsDate.get(control - 2));
				date5.setText(userTrainingsDate.get(control - 1));

				view1.setVisibility(View.VISIBLE);
				view2.setVisibility(View.VISIBLE);
				view3.setVisibility(View.VISIBLE);
				view4.setVisibility(View.VISIBLE);
				view5.setVisibility(View.VISIBLE);

				edit1.setVisibility(View.VISIBLE);
				edit2.setVisibility(View.VISIBLE);
				edit3.setVisibility(View.VISIBLE);
				edit4.setVisibility(View.VISIBLE);
				edit5.setVisibility(View.VISIBLE);

				delete1.setVisibility(View.VISIBLE);
				delete2.setVisibility(View.VISIBLE);
				delete3.setVisibility(View.VISIBLE);
				delete4.setVisibility(View.VISIBLE);
				delete5.setVisibility(View.VISIBLE);

				control -= 5;
				break;
			}
			break;
		case R.id.nextTrainingsImageButton:
			prev.setClickable(true);
			int diff = userTrainingsName.size() - control;
			switch (diff) {
			case 1:
				name1.setText(userTrainingsName.get(control));
				name2.setText("");
				name3.setText("");
				name4.setText("");
				name5.setText("");

				date1.setText(userTrainingsDate.get(control));
				date2.setText("");
				;
				date3.setText("");
				date4.setText("");
				date5.setText("");

				view2.setVisibility(View.INVISIBLE);
				view3.setVisibility(View.INVISIBLE);
				view4.setVisibility(View.INVISIBLE);
				view5.setVisibility(View.INVISIBLE);

				edit2.setVisibility(View.INVISIBLE);
				edit3.setVisibility(View.INVISIBLE);
				edit4.setVisibility(View.INVISIBLE);
				edit5.setVisibility(View.INVISIBLE);

				delete2.setVisibility(View.INVISIBLE);
				delete3.setVisibility(View.INVISIBLE);
				delete4.setVisibility(View.INVISIBLE);
				delete5.setVisibility(View.INVISIBLE);

				next.setClickable(false);
				break;
			case 2:
				name1.setText(userTrainingsName.get(control));
				name2.setText(userTrainingsName.get(control + 1));
				name3.setText("");
				name4.setText("");
				name5.setText("");

				date1.setText(userTrainingsDate.get(control));
				date2.setText(userTrainingsName.get(control + 1));
				date3.setText("");
				date4.setText("");
				date5.setText("");

				view3.setVisibility(View.INVISIBLE);
				view4.setVisibility(View.INVISIBLE);
				view5.setVisibility(View.INVISIBLE);

				edit3.setVisibility(View.INVISIBLE);
				edit4.setVisibility(View.INVISIBLE);
				edit5.setVisibility(View.INVISIBLE);

				delete3.setVisibility(View.INVISIBLE);
				delete4.setVisibility(View.INVISIBLE);
				delete5.setVisibility(View.INVISIBLE);

				next.setClickable(false);
				break;
			case 3:
				name1.setText(userTrainingsName.get(control));
				name2.setText(userTrainingsName.get(control + 1));
				name3.setText(userTrainingsName.get(control + 2));
				name4.setText("");
				name5.setText("");

				date1.setText(userTrainingsDate.get(control));
				date2.setText(userTrainingsName.get(control + 1));
				date3.setText(userTrainingsName.get(control + 2));
				date4.setText("");
				date5.setText("");

				view4.setVisibility(View.INVISIBLE);
				view5.setVisibility(View.INVISIBLE);

				edit4.setVisibility(View.INVISIBLE);
				edit5.setVisibility(View.INVISIBLE);

				delete4.setVisibility(View.INVISIBLE);
				delete5.setVisibility(View.INVISIBLE);

				next.setClickable(false);
				break;
			case 4:
				name1.setText(userTrainingsName.get(control));
				name2.setText(userTrainingsName.get(control + 1));
				name3.setText(userTrainingsName.get(control + 2));
				name4.setText(userTrainingsName.get(control + 3));
				name5.setText("");

				date1.setText(userTrainingsDate.get(control));
				date2.setText(userTrainingsName.get(control + 1));
				date3.setText(userTrainingsName.get(control + 2));
				date4.setText(userTrainingsName.get(control + 3));
				date5.setText("");

				view5.setVisibility(View.INVISIBLE);

				edit5.setVisibility(View.INVISIBLE);

				delete5.setVisibility(View.INVISIBLE);

				next.setClickable(false);
				break;
			case 5:
				name1.setText(userTrainingsName.get(control));
				name2.setText(userTrainingsName.get(control + 1));
				name3.setText(userTrainingsName.get(control + 2));
				name4.setText(userTrainingsName.get(control + 3));
				name5.setText(userTrainingsName.get(control + 4));

				date1.setText(userTrainingsDate.get(control));
				date2.setText(userTrainingsName.get(control + 1));
				date3.setText(userTrainingsName.get(control + 2));
				date4.setText(userTrainingsName.get(control + 3));
				date5.setText(userTrainingsName.get(control + 4));

				next.setClickable(false);
				break;
			default:
				name1.setText(userTrainingsName.get(control));
				name2.setText(userTrainingsName.get(control + 1));
				name3.setText(userTrainingsName.get(control + 2));
				name4.setText(userTrainingsName.get(control + 3));
				name5.setText(userTrainingsName.get(control + 4));

				date1.setText(userTrainingsDate.get(control));
				date2.setText(userTrainingsName.get(control + 1));
				date3.setText(userTrainingsName.get(control + 2));
				date4.setText(userTrainingsName.get(control + 3));
				date5.setText(userTrainingsName.get(control + 4));

				control += 5;
				break;
			}
			break;
		}

	}

	// Método donde se construye la alerta de borrar el entrenamiento
	public void alertDeleteTraining() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					new DeleteTraining().execute();
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					finish();
					startActivity(getIntent());
					break;

				case DialogInterface.BUTTON_NEGATIVE:

					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Precaución");
		builder.setMessage("Perderá todos los datos referentes al entrenamiento ¿Desea continuar?")
				.setPositiveButton("Sí", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
	}

	// Clase asíncrona utilizada para llamar al servicio web deleteTraining
	class DeleteTraining extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			deleteTraining();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web deleteTraining
	private void deleteTraining() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/deleteTraining.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("trainingname", trainingToDelete));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Clase asíncrona utilizada para llamar al servicio web loadTrainings
	class LoadTrainings extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			loadTrainings();
			synchronized (lock) {

				lock.notify();
			}
			return null;
		}
	}

	// Método donde se ejecuta el servicio web loadTrainings, donde cargamos los
	// entrenamientos pertenecientes al usuario
	private void loadTrainings() {
		String request = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://IP_SERVIDOR/webservices/loadTrainings.php");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair("username", NavigationActivity.user.getUserName()));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			request = client.execute(post, responseHandler);
			String[] datas = request.split("<br>");
			if (!(datas.length == 0)) {
				for (int i = 1; i < datas.length; i++) {
					String[] sections = datas[i].split("/");
					userTrainingsName.add(sections[0]);
					userTrainingsDate.add(sections[1]);
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
}
