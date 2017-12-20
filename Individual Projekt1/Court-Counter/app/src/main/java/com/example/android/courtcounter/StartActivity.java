/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.courtcounter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.TextView;

/**
 * This activity keeps track of the basketball score for 2 teams.
 */
public class StartActivity extends AppCompatActivity {

    // Tracks the score for Team A
    int scoreTeamA = 0;
    // Tracks the score for Team B
    int scoreTeamB = 0;
    boolean a = false;
    boolean b = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String jamesbond = "hi";
        String jamesBond = "hello";
        String s = jamesBond + jamesbond;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Context context = getApplicationContext();
        CharSequence text ="Welcome to court counter!";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimpSlifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    /**This part is for store the states before the rotation*/

    public void onSaveInstanceState(Bundle savedInstanceState){
        //save all the current state and prepare to reload after rotation
        //String name_a = "" + ((EditText)findViewById(R.id.team_a)).getText();
        savedInstanceState.putInt("team_a", scoreTeamA); //save the score for team a and b for rotation
        savedInstanceState.putInt("team_b", scoreTeamB);
        //savedInstanceState.putString("name_a",name_a);

        super.onSaveInstanceState(savedInstanceState);

    }
    @Override
    /**This method is for reload the state after the rotation*/
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        scoreTeamA = savedInstanceState.getInt("team_a");
        scoreTeamB = savedInstanceState.getInt("team_b");
        displayForTeamA(scoreTeamA); //get and display the score after the rotation
        displayForTeamB(scoreTeamB);

        ImageButton imga = (ImageButton) findViewById(R.id.img_a);
        if(a = true){
        imga.setImageResource(R.drawable.sledge);}
        else {imga.setImageResource(R.drawable.ash);}

        ImageButton imgb = (ImageButton) findViewById(R.id.img_b);
        if(b = true){
            imgb.setImageResource(R.drawable.monti);}
        else {imgb.setImageResource(R.drawable.ela);}

//       String a_string = savedInstanceState.getString("name_a");
//       EditText a_name = (EditText) findViewById(R.id.team_a);
//       a_name.setText(a_string);

    }


   //change the icon of the team
    public void changeA(View v){

        ImageButton change = (ImageButton)v;
        change.setImageResource(R.drawable.ash);
        a = true;
    }

    public void changeB(View v){
        ImageButton change = (ImageButton)v;
        change.setImageResource(R.drawable.ela);
        b = true;
    }

  //get the winner
    public String getWinner(int scoreTeamA, int scoreTeamB){
        EditText teama = (EditText)findViewById(R.id.team_a);
        String team_a = teama.getText().toString();
        EditText teamb = (EditText)findViewById(R.id.team_b);
        String team_b = teamb.getText().toString();
        String winner = "";

        if(team_a.length() == 0){
            team_a = "team A"
            ;}
        if (team_b.length() == 0){
            team_b = "team B";
        }

        if(scoreTeamA > scoreTeamB){
            winner = team_a;
        }
        else if (scoreTeamA < scoreTeamB){
            winner = team_b;
        }
        else{
            winner = "Both team";
        }



     return winner;
    }


    /**
     * Increase the score for Team A by 1 point.
     */
    public void addOneForTeamA(View v) {
        scoreTeamA = scoreTeamA + 1;
        displayForTeamA(scoreTeamA);
    }

    /**
     * Increase the score for Team A by 2 points.
     */
    public void addTwoForTeamA(View v) {
        scoreTeamA = scoreTeamA + 2;
        displayForTeamA(scoreTeamA);
    }

    /**
     * Increase the score for Team A by 3 points.
     */
    public void addThreeForTeamA(View v) {
        scoreTeamA = scoreTeamA + 3;
        displayForTeamA(scoreTeamA);
    }

    /**
     * Increase the score for Team B by 1 point.
     */
    public void addOneForTeamB(View v) {
        scoreTeamB = scoreTeamB + 1;
        displayForTeamB(scoreTeamB);
    }

    /**
     * Increase the score for Team B by 2 points.
     */
    public void addTwoForTeamB(View v) {
        scoreTeamB = scoreTeamB + 2;
        displayForTeamB(scoreTeamB);
    }

    /**
     * Increase the score for Team B by 3 points.
     */
    public void addThreeForTeamB(View v) {
        scoreTeamB = scoreTeamB + 3;
        displayForTeamB(scoreTeamB);
    }

    public void minusoneforTeamA(View v){
        scoreTeamA = scoreTeamA - 1;
        displayForTeamA(scoreTeamA);
    }
    public void minusoneforTeamB(View v){
        scoreTeamB = scoreTeamB - 1;
        displayForTeamB(scoreTeamB);
    }

    /**
     * Resets the score for both teams back to 0.
     */
    public void resetScore(View v) {
        scoreTeamA = 0;
        scoreTeamB = 0;
        displayForTeamA(scoreTeamA);
        displayForTeamB(scoreTeamB);
    }

    public void sendEmail(View v){

        EditText teama = (EditText)findViewById(R.id.team_a);
        String team_a = teama.getText().toString();
        EditText teamb = (EditText)findViewById(R.id.team_b);
        String team_b = teamb.getText().toString();

        if(team_a.length() == 0){
            team_a = "team A"
            ;}
        if (team_b.length() == 0){
            team_b = "team B";
        }

        String win = getWinner(scoreTeamA,scoreTeamB);
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"email@email.com"});
        email.putExtra(Intent.EXTRA_CC,new String[]{"email@email.com"});
        email.putExtra(Intent.EXTRA_SUBJECT,"Subject");
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_TEXT,"the winner is "+ win+"\n"+"Team A is " + team_a+ " and the score is " + scoreTeamA +"\n" + "Team B is " + team_b + " and the score is "+ scoreTeamB);
        startActivity(Intent.createChooser(email,""));

    }

    /**
     * Displays the given score for Team A.
     */
    public void displayForTeamA(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_a_score);
        scoreView.setText(String.valueOf(score));
    }

    /**
     * Displays the given score for Team B.
     */
    public void displayForTeamB(int score) {
        TextView scoreView = (TextView) findViewById(R.id.team_b_score);
        scoreView.setText(String.valueOf(score));
    }

}
