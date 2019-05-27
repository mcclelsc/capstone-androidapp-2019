package com.mcclelland.scott.derailmentreportchatbotservice;

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

public class MiddlewareConnector {
    String urlString = "";
    String json = "";
    String responsePayloadString = "";
    public MiddlewareConnector(String urlString, String json){
        this.urlString = urlString;
        this.json = json;
    }
    public String connect(){
        try {
            //Instantiate connection to node js middleware server
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //Declare request method to be of type 'POST'
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            //Instantiate outputstream and buffered writer objects to write content to the POST request
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
            writer.write(json);
            writer.flush();
            writer.close();

            //After writing is complete, connect and send request to server.
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            //If the network call was successful, build the response into a single string.
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
            //Otherwise, report error code
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
}
