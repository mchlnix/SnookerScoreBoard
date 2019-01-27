package com.mchlnix.snookerscoreboard;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ScoreBoard extends AppCompatActivity implements NumberPicker.OnValueChangeListener, AdapterView.OnItemSelectedListener, TextWatcher {
    boolean foulMode = false;
    int player1 = R.id.avatar_player1;
    int player2 = R.id.avatar_player2;

    int currentPlayer = player1;
    int waitingPlayer = player2;

    int breakLimit = 0;

    private GameStateUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        ((Spinner) findViewById(R.id.spinner_player1)).setOnItemSelectedListener(this);
        ((Spinner) findViewById(R.id.spinner_player2)).setOnItemSelectedListener(this);
        ((NumberPicker) findViewById(R.id.score_picker1)).setOnValueChangedListener(this);
        ((NumberPicker) findViewById(R.id.score_picker2)).setOnValueChangedListener(this);
        ((EditText) findViewById(R.id.break_limit)).addTextChangedListener(this);

        this.highlightPlayer(currentPlayer);

        NumberPicker numberPicker = findViewById(R.id.score_picker1);

        numberPicker.setMinValue(0);
        numberPicker.setValue(0);
        numberPicker.setMaxValue(147);

        numberPicker = findViewById(R.id.score_picker2);

        numberPicker.setMinValue(0);
        numberPicker.setValue(0);
        numberPicker.setMaxValue(147);

        this.updater = new GameStateUpdater();

        Thread thread = new Thread(this.updater, "Gamestate update thread");

        thread.start();
    }

    public String getGameState()
    {
        Spinner spinner_player1 = findViewById(R.id.spinner_player1);
        Spinner spinner_player2 = findViewById(R.id.spinner_player2);

        String p1_name = spinner_player1.getSelectedItem().toString();
        String p2_name = spinner_player2.getSelectedItem().toString();

        NumberPicker score_picker1 = findViewById(R.id.score_picker1);
        NumberPicker score_picker2 = findViewById(R.id.score_picker2);

        int p1_points = score_picker1.getValue();
        int p2_points = score_picker2.getValue();

        JSONObject p1_json = new JSONObject();
        JSONObject p2_json = new JSONObject();

        JSONObject game = new JSONObject();

        try {
            p1_json.put("name", p1_name);
            p1_json.put("score", p1_points);
            p1_json.put("breaks", 0);
            p1_json.put("is_playing", currentPlayer == player1);

            p2_json.put("name", p2_name);
            p2_json.put("score", p2_points);
            p2_json.put("breaks", 0);
            p2_json.put("is_playing", currentPlayer == player2);

            game.put("breaks", this.breakLimit);
            game.put("player1", p1_json);
            game.put("player2", p2_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return game.toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        this.updater.makeDirty();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        this.updater.makeDirty();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        this.updater.makeDirty();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String breaks = editable.toString();

        if (breaks.length() == 0)
            this.setBreakLimit(0);
        else
            this.setBreakLimit(Integer.parseInt(breaks));
    }

    private void setBreakLimit(int limit)
    {
        this.breakLimit = limit;
        this.updater.makeDirty();
    }

    class GameStateUpdater implements Runnable
    {
        private boolean dirty = true;

        @Override
        public void run() {
            try {
                Socket socket = new Socket("192.168.2.112", 8888);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                    while (true) {
                        if (this.dirty) {
                            String state = getGameState();
                            byte[] stateBytes = state.getBytes(StandardCharsets.UTF_8);

                            ByteBuffer payload = ByteBuffer.allocate(stateBytes.length + 4);
                            payload.putInt(stateBytes.length);
                            payload.put(stateBytes);

                            socket.getOutputStream().write(payload.array());

                            this.dirty = false;
                        }

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void makeDirty()
        {
            this.dirty = true;
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

        this.updater.makeDirty();
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

        this.updater.makeDirty();
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
