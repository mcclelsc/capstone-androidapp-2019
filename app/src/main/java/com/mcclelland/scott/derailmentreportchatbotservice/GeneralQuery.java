package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GeneralQuery extends AppCompatActivity {

    DrawerLayout globalNavDrawerLayout;

    String middlewareURL = "https://demo-middleware.herokuapp.com";
    private Boolean realUserSelect = false;
    Spinner spinnerSuggestedQueries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_query);

        globalNavDrawerLayout = findViewById(R.id.drawer_layout_generalQuery);

        NavigationView navigationView = findViewById(R.id.navigation_menu);
        final ImageButton globalNavImage = findViewById(R.id.btnGlobalNav);

        globalNavDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                globalNavImage.setImageResource(R.drawable.close_icon);

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                globalNavImage.setImageResource(R.drawable.menu_icon);
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.conversationNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.START);
                        globalNavImage.setImageResource(R.drawable.menu_icon);
                        Intent intent = new Intent(GeneralQuery.this, Conversation.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.generalQueryNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.START);
                        globalNavImage.setImageResource(R.drawable.menu_icon);
                        Intent intent = new Intent(GeneralQuery.this, GeneralQuery.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.uploadDocumentNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.START);
                        globalNavImage.setImageResource(R.drawable.menu_icon);
                        Intent intent = new Intent(GeneralQuery.this, UploadDocument.class);
                        startActivity(intent);
                        break;
                    }
                }
                return true;
            }
        });

        globalNavImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalNavImage.setImageResource(R.drawable.close_icon);
                globalNavDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        final Context context = this;

        Button btnSubmit = (Button)findViewById(R.id.btnQuerySubmit);
        final EditText editGeneralQuery = (EditText)findViewById(R.id.editGeneralQuery);
        //Spinner (dropdown) wigits need their own adapters to populate their rows.
        //Below the adapter populates itself based on a string_array that can be found
        //under /res/values/strings.xml
        spinnerSuggestedQueries = findViewById(R.id.spinnerSuggestedQueries);
        //ArrayAdapter<String> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.suggested_questions, android.R.layout.simple_spinner_item);
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinnerSuggestedQueries.setAdapter(spinnerAdapter);

        new SuggestedQuestions(context).execute();

        //Spinners have a strange functionality of their rows being called both when clicked,
        //and when the acitivty is being created. However unlike the OnItemSelected Event,
        //the Touch event only fires during user press.
        spinnerSuggestedQueries.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                realUserSelect = true;
                return false;
            }
        });

        spinnerSuggestedQueries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //When touch event sets boolean value to true, populate the editGeneralQuery view
                if (realUserSelect){
                    if (i == 0){
                        editGeneralQuery.setText("");
                    }
                    else{
                        if (!spinnerSuggestedQueries.getSelectedItem().toString().equals("Suggested Questions")) {
                            editGeneralQuery.setText(spinnerSuggestedQueries.getSelectedItem().toString());
                        }
                    }
                }
                realUserSelect = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendToGeneralQueryResult(editGeneralQuery);
            }
        });

    }

    private void sendToGeneralQueryResult(EditText editText){
        if (editText.getText().toString().equals("") || editText.getText().toString().trim().length() == 0){
            Toast.makeText(getApplicationContext(),"Please enter a Query", Toast.LENGTH_SHORT).show();
        }
        else{
            Bundle generalBundle = new Bundle();
            generalBundle.putString("query", editText.getText().toString());
            Intent i = new Intent(GeneralQuery.this, GeneralQueryResult.class);
            i.putExtras(generalBundle);
            startActivity(i);
        }

    }

    private class SuggestedQuestions extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        Context context;

        SuggestedQuestions(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute(){

        }
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {

            String responsePayloadString = "";
            String urlString = middlewareURL + "/suggestedQuestions";
            JSONObject json = new JSONObject();
            ArrayList<String> questions = new ArrayList<>();

            try {
                json.put("message", "blank");
            }catch (JSONException e){
                throw new RuntimeException(e);
            }

            MiddlewareConnector middlewareConnection = new MiddlewareConnector(urlString, json.toString());
            responsePayloadString = middlewareConnection.connect();

            //Unpack the results of the user's query
            JSONObject payloadObject;
            JSONArray payloadArray;
            try {
                payloadObject = new JSONObject(responsePayloadString);
                payloadArray = payloadObject.getJSONArray("questions");
                for (int i = 0; i < payloadArray.length(); i++){
                    questions.add(payloadArray.get(i).toString());
                }
            }catch (JSONException e){
                throw new RuntimeException(e);
            }

            return questions;
        }
        protected void onPostExecute(ArrayList<String> questions) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, questions);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSuggestedQueries.setAdapter(spinnerAdapter);
        }

    }

}
