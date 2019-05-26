package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GeneralQuery extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_query);

        Button btnSubmit = (Button)findViewById(R.id.btnQuerySubmit);
        final EditText editGeneralQuery = (EditText)findViewById(R.id.editGeneralQuery);

        btnSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendToGeneralQueryResult(editGeneralQuery);
            }
        });
    }

    private void sendToGeneralQueryResult(EditText editText){
        if (editText.toString().equals("") || editText.toString().equals(" ")){

        }
        else{
            Bundle generalBundle = new Bundle();
            generalBundle.putString("query", editText.toString());
            Intent i = new Intent(GeneralQuery.this, GeneralQueryResult.class);
            i.putExtras(generalBundle);
            startActivity(i);
        }

    }
}
