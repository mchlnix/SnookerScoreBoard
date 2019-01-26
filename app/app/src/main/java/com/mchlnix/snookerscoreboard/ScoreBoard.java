package com.mchlnix.snookerscoreboard;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Spinner;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ScoreBoard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        NumberPicker numberPicker = findViewById(R.id.score_picker1);

        numberPicker.setMinValue(0);
        numberPicker.setValue(0);
        numberPicker.setMaxValue(147);

        numberPicker = findViewById(R.id.score_picker2);

        numberPicker.setMinValue(0);
        numberPicker.setValue(0);
        numberPicker.setMaxValue(147);

        Thread thread = new Thread(new GameStateUpdater(), "Gamestate update thread");

        thread.start();
    }

    public String getGameState()
    {
        Spinner spinner_player1 = findViewById(R.id.spinner_player1);
        Spinner spinner_player2 = findViewById(R.id.spinner_player2);

        String player1 = spinner_player1.getSelectedItem().toString();
        String player2 = spinner_player2.getSelectedItem().toString();

        NumberPicker score_picker1 = findViewById(R.id.score_picker1);
        NumberPicker score_picker2 = findViewById(R.id.score_picker2);

        int points1 = score_picker1.getValue();
        int points2 = score_picker2.getValue();

        return "{\"player1\": {\"name\": \"" + player1 + "\", \"score\": " + points1 + ", \"is_playing\": false}, \"player2\": {\"name\": \"" + player2 + "\", \"score\": " + points2 + ",  \"is_playing\": false}}";
    }

    class GameStateUpdater implements Runnable
    {
        @Override
        public void run() {
            try {
                Socket socket = new Socket("192.168.2.112", 8888);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                    while (true) {
                        String state = getGameState();
                        byte[] stateBytes = state.getBytes(StandardCharsets.UTF_8);

                        ByteBuffer payload = ByteBuffer.allocate(stateBytes.length + 4);
                        payload.putInt(stateBytes.length);
                        payload.put(stateBytes);

                        socket.getOutputStream().write(payload.array());

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void scoreBall(View v)
    {
        NumberPicker score = findViewById(R.id.score_picker1);

        int points = 0;

        switch(v.getId())
        {
            case R.id.redBall:
                points = 1;
                break;

            case R.id.yellowBall:
                points = 2;
                break;

            case R.id.greenBall:
                points = 3;
                break;

            case R.id.brownBall:
                points = 4;
                break;

            case R.id.blueBall:
                points = 5;
                break;

            case R.id.pinkBall:
                points = 6;
                break;

            case R.id.blackBall:
                points = 7;
                break;

            case R.id.whiteBall:
                points = 4;
                break;
        }

        score.setValue(score.getValue() + points);
    }
}
