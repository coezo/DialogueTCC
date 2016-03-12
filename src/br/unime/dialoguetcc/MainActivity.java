package br.unime.dialoguetcc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private TextView textMatch;
	private Button btSpeak;
	private TextView conversation;
	private CheckBox cbMPlants;
	
	private TextToSpeech tts;
	private OnInitListener onInitListener;
	private DialogueManager dialogueManager;
	
	public static final int ASR_REQUEST_CODE = 1001;
	public static final int TTS_REQUEST_CODE = 1002;
	
	private static final int MAX_RESULTS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prepareLayout();
		
		prepareTTS();
		
		checkASR();
		
		prepareDialogueManager();

	}
		
	private void prepareLayout(){
		textMatch = (TextView) findViewById(R.id.tvTextMatch);
		btSpeak = (Button) findViewById(R.id.btSpeak);
		conversation = (TextView) findViewById(R.id.conversation);
		cbMPlants = (CheckBox) findViewById(R.id.cbMPlants);
		
		btSpeak.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listen();
				tts.stop();
			}
		});
		
		cbMPlants.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dialogueManager.setMedPlantsSpiralVisited(isChecked);
			}
		});
	}
		
	private void checkASR(){
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			btSpeak.setEnabled(false);
			btSpeak.setText(getString(R.string.asr_unavailable));
			showToastMessage(getString(R.string.asr_unavailable));
		}
	}
	
	public void listen() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
				.getPackage().getName());

		// Display an hint to the user about what he should say.
//		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint.getText().toString());

		// Given an hint to the recognizer about what the user is going to say
		//There are two form of language model available
		//1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		//2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		// If number of Matches is not selected then return show toast message
//		if (msTextMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
//			Toast.makeText(this, "Please select No. of Matches from spinner", 
//					Toast.LENGTH_SHORT).show();
//		    return;
//		}

//		int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem().toString());
		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS);
		//Start the Voice recognizer activity for the result.
		startActivityForResult(intent, MainActivity.ASR_REQUEST_CODE);
	}
	
	public String processRecognitionResult(Intent data){
		ArrayList<String> textMatchList = data
				.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		
		String speech = "";

		if (!textMatchList.isEmpty()) {
			// populate the Match
	        speech = textMatchList.get(0);
		}
		
		return speech;
	}
	
	private void prepareTTS(){
		
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, TTS_REQUEST_CODE);
		onInitListener = new OnInitListener() {
			
			@Override
			public void onInit(int status) {
				if(status == TextToSpeech.SUCCESS) {
					Locale locale = new Locale("pt", "BR");
					tts.setLanguage(locale);
					tts.setSpeechRate((float) 2.0);
//					tts.setPitch((float) 2.0);
					//after it's successfully init, say the intro.
					read(getString(R.string.edible_plants_intro));
				}
				
			}
		};
		
	}
	
	private void prepareDialogueManager(){
		dialogueManager = new DialogueManager(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ASR_REQUEST_CODE){

			//If Voice recognition is successful then it returns RESULT_OK
			if(resultCode == RESULT_OK) {
				
				String speech = processRecognitionResult(data);
				textMatch.setText(speech);
				
				String response = dialogueManager.getResponse(speech);
				
				conversation.setText(response);
//				
				read(response);
			
			//Result code for various error.
	   		} else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
		   		showToastMessage(getString(R.string.audio_error));
		    } else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
		    	showToastMessage(getString(R.string.client_error));
		    } else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
		    	showToastMessage(getString(R.string.network_error));
		    } else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
		    	showToastMessage(getString(R.string.no_match));
		    } else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
		    	showToastMessage(getString(R.string.server_error));
		    }
		} else if(requestCode == TTS_REQUEST_CODE) {
			if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				tts = new TextToSpeech(this, onInitListener);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void showToastMessage(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	private void read(String text){
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	@Override
	protected void onDestroy() {
		tts.shutdown();
		super.onDestroy();
	}

}
