package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GeneralQueryResult extends AppCompatActivity {

    DrawerLayout globalNavDrawerLayout;

    ProgressBar generalQueryResultProgressBar;
    String generalQuery;
    String middlewareURL = "https://demo-middleware.herokuapp.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_query_result);

        globalNavDrawerLayout = findViewById(R.id.drawer_layout_generalQueryResult);

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
                        Intent intent = new Intent(GeneralQueryResult.this, Conversation.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.generalQueryNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.START);
                        globalNavImage.setImageResource(R.drawable.menu_icon);
                        Intent intent = new Intent(GeneralQueryResult.this, GeneralQuery.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.uploadDocumentNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.START);
                        globalNavImage.setImageResource(R.drawable.menu_icon);
                        Intent intent = new Intent(GeneralQueryResult.this, UploadDocument.class);
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

        generalQueryResultProgressBar = findViewById(R.id.generalQueryResultProgressBar);

        final Context context = this;
        //Unpack query data from GeneraQuery Activity
        Bundle generalBundle = getIntent().getExtras();
        generalQuery = generalBundle.getString("query");
        //Start AsyncTask to query the Watson Discovery Service
        //Context must be passed to update and create new views
        //Depending on data received from Discovery
        new ProcessQuery(context).execute(generalQuery);
    }

    private class ProcessQuery extends AsyncTask<String, Void, String> {
        Context context;
        public ProcessQuery(Context context){
            this.context = context;
        }
        @Override
        protected void onPreExecute(){
            generalQueryResultProgressBar.setVisibility(View.VISIBLE);
        }
        protected String doInBackground(String... params) {
            //Retrieve query
            String generalQuery = params[0];
            JSONObject json = new JSONObject();
            //Set middleware destination URL
            String urlString = middlewareURL + "/generalDiscoveryQuery";
            String responsePayloadString = "";
            try {
                //Set the retrieved query as value of key 'message' in JSON payload
                json.put("message", generalQuery);
            }catch (JSONException e){
                throw new RuntimeException(e);
            }
            //Start network connection, return string when complete.
            MiddlewareConnector middlewareConnection = new MiddlewareConnector(urlString, json.toString());
            responsePayloadString = middlewareConnection.connect();
            return responsePayloadString;
        }
        protected void onPostExecute(String payload) {
            //When complete, build new views with the response's payload
            try {
                //Unpack payload into a JSON object
                JSONObject jsonCollection = new JSONObject(payload);
                JSONObject tempObject;
                int tempPassageCount = 0;
                //Get the 'results' array from the payload object
                JSONArray resultsArray = jsonCollection.getJSONArray("results");
                //Two arraylists, the first keeping the document's IDs together in a single
                //object, connecting the Watson Discovery ID to the document's index in the arraylist
                //The second creates an arraylist of DocumentDetails objects
                final ArrayList<DocumentKeyPair> documentKeyPairs = new ArrayList<DocumentKeyPair>();
                final ArrayList<DocumentDetails> documentList = new ArrayList<DocumentDetails>();
                //For each document within the 'results' array, make a new DocumentDetails object
                for (int i = 0; i < resultsArray.length(); i++){
                    tempObject = resultsArray.getJSONObject(i);
                    documentList.add(new DocumentDetails(tempObject.getString("id"), tempObject.getJSONObject("extracted_metadata").getString("filename"),tempObject.getString("text")));
                }
                //Retrieve the passages array from payload
                JSONArray passagesArray = jsonCollection.getJSONArray("passages");
                //Compare the document_id of each passage to the IDs of each
                //DocumentList object, and assign them appropriate passageCounts
                for (int i = 0; i < documentList.size(); i++){
                    tempPassageCount = 0;
                    for (int j = 0; j < passagesArray.length(); j++){
                        tempObject = passagesArray.getJSONObject(j);
                        if (tempObject.getString("document_id").equals(documentList.get(i).getId())){
                            tempPassageCount++;
                        }
                    }
                    documentList.get(i).setPassageCount(tempPassageCount);
                }
                //The current layout has to be stored and cloned so it can be updated with new views
                ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.activity_general_query_result_interior_layout);
                ConstraintSet set = new ConstraintSet();

                TextView addedText;
                String exampleText = "";
                int generatedId = 0;
                //Create a new textview in the activity for each found document
                for (int i = 0; i < documentList.size(); i++){
                    exampleText = "";
                    addedText = new TextView(context);
                    //Views need to have unique IDs otherwise they risk clashing
                    generatedId = View.generateViewId();
                    documentKeyPairs.add(new DocumentKeyPair(generatedId, i));
                    addedText.setId(generatedId);
                    //Add view to layout
                    layout.addView(addedText, i);
                    set.clone(layout);
                    //Set constraints for the text view, the first having different constraint options than following textviews
                    if (i == 0){
                        set.connect(addedText.getId(), ConstraintSet.TOP, R.id.textMatchedQuery, ConstraintSet.TOP, 60);
                    }
                    else{
                        set.connect(addedText.getId(), ConstraintSet.TOP, documentKeyPairs.get(i-1).getViewId(), ConstraintSet.BOTTOM, 60);
                    }
                    //Apply new view to layout
                    set.applyTo(layout);
                    //If the document's sample text is too large, trim it down to 50 characters
                    if (documentList.get(i).getText().length() > 80){
                        exampleText = documentList.get(i).getText().substring(0, 80);
                    }
                    else{
                        exampleText = documentList.get(i).getText();
                    }
                    //Set text and add event listener to the textview
                    addedText.setText("Filename: " + documentList.get(i).getFilename() + "\nIntro Text: " + exampleText + "\nNumber of Passages: " + documentList.get(i).getPassageCount());
                    addedText.setClickable(true);
                    addedText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(GeneralQueryResult.this, GeneralQueryResultSpecific.class);
                            for (int j = 0; j < documentKeyPairs.size(); j++){
                                if (documentKeyPairs.get(j).getViewId() == view.getId()){
                                    intent.putExtra("documentDetails", documentList.get(documentKeyPairs.get(j).getDocumentListId()));
                                }
                            }
                            intent.putExtra("query", generalQuery);

                            startActivity(intent);
                        }
                    });
                }

            }catch (JSONException e){
                throw new RuntimeException(e);
            }
            generalQueryResultProgressBar.setVisibility(View.INVISIBLE);
        }

    }
}
