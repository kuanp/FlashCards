package ex.example.flashcards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    Random random;
    Set<Integer> usedWordIndices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer.setAudioAttributes(
                new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
        random = new Random();
        usedWordIndices = new HashSet<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shuffleCards(null);

        SeekBar volumeBar = findViewById(R.id.volumeBar);
        volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean pausedMedia;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pausedMedia = true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (pausedMedia) {
                    mediaPlayer.start();
                    pausedMedia = false;
                }
            }
        });
    }

    public void shuffleCards(View view) {
        usedWordIndices.clear();
        ConstraintLayout flashCardLayout = findViewById(R.id.flashCardLayout);
        for (int i = 0; i < flashCardLayout.getChildCount(); i++) {
            Button card = (Button) flashCardLayout.getChildAt(i);
            card.setText(pickWord());
        }

    }

    public void playCard(View view) {
        Button clickedButton = (Button) view;
        String audioPath = "android.resource://" +
                this.getPackageName() +
                "/raw/" +
                FlashCardPaths.FLASH_CARD_PATHS_MAP.get(clickedButton.getText());

        Log.v("CARD", "PATH: " + audioPath);


        mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(this, getResources().getIdentifier(
                FlashCardPaths.FLASH_CARD_PATHS_MAP.get(clickedButton.getText()),
                "raw",
                getPackageName()
        ));
        mediaPlayer.start();
    }

    private String pickWord() {
        int numWordsAvailable = FlashCardPaths.FLASH_CARD_PATHS_MAP.size();
        List<String> keys = new ArrayList<>(FlashCardPaths.FLASH_CARD_PATHS_MAP.keySet());

        if (usedWordIndices.size() < numWordsAvailable) {
            int potentialWordIndex = random.nextInt(numWordsAvailable);
            while (usedWordIndices.contains(potentialWordIndex)) {
                potentialWordIndex = random.nextInt(numWordsAvailable);
            }

            usedWordIndices.add(potentialWordIndex);
            return keys.get(potentialWordIndex);

        } else {
            throw new RuntimeException("Woops Ran Out of Words.");
        }
    }
}
