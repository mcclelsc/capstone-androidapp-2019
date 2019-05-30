package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
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
import java.util.regex.Pattern;


public class Conversation extends AppCompatActivity {

    Assistant chatAssistant;
    RecyclerViewAdapter recyclerViewAdapter;
    RecyclerView recyclerView;
    EditText editMessage;
    ArrayList<String> chatMessageLog;
    ArrayList<PassageDetails> passageCollection;

    String documentFilename = "";
    String documentFileId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        //The Watson Assistant has specific parameters that need to be set
        //so API calls can be sent to the correct chatbot
        //Watson Assistant Setup Begins
        IamOptions options = new IamOptions.Builder()
                .apiKey("jP5yGzV5NNzsfS7NG5xmDg96b9Dj6_t0kug5Kg6nEQUM")
                .build();

        chatAssistant = new Assistant("2019-02-28", options);

        chatAssistant.setEndPoint("https://gateway.watsonplatform.net/assistant/api");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Watson-Learning-Opt-Out", "true");

        chatAssistant.setDefaultHeaders(headers);
        //Watson Assistant Setup Ends

        chatMessageLog = new ArrayList<>();

        Button sendButton = findViewById(R.id.sendMessage);
        editMessage = findViewById(R.id.editMessage);
        //Chat content will be hosted by a recyclerview, and must be
        //prepared accordingly
        recyclerView = findViewById(R.id.chatBox);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new RecyclerViewAdapter(this, chatMessageLog);
        recyclerView.setAdapter(recyclerViewAdapter);
        ((LinearLayoutManager)recyclerView.getLayoutManager()).setStackFromEnd(true);

        final Context context = this;
        //When the 'Send' button is clicked, pass context and chat log to MessageWatson AsyncTask
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (editMessage.getText().toString().equals("") || editMessage.getText().toString().trim().length() == 0){
                    chatMessageLog.add("Your message did not contain any text. Please provide me with a sentence so I can fulfill your query.");
                    updateChatbox(context, chatMessageLog);
                    editMessage.setText("");
                }
                else{
                    new MessageWatson(context).execute(chatMessageLog);
                }
            }
        });
        //If this Activity is started due to being redirected from the GeneralQueryResultSpecific Activity...
        Bundle checkBundle = getIntent().getExtras();
        if (checkBundle != null){
            //Store the desired report's Watson Discovery ID and filename
            documentFileId = checkBundle.getString("documentId");
            documentFilename = checkBundle.getString("documentFilename");
        }
        //Start the chat
        new StartWatson(context).execute(chatMessageLog);

    }
    //Exterior method that the MessageWatson AsyncTask will call when the
    //recyclerview needs to be updated
    private void updateChatbox(Context context, ArrayList<String> chatMessageLog){
        recyclerViewAdapter = new RecyclerViewAdapter(context, chatMessageLog);
        recyclerView.setAdapter(recyclerViewAdapter);
    }
    //Class and supporting interface for the recyclerview
    private interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
        private List<String> chatData;
        private LayoutInflater inflater;
        private ItemClickListener rowClickListener;

        RecyclerViewAdapter(Context context, List<String> chatData){
            this.inflater = LayoutInflater.from(context);
            this.chatData = chatData;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = inflater.inflate(R.layout.conversation_row, parent, false);;
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position){
            String message = chatData.get(position);
            SpannableStringBuilder messageToHighlight = new SpannableStringBuilder(message);
            int stringIndexStart = 0;
            int stringIndexEnd = 0;
            stringIndexStart = message.indexOf("<span>", stringIndexStart);
            stringIndexEnd = message.indexOf("</span>", stringIndexEnd);
            while (stringIndexStart != -1) {
                messageToHighlight.setSpan(new BackgroundColorSpan(Color.YELLOW), stringIndexStart+6, stringIndexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringIndexStart += 6;
                stringIndexStart = message.indexOf("<span>", stringIndexStart);
                stringIndexEnd += 7;
                stringIndexEnd = message.indexOf("</span>", stringIndexEnd);

            }
            stringIndexStart = 0;
            stringIndexEnd = 0;
            stringIndexStart = messageToHighlight.toString().indexOf("<span>", stringIndexStart);

            while (stringIndexStart != -1){
                messageToHighlight.delete(stringIndexStart, stringIndexStart+6);
                stringIndexStart = 0;
                stringIndexStart = messageToHighlight.toString().indexOf("<span>", stringIndexStart);

            }

            stringIndexEnd = messageToHighlight.toString().indexOf("</span>", stringIndexEnd);
            while (stringIndexEnd != -1){
                messageToHighlight.delete(stringIndexEnd, stringIndexEnd+7);
                stringIndexEnd = 0;
                stringIndexEnd = messageToHighlight.toString().indexOf("</span>", stringIndexEnd);
            }
            holder.rowTextView.setText(messageToHighlight);
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
    /*The StartWatson AsyncTask is similar to the later MessageWatson, albeit
    *much more trimmed. They both make network calls and pass specific data to the middleware
    *However, before communicating with Watson Assistant a session has to be started
    *The StartWatson's main task is to cleanly start the session with the chatbot before
    the user will even notice, able to start chatting once the activity loads.*/
    private class StartWatson extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        Context context;

        StartWatson(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute(){

        }
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            //Retrieve the chatMessageLog Arraylist and set destination URL
            final ArrayList<String> currentChatLog = params[0];
            String responsePayloadString = "";
            String urlString = "https://capstone-middleware-2019.herokuapp.com/startConversation";
            JSONObject json = new JSONObject();
            //If this activity starts from the Main Acitivty, presume fresh conversation.
            //Otherwise, use the chatbot's other entry point to continue the specific question cycle
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
            //Send request to middleware with the populated json payload
            MiddlewareConnector middlewareConnection = new MiddlewareConnector(urlString, json.toString());
            responsePayloadString = middlewareConnection.connect();
            //If the response is from the 'alreadyHaveDocumentId' path,
            //Then update the response here. This data cannot be passed to the
            //Watson Assistant, so the topic report's filename is instead presented here.
            if (responsePayloadString.equals("How may I help you with the chosen report?")){
                responsePayloadString = "How many I help you with report " + documentFilename + "?";
            }

            currentChatLog.add(responsePayloadString);

            return currentChatLog;
        }
        protected void onPostExecute(ArrayList<String> currentChatLog) {
            //Update recyclerview
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

        }
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            //Unpack the chatMessageLog Arraylist so it can be updated later
            final ArrayList<String> currentChatLog = params[0];
            String responsePayloadString = "";
            String enteredMessage = editMessage.getText().toString();
            JSONObject json = new JSONObject();
            //Attach the user's message to the request payload's JSON object
            //for the middleware to receive
            try {
                json.put("message", enteredMessage);
            }catch (JSONException e){
                throw new RuntimeException(e);
            }
            //Send request to the middleware and receive response as a string.
            String urlString = "https://capstone-middleware-2019.herokuapp.com/continueConversation";
            MiddlewareConnector middlewareConnection = new MiddlewareConnector(urlString, json.toString());
            responsePayloadString = middlewareConnection.connect();

            //Handling different responses from chatbot received from the middleware

            //When the chatbot has identified that the user needs to make
            //a general query to Discovery, prepare intent to redirect user
            if (responsePayloadString.equals("generalDiscoveryQuery")) {
                Bundle generalBundle = new Bundle();
                generalBundle.putString("query", enteredMessage);
                Intent i = new Intent(Conversation.this, GeneralQueryResult.class);
                i.putExtras(generalBundle);
                startActivity(i);
            }
            else if (responsePayloadString.equals("Sending you back to the homepage...")){
                Intent i = new Intent(Conversation.this, MainActivity.class);
                startActivity(i);
            }
            //When the chatbot realizes it needs to search for a report...
            else if (responsePayloadString.contains("Give me a moment to find that report.")){
                //Unpack the response payload
                String [] splitString = responsePayloadString.split(";uniqueDelimiter;");
                currentChatLog.add(enteredMessage);
                currentChatLog.add(splitString[0]);
                documentFilename = splitString[1];
                //Attach the desired filename to the next request
                try {
                    json.put("filename", splitString[1]);
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }
                //Send request to the 'getDocumentId' function, which will return a document ID
                //based on the filename.
                urlString = "https://capstone-middleware-2019.herokuapp.com/getDocumentId";
                middlewareConnection = new MiddlewareConnector(urlString, json.toString());
                responsePayloadString = middlewareConnection.connect();
                splitString = responsePayloadString.split(";uniqueDelimiter;");
                //If the report is found, unpack received document id
                if (splitString[0].equals("reportFound")){
                    documentFileId = splitString[1];
                }
                try {
                    json.put("message", splitString[0]);
                    json.remove("filename");
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }
                //Send the Watson Assistant the results, and it will respond with the
                //Appropriate answer if the document was successfully found,
                //Or no document of that name could be found. The Watson assistant must be informed
                //so the user's place in the conversation is not lost.
                urlString = "https://capstone-middleware-2019.herokuapp.com/continueConversation";
                middlewareConnection = new MiddlewareConnector(urlString, json.toString());
                responsePayloadString = middlewareConnection.connect();
                currentChatLog.add(responsePayloadString);
            }
            //When the Watson Assistant determines that the user is ready to make
            //a query about a specific report...
            else if(responsePayloadString.equals("specificDiscoveryQuery")){
                //Send document's ID to the middleware
                try {
                    json.put("documentId", documentFileId);
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }

                urlString = "https://capstone-middleware-2019.herokuapp.com/specificDiscoveryQuery";
                middlewareConnection = new MiddlewareConnector(urlString, json.toString());
                responsePayloadString = middlewareConnection.connect();

                //Unpack the results of the user's query
                JSONObject passagesObject;
                JSONArray payloadArray;
                JSONArray passagesArray;
                JSONArray highlightedTermsArray;
                try {
                    json.put("message", "discoveryCycle");
                    payloadArray = new JSONArray(responsePayloadString);
                    passagesArray = payloadArray.getJSONArray(0);
                    highlightedTermsArray = payloadArray.getJSONArray(1);
                    passageCollection = new ArrayList<PassageDetails>();
                    for (int i = 0; i < passagesArray.length(); i++){
                        passagesObject = passagesArray.getJSONObject(i);
                        passageCollection.add(new PassageDetails(passagesObject.getString("passage_score"), passagesObject.getString("passage_text")));
                    }
                }catch (JSONException e){
                    throw new RuntimeException(e);
                }
                //Print the passage results to the chat window
                currentChatLog.add(enteredMessage);
                String htmlFormattedResult;
                System.out.println(highlightedTermsArray);
                String presentPassageResults = "The Top Matching Results:\n";
                for (int i = 0; i < passageCollection.size(); i++){
                    htmlFormattedResult = passageCollection.get(i).getPassageText();
                    for (int j = 0; j < highlightedTermsArray.length(); j++){
                        try{
                            htmlFormattedResult = htmlFormattedResult.replaceAll("(?i)"+ Pattern.quote(highlightedTermsArray.get(j).toString()), "<span>" + highlightedTermsArray.get(j).toString() + "</span>");
                        }catch (JSONException e){
                            throw new RuntimeException(e);
                        }
                    }
                    presentPassageResults += "Result " + (i+1) + ":\n" + htmlFormattedResult;
                    if (passageCollection.size() - 1 != i){
                        presentPassageResults += "\n";
                    }
                }
                currentChatLog.add(presentPassageResults);
                //Notify the Watson Assistant that the user has been given the related passages
                //and to continue the conversation
                urlString = "https://capstone-middleware-2019.herokuapp.com/continueConversation";
                middlewareConnection = new MiddlewareConnector(urlString, json.toString());
                responsePayloadString = middlewareConnection.connect();
                currentChatLog.add(responsePayloadString);
            }
            //When the Watson Assistant does not need to call Discovery, continue the conversation.
            else{
                currentChatLog.add(enteredMessage);
                currentChatLog.add(responsePayloadString);
            }

            return currentChatLog;
        }
        protected void onPostExecute(ArrayList<String> currentChatLog) {
            //Update the recycleview, reset the editMessage view
            updateChatbox(context, currentChatLog);
            editMessage.setText("");
        }

    }
}
