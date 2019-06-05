package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    DrawerLayout globalNavDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globalNavDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.navigation_menu);
        final ImageButton globalNavImage = findViewById(R.id.btnGlobalNav);

        final Button btnStartChat = findViewById(R.id.btnStartChatButtonMain);
        Button btnQuery = findViewById(R.id.btnGeneralQueryMain);
        Button btnUploadDocument = findViewById(R.id.btnUploadDocumentMain);

        globalNavDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                btnStartChat.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                globalNavImage.setVisibility(View.GONE);

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                globalNavImage.setVisibility(View.VISIBLE);
                btnStartChat.setVisibility(View.VISIBLE);
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
                        Intent intent = new Intent(MainActivity.this, Conversation.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.generalQueryNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.END);
                        globalNavImage.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(MainActivity.this, GeneralQuery.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.uploadDocumentNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.END);
                        globalNavImage.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(MainActivity.this, UploadDocument.class);
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
                globalNavImage.setVisibility(View.GONE);
                globalNavDrawerLayout.openDrawer(GravityCompat.END);
            }
        });

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

        btnUploadDocument.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendToUploadDocument();
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
    //Send user to the general query activity
    private void sendToUploadDocument(){
        Intent intent = new Intent(MainActivity.this, UploadDocument.class);
        startActivity(intent);
    }

}
