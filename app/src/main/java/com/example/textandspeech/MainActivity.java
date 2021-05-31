package com.example.textandspeech;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private TextView inputText;
    private TextView message;
    private Button micButton;
    private Countries countries;
    private Object[] candidates;
    private String response;
    private char firstLetter = '`';

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.spokenText);
        message = findViewById(R.id.textMessage);
        micButton = findViewById(R.id.micButton);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        countries = new Countries(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        getListenerForSpeechRecognizer();
        getListenerForMicButton(speechRecognizerIntent);

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });
    }

    private void getListenerForSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                inputText.setText("...");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onBeginningOfSpeech() {
                inputText.setText("Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onEndOfSpeech() {
                inputText.setText("Stop listening");
            }

            @Override
            public void onError(int error) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResults(Bundle results) {
                micButton.setBackgroundResource(R.drawable.mic);
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String str = data.get(0);
                inputText.setText(str);
                str = str.toLowerCase();
                if (str.charAt(0) != firstLetter && firstLetter != '`') {
                    message.setText("Не пытайтесь меня обмануть!");
                } else {
                    if (countries.set.remove(str)) {
                        char last = str.charAt(str.length() - 1);
                        if (last == 'ы' || last == 'ь') {
                            last = str.charAt(str.length() - 2);
                        }
                        candidates = countries.getCountriesStartWith(String.valueOf(last));
                        response = new Answerer().getAnswer(candidates, countries);
                        firstLetter = response.charAt(response.length() - 1);
                    } else {
                        response = "Такой страны нет!";
                    }
                    message.setText(response);
                    textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, "response");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                System.out.println("partial results");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void getListenerForMicButton(final Intent speechRecognizerIntent) {
        micButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP){
                speechRecognizer.stopListening();
                micButton.setBackgroundResource(R.drawable.mic);
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                micButton.setBackgroundResource(R.drawable.dyn);
                speechRecognizer.startListening(speechRecognizerIntent);
            }
            return false;
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }
}
