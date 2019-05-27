package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartChat = (Button)findViewById(R.id.startChatButton);
        Button btnQuery = (Button)findViewById(R.id.queryButton);

        btnStartChat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendToConversation();
            }
        });

        btnQuery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendToGeneralQuery();
            }
        });
    }
    //Send user directly to conversation
    private void sendToConversation(){
        Intent intent = new Intent(MainActivity.this, Conversation.class);
        startActivity(intent);
    }
    //Send user to the general query activity
    private void sendToGeneralQuery(){
        Intent intent = new Intent(MainActivity.this, GeneralQuery.class);
        startActivity(intent);
    }

}
