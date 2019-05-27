package com.mcclelland.scott.derailmentreportchatbotservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class GeneralQuery extends AppCompatActivity {
    private Boolean realUserSelect = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_query);

        Button btnSubmit = (Button)findViewById(R.id.btnQuerySubmit);
        final EditText editGeneralQuery = (EditText)findViewById(R.id.editGeneralQuery);

        final Spinner spinnerSuggestedQueries = (Spinner)findViewById(R.id.spinnerSuggestedQueries);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.suggested_questions, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSuggestedQueries.setAdapter(spinnerAdapter);

        spinnerSuggestedQueries.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                realUserSelect = true;
                return false;
            }
        });

        spinnerSuggestedQueries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (realUserSelect){
                    if (i == 0){
                        editGeneralQuery.setText("");
                    }
                    else{
                        editGeneralQuery.setText(spinnerSuggestedQueries.getSelectedItem().toString());
                    }
                }
                realUserSelect = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendToGeneralQueryResult(editGeneralQuery);
            }
        });

    }

    private void sendToGeneralQueryResult(EditText editText){
        if (editText.getText().toString().equals("") || editText.getText().toString().trim().length() == 0){
            Toast.makeText(getApplicationContext(),"Please enter a Query", Toast.LENGTH_SHORT).show();
        }
        else{
            Bundle generalBundle = new Bundle();
            generalBundle.putString("query", editText.getText().toString());
            Intent i = new Intent(GeneralQuery.this, GeneralQueryResult.class);
            i.putExtras(generalBundle);
            startActivity(i);
        }

    }

}
