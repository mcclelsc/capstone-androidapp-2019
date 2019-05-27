package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.assistant.v2.Assistant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Conversation extends AppCompatActivity {

    RecyclerViewAdapter recyclerViewAdapter;
    EditText editMessage;
    RecyclerView recyclerView;
    ArrayList<String> chatMessageLog;
    ArrayList<PassageDetails> passageCollection;
    Assistant chatAssistant;
    String documentFilename = "";
    String documentFileId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        IamOptions options = new IamOptions.Builder()
                .apiKey("jP5yGzV5NNzsfS7NG5xmDg96b9Dj6_t0kug5Kg6nEQUM")
                .build();

        chatAssistant = new Assistant("2019-02-28", options);

        chatAssistant.setEndPoint("https://gateway.watsonplatform.net/assistant/api");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Watson-Learning-Opt-Out", "true");

        chatAssistant.setDefaultHeaders(headers);

        chatMessageLog = new ArrayList<>();

        Button sendButton = findViewById(R.id.sendMessage);
        editMessage = findViewById(R.id.editMessage);
        recyclerView = findViewById(R.id.chatBox);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new RecyclerViewAdapter(this, chatMessageLog);
        //recyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);

        final Context context = this;
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new MessageWatson(context).execute(chatMessageLog);
            }
        });

        Bundle checkBundle = getIntent().getExtras();
        if (checkBundle != null){
            documentFileId = checkBundle.getString("documentId");
            documentFilename = checkBundle.getString("documentFilename");
        }
        new StartWatson(context).execute(chatMessageLog);

    }

    private void updateChatbox(Context context, ArrayList<String> chatMessageLog){
        recyclerViewAdapter = new RecyclerViewAdapter(context, chatMessageLog);
        //recyclerViewAdapter.setClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
        private List<String> chatData;
        private LayoutInflater inflater;
        private ItemClickListener rowClickListener;

        RecyclerViewAdapter(Context context, List<String> data){
            this.inflater = LayoutInflater.from(context);
            this.chatData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = inflater.inflate(R.layout.conversation_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position){
            String message = chatData.get(position);
            holder.rowTextView.setText(message);
        }

        @Override
        public int getItemCount() {
            return chatData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView rowTextView;

            ViewHolder(View itemView){
                super(itemView);
                rowTextView = itemView.findViewById(R.id.messageRow);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view){
                if (rowClickListener != null) rowClickListener.onItemClick(view, getAdapterPosition());
            }

        }
        void setClickListener(ItemClickListener itemClickListener) {
            this.rowClickListener = itemClickListener;
        }

    }

    private class StartWatson extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        Context context;

        StartWatson(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute(){
            //progress = ProgressDialog.show(Conversation.this, "Starting Conversation...", "Please Wait");
        }
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            final ArrayList<String> currentChatLog = params[0];
            String responsePayloadString = "";
            String urlString = "https://capstone-middleware-2019.herokuapp.com/startConversation";
            JSONObject json = new JSONObject();
            try {
                if (documentFileId.equals("")){
                    json.put("message", "initiateConversation");
                }
                else{
                    json.put("message", "alreadyHaveDocumentId");
                }
            }catch (JSONException e){
                throw new RuntimeException(e);
            }
            MiddlewareConnector middlewareConnection = new MiddlewareConnector(urlString, json.toString());
            responsePayloadString = middlewareConnection.connect();
            if (responsePayloadString.equals("How may I help you with the chosen report?")){
                responsePayloadString = "How many I help you with report " + documentFilename + "?";
            }

            currentChatLog.add(responsePayloadString);

            return currentChatLog;
        }
        protected void onPostExecute(ArrayList<String> currentChatLog) {
            updateChatbox(context, currentChatLog);
        }

    }

    private class MessageWatson extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        Context context;

        MessageWatson(Context context){
            this.context = context;
        }
        @Override
        protected void onPreExecute(){
            //progress = ProgressDialog.show(Conversation.this, "Starting Conversation...", "Please Wait");
        }
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            final ArrayList<String> currentChatLog = params[0];
            String responsePayloadString = "";
            String enteredMessage = editMessage.getText().toString();
            JSONObject json = new JSONObject();
            try {
                json.put("message", enteredMessage);
            }catch (JSONException e){
                throw new RuntimeException(e);
            }

            String urlString = "https://capstone-middleware-2019.herokuapp.com/continueConversation";
            MiddlewareConnector middlewareConnection = new MiddlewareConnector(urlString, json.toString());
            responsePayloadString = middlewareConnection.connect();
            System.out.println(responsePayloadString);
            if (responsePayloadString.equals("generalDiscoveryQuery")) {
                Bundle generalBundle = new Bundle();
                generalBundle.putString("query", enteredMessage);
                Intent i = new Intent(Conversation.this, GeneralQueryResult.class);
                i.putExtras(generalBundle);
                startActivity(i);
            }
            else if (responsePayloadString.contains("Give me a moment to find that report.")){
                String [] splitString = responsePayloadString.split(";uniqueDelimiter;");
                currentChatLog.add(enteredMessage);
                currentChatLog.add(splitString[0]);
                documentFilename = splitString[1];
                try {
                    json.put("filename", splitString[1]);
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }
                urlString = "https://capstone-middleware-2019.herokuapp.com/getDocumentId";
                middlewareConnection = new MiddlewareConnector(urlString, json.toString());
                responsePayloadString = middlewareConnection.connect();
                splitString = responsePayloadString.split(";uniqueDelimiter;");
                if (splitString[0].equals("reportFound")){
                    documentFileId = splitString[1];
                }
                try {
                    json.put("message", splitString[0]);
                    json.remove("filename");
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }
                urlString = "https://capstone-middleware-2019.herokuapp.com/continueConversation";
                middlewareConnection = new MiddlewareConnector(urlString, json.toString());
                responsePayloadString = middlewareConnection.connect();
                currentChatLog.add(responsePayloadString);
            }
            else if(responsePayloadString.equals("specificDiscoveryQuery")){
                try {
                    json.put("documentId", documentFileId);
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }

                urlString = "https://capstone-middleware-2019.herokuapp.com/specificDiscoveryQuery";
                middlewareConnection = new MiddlewareConnector(urlString, json.toString());
                responsePayloadString = middlewareConnection.connect();

                JSONObject passagesObject;
                JSONObject passageTemp;
                JSONArray passagesArray;
                try {
                    passagesObject = new JSONObject(responsePayloadString);
                    passagesArray = passagesObject.getJSONArray("passages");
                    passageCollection = new ArrayList<PassageDetails>();
                    for (int i = 0; i < passagesArray.length(); i++){
                        passageTemp = passagesArray.getJSONObject(i);
                        passageCollection.add(new PassageDetails(passageTemp.getString("passage_score"), passageTemp.getString("passage_text")));
                    }
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }

                currentChatLog.add(enteredMessage);
                currentChatLog.add("");
            }
            else{
                currentChatLog.add(enteredMessage);
                currentChatLog.add(responsePayloadString);
            }

            return currentChatLog;
        }
        protected void onPostExecute(ArrayList<String> currentChatLog) {
            updateChatbox(context, currentChatLog);
            editMessage.setText("");
        }

    }
}
