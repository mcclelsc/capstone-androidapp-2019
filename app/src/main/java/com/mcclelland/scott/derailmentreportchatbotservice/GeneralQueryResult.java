package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GeneralQueryResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_query_result);
        final Context context = this;

        Bundle generalBundle = getIntent().getExtras();
        String generalQuery = generalBundle.getString("query");
        new ProcessQuery(context).execute(generalQuery);
    }

    private class ProcessQuery extends AsyncTask<String, Void, String> {
        Context context;
        public ProcessQuery(Context context){
            this.context = context;
        }
        @Override
        protected void onPreExecute(){
            //progress = ProgressDialog.show(Conversation.this, "Starting Conversation...", "Please Wait");
        }
        protected String doInBackground(String... params) {
            String generalQuery = params[0];
            JSONObject json = new JSONObject();

            String urlString = "https://capstone-middleware-2019.herokuapp.com/generalDiscoveryQuery";
            String responsePayloadString = "";
            try {
                json.put("message", generalQuery);
            }catch (JSONException e){
                throw new RuntimeException(e);
            }
            MiddlewareConnector middlewareConnection = new MiddlewareConnector(urlString, json.toString());
            responsePayloadString = middlewareConnection.connect();
            return responsePayloadString;
        }
        protected void onPostExecute(String payload) {
            try {
                JSONObject jsonCollection = new JSONObject(payload);
                JSONObject tempObject;
                int tempPassageCount = 0;
                //System.out.println(jsonArray.toString());
                JSONArray resultsArray = jsonCollection.getJSONArray("results");
                final ArrayList<DocumentKeyPair> documentKeyPairs = new ArrayList<DocumentKeyPair>();
                final ArrayList<DocumentDetails> documentList = new ArrayList<DocumentDetails>();
                for (int i = 0; i < resultsArray.length(); i++){
                    tempObject = resultsArray.getJSONObject(i);
                    documentList.add(new DocumentDetails(tempObject.getString("id"), tempObject.getJSONObject("extracted_metadata").getString("filename"),tempObject.getString("text")));
                }

                JSONArray passagesArray = jsonCollection.getJSONArray("passages");
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

                ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.activity_general_query_result);
                ConstraintSet set = new ConstraintSet();

                TextView addedText;
                String exampleText = "";
                int generatedId = 0;
                System.out.println(documentList.size());
                for (int i = 0; i < documentList.size(); i++){
                    exampleText = "";
                    addedText = new TextView(context);
                    generatedId = View.generateViewId();
                    documentKeyPairs.add(new DocumentKeyPair(generatedId, i));
                    addedText.setId(generatedId);
                    layout.addView(addedText, i);
                    set.clone(layout);
                    if (i == 0){
                        set.connect(addedText.getId(), ConstraintSet.TOP, R.id.textMatchedQuery, ConstraintSet.TOP, 60);
                    }
                    else{
                        set.connect(addedText.getId(), ConstraintSet.TOP, documentKeyPairs.get(i-1).getViewId(), ConstraintSet.BOTTOM, 60);
                    }

                    //set.connect(addedText.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, 60);

                    set.applyTo(layout);

                    if (documentList.get(i).getText().length() > 50){
                        exampleText = documentList.get(i).getText().substring(0, 50);
                    }
                    else{
                        exampleText = documentList.get(i).getText();
                    }

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

                            startActivity(intent);
                        }
                    });
                }

            }catch (JSONException e){
                throw new RuntimeException(e);
            }

        }

    }
}
