package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class UploadDocument extends AppCompatActivity {

    DrawerLayout globalNavDrawerLayout;

    String adminEmail = "ghassem.tofighi@sheridancollege.ca";

    Uri filepath;
    Button btnBrowseFile;
    Button btnUploadDocument;
    TextView txtFilepath;
    EditText editReportDetails;

    //String middlewareURL = "https://capstone-middleware-2019.herokuapp.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);

        globalNavDrawerLayout = findViewById(R.id.drawer_layout_uploadDocument);

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
                        Intent intent = new Intent(UploadDocument.this, Conversation.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.generalQueryNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.START);
                        globalNavImage.setImageResource(R.drawable.menu_icon);
                        Intent intent = new Intent(UploadDocument.this, GeneralQuery.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.uploadDocumentNavItem: {
                        globalNavDrawerLayout.closeDrawer(GravityCompat.START);
                        globalNavImage.setImageResource(R.drawable.menu_icon);
                        Intent intent = new Intent(UploadDocument.this, UploadDocument.class);
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

        btnBrowseFile = findViewById(R.id.btnBrowseFile);
        btnUploadDocument = findViewById(R.id.btnUploadDocument);
        txtFilepath = findViewById(R.id.txtFilepath);
        editReportDetails = findViewById(R.id.editReportDetails);

        final Context context = this;

        btnBrowseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
            }
        });

        btnUploadDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{adminEmail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Missing Report");
                if (txtFilepath.getText().toString().equals("No File Selected")){
                    intent.putExtra(Intent.EXTRA_TEXT   , "I believe a report involving these details:\n\n" + editReportDetails.getText().toString() + "\n\nis missing from the collection of reports.");
                }
                else{
                    intent.putExtra(Intent.EXTRA_TEXT   , "I believe report " + txtFilepath.getText().toString() + "is missing from the collection of reports.");
                }
                try {
                    startActivity(Intent.createChooser(intent, "Notify Admin"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(UploadDocument.this, "No Email Clients could be found.\nPlease install an email client and try again.", Toast.LENGTH_LONG).show();
                }

                /*if (filepath == null){

                }
                else{
                    btnBrowseFile.setEnabled(false);
                    btnUploadDocument.setEnabled(false);
                    new SendDocument(context, filepath).execute();
                }*/
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                filepath = result.getData();
                String stringFilepath = filepath.toString();
                File file = new File(stringFilepath);
                String tempPath = file.getAbsolutePath();
                String fileDisplayName = "";

                if (stringFilepath.startsWith("content://")){
                    Cursor cursor = null;
                    try {
                        cursor = this.getContentResolver().query(filepath, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()){
                            fileDisplayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    }finally{
                        cursor.close();
                    }
                }
                else if (stringFilepath.startsWith("file://")){
                    fileDisplayName = file.getName();
                }
                txtFilepath.setText(fileDisplayName);
            }
        }
    }

    /*private class SendDocument extends AsyncTask<Void, Void, Void>{
        private Context context;
        private Uri filePath;
        public SendDocument(Context context, Uri filePath){
            this.context = context;
            this.filePath = filePath;
        }
        @Override
        protected void onPreExecute(){

        }

        protected Void doInBackground(Void... params){
            String urlString = middlewareURL + "/uploadDocument";
            MiddlewareConnector middlewareConnector = new MiddlewareConnector(urlString);
            String responsePayload = middlewareConnector.sendDocument(context, filePath);
            return null;
        }

        protected void onPostExecute(){
            btnBrowseFile.setEnabled(true);
            btnUploadDocument.setEnabled(true);
        }
    }*/
}
