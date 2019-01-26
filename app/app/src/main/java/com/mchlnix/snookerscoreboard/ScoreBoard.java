package com.mchlnix.snookerscoreboard;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

        Thread thread = new Thread(new GameStateUpdater(), "Gamestate update thread");

        thread.start();
    }

    public String getGameState()
    {
        Spinner spinner_player1 = findViewById(R.id.spinner_player1);
        Spinner spinner_player2 = findViewById(R.id.spinner_player2);

        String player1 = spinner_player1.getSelectedItem().toString();
        String player2 = spinner_player2.getSelectedItem().toString();

        int points1 = (int) (Math.random() * 148);
        int points2 = (int) (Math.random() * 148);

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

                        Log.d("THREAD", "run: Hi");

                        Thread.sleep(1000);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
