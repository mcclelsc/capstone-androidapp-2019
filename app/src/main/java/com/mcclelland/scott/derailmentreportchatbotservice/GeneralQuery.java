package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class GeneralQuery extends AppCompatActivity {
    DrawerLayout globalNavDrawerLayout;
    private Boolean realUserSelect = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_query);

        globalNavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        final ImageButton globalNavImage = (ImageButton)findViewById(R.id.btnGlobalNav);

        globalNavDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                globalNavImage.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                globalNavImage.setVisibility(View.VISIBLE);
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
                        globalNavDrawerLayout.closeDrawer(GravityCompat.END);
                        globalNavImage.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(GeneralQuery.this, Conversation.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.generalQueryNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.END);
                        globalNavImage.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(GeneralQuery.this, GeneralQuery.class);
                        startActivity(intent);
                        break;
                    }
                    //case R.id.uploadDocumentNavItem: {
                    //break;
                    //}
                }
                return true;
            }
        });

        globalNavImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalNavImage.setVisibility(View.GONE);
                globalNavDrawerLayout.openDrawer(GravityCompat.END);
            }
        });

        Button btnSubmit = (Button)findViewById(R.id.btnQuerySubmit);
        final EditText editGeneralQuery = (EditText)findViewById(R.id.editGeneralQuery);
        //Spinner (dropdown) wigits need their own adapters to populate their rows.
        //Below the adapter populates itself based on a string_array that can be found
        //under /res/values/strings.xml
        final Spinner spinnerSuggestedQueries = (Spinner)findViewById(R.id.spinnerSuggestedQueries);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.suggested_questions, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSuggestedQueries.setAdapter(spinnerAdapter);
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
                        editGeneralQuery.setText(spinnerSuggestedQueries.getSelectedItem().toString());
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

}
