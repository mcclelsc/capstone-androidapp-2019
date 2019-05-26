package com.mcclelland.scott.derailmentreportchatbotservice;

import android.app.ProgressDialog;
import android.content.Context;
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

import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.cloud.sdk.core.service.exception.RequestTooLargeException;
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException;
import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageContext;
import com.ibm.watson.assistant.v2.model.MessageContextGlobal;
import com.ibm.watson.assistant.v2.model.MessageContextGlobalSystem;
import com.ibm.watson.assistant.v2.model.MessageContextSkills;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Conversation extends AppCompatActivity {

    private ProgressDialog progress;
    RecyclerViewAdapter recyclerViewAdapter;
    EditText editMessage;
    RecyclerView recyclerView;
    ArrayList<String> chatMessageLog;
    Assistant chatAssistant;
    String chatSessionId;

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

        new StartWatson().execute();

    }

    private void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
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

    private class StartWatson extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute(){
            //progress = ProgressDialog.show(Conversation.this, "Starting Conversation...", "Please Wait");
        }
        protected String doInBackground(String... params) {
            String responsePayloadString = "Failure";

            try {
                //Instantiate connection to node js middleware server
                URL url = new URL("https://capstone-middleware-capstone-middleware.1d35.starter-us-east-1.openshiftapps.com/startConversation");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //urlConnection.setRequestProperty("Content-Type", "application/json");
                //urlConnection.setRequestProperty("Accept", "application/json");

                //Declare request method to be of type 'POST'
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                //Instantiate outputstream and buffered writer objects to write content to the POST request
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                writer.write("Send Payload");
                writer.flush();
                writer.close();

                //After writing is complete, connect and send request to server.
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                //System.out.println(responseCode);
                if (responseCode == 200){
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                    StringBuilder stringBuilder = new StringBuilder();
                    responsePayloadString = "";
                    while ((responsePayloadString = reader.readLine()) != null){
                        stringBuilder.append(responsePayloadString);
                    }
                    responsePayloadString = stringBuilder.toString();
                    inputStream.close();
                }
                else{
                    System.out.println(responseCode);
                }
                outputStream.close();
                urlConnection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            return responsePayloadString;
        }
        protected void onPostExecute(String sessionId) {
            //chatSessionId = sessionId;
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
            Boolean secondCall = false;
            JSONObject json = new JSONObject();
            try {
                json.put("message", editMessage.getText().toString());
            }catch (JSONException e){
                throw new RuntimeException(e);
            }
            try {
                //Instantiate connection to node js middleware server
                URL url = new URL("https://capstone-middleware-capstone-middleware.1d35.starter-us-east-1.openshiftapps.com/continueConversation");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //urlConnection.setRequestProperty("Content-Type", "application/json");
                //urlConnection.setRequestProperty("Accept", "application/json");

                //Declare request method to be of type 'POST'
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                //Instantiate outputstream and buffered writer objects to write content to the POST request
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                writer.write(json.toString());
                writer.flush();
                writer.close();

                //After writing is complete, connect and send request to server.
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                System.out.println(responseCode);
                if (responseCode == 200){
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                    StringBuilder stringBuilder = new StringBuilder();
                    responsePayloadString = "";
                    while ((responsePayloadString = reader.readLine()) != null){
                        stringBuilder.append(responsePayloadString);
                    }
                    responsePayloadString = stringBuilder.toString();
                    inputStream.close();
                    if (responsePayloadString.equals("queryDiscovery")){
                        secondCall = true;
                    }
                    else {
                        currentChatLog.add(editMessage.getText().toString());
                        currentChatLog.add(responsePayloadString);
                    }
                }
                else{
                    System.out.println(responseCode);
                }

                outputStream.close();
                urlConnection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            if (secondCall){
                try {
                    //Instantiate connection to node js middleware server
                    URL url = new URL("https://capstone-middleware-capstone-middleware.1d35.starter-us-east-1.openshiftapps.com/queryDiscovery");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //urlConnection.setRequestProperty("Content-Type", "application/json");
                    //urlConnection.setRequestProperty("Accept", "application/json");

                    //Declare request method to be of type 'POST'
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    //Instantiate outputstream and buffered writer objects to write content to the POST request
                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                    writer.write(json.toString());
                    writer.flush();
                    writer.close();

                    //After writing is complete, connect and send request to server.
                    urlConnection.connect();

                    int responseCode = urlConnection.getResponseCode();
                    System.out.println(responseCode);
                    if (responseCode == 200){
                        InputStream inputStream = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                        StringBuilder stringBuilder = new StringBuilder();
                        responsePayloadString = "";
                        while ((responsePayloadString = reader.readLine()) != null){
                            stringBuilder.append(responsePayloadString);
                        }
                        responsePayloadString = stringBuilder.toString();
                        inputStream.close();

                        currentChatLog.add(editMessage.getText().toString());
                        currentChatLog.add(responsePayloadString);
                    }
                    else{
                        System.out.println(responseCode);
                    }

                    outputStream.close();
                    urlConnection.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }


            return currentChatLog;
        }
        protected void onPostExecute(ArrayList<String> currentChatLog) {
            chatMessageLog = currentChatLog;
            recyclerViewAdapter = new RecyclerViewAdapter(context, chatMessageLog);
            //recyclerViewAdapter.setClickListener(this);
            recyclerView.setAdapter(recyclerViewAdapter);
        }

    }
}
