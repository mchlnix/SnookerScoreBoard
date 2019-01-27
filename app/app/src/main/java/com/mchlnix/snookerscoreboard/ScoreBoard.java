package com.mchlnix.snookerscoreboard;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ScoreBoard extends AppCompatActivity {
    boolean foulMode = false;
    int player1 = R.id.avatar_player1;
    int player2 = R.id.avatar_player2;

    int currentPlayer = player1;
    int waitingPlayer = player2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        this.highlightPlayer(currentPlayer);

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

        String player_name1 = spinner_player1.getSelectedItem().toString();
        String player_name2 = spinner_player2.getSelectedItem().toString();

        NumberPicker score_picker1 = findViewById(R.id.score_picker1);
        NumberPicker score_picker2 = findViewById(R.id.score_picker2);

        int points1 = score_picker1.getValue();
        int points2 = score_picker2.getValue();

        return "{\"player1\": {\"name\": \"" + player_name1 + "\", \"score\": " + points1 + ", \"is_playing\": " +  (player1 == currentPlayer) + "}, \"player2\": {\"name\": \"" + player_name2 + "\", \"score\": " + points2 + ",  \"is_playing\": " + (player2==currentPlayer) + "}}";
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
        // todo prettify
        NumberPicker score_player1 = findViewById(R.id.score_picker1);
        NumberPicker score_player2 = findViewById(R.id.score_picker2);

        NumberPicker score_current, score_waiting, score;

        if (currentPlayer == player1) {
            score_current = score_player1;
            score_waiting = score_player2;
        }
        else
        {
            score_current = score_player2;
            score_waiting = score_player1;
        }

        if (this.foulMode)
            score = score_waiting;
        else
            score = score_current;

        this.setFoulMode(false);

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
        }

        score.setValue(score.getValue() + points);
    }

    public void whiteBall(View view)
    {
        this.setFoulMode(! this.foulMode);
    }

    private void setFoulMode(boolean state)
    {
        this.foulMode = state;

        ImageButton whiteBall = findViewById(R.id.whiteBall);

        if (this.foulMode)
        {
            whiteBall.setColorFilter(Color.argb(0x40, 0x00, 0x80, 0xFF));
        }
        else
        {
            whiteBall.setColorFilter(Color.argb(0x00, 0x00, 0x00, 0x00));
        }
    }

    public void setCurrentPlayer(View v)
    {
        if (currentPlayer == v.getId())
            return;

        this.waitingPlayer = currentPlayer;
        this.currentPlayer = v.getId();

        unhighlightPlayer(waitingPlayer);
        highlightPlayer(currentPlayer);
    }

    private void highlightPlayer(int id)
    {
        ImageView avatar = findViewById(id);

        avatar.setColorFilter(Color.argb(0x40, 0x00, 0x80, 0xFF));
    }

    private void unhighlightPlayer(int id)
    {
        ImageView avatar = findViewById(id);

        avatar.setColorFilter(Color.argb(0x00, 0x00, 0x00, 0x00));
    }
}
