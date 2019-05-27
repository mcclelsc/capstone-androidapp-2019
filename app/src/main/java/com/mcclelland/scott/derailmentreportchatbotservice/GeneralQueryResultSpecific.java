package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GeneralQueryResultSpecific extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_query_result_specific);
        final DocumentDetails chosenDocument = (DocumentDetails) getIntent().getSerializableExtra("documentDetails");

        TextView txtFilename = findViewById(R.id.txtFilename);
        TextView txtPassageCount = findViewById(R.id.txtPassageCount);
        TextView txtText = findViewById(R.id.txtText);

        Button btnStartNewConversation = findViewById(R.id.btnStartNewConversation);

        txtFilename.setText(chosenDocument.getFilename());
        txtPassageCount.setText(String.valueOf(chosenDocument.getPassageCount()));
        txtText.setText(chosenDocument.getText());

        btnStartNewConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpecificConversation(chosenDocument.getId(), chosenDocument.getFilename());
            }
        });
    }

    private void startSpecificConversation(String documentId, String documentFilename){
        Intent intent = new Intent(GeneralQueryResultSpecific.this, GeneralQueryResultSpecific.class);
        intent.putExtra("documentId", documentId);
        intent.putExtra("documentFilename", documentFilename);
        startActivity(intent);
    }
}
