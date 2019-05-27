package com.mcclelland.scott.derailmentreportchatbotservice;

import java.io.Serializable;

public class DocumentDetails implements Serializable {
    //Watson Discovery ID
    private String id;
    //Filename of the document uploaded to Discovery
    private String filename;
    //Sample text the Discovery api returns through its json payload
    private String text;
    //How many passages are associated with this document
    private int passageCount;

    public DocumentDetails(String id, String filename, String text){
        this.id = id;
        this.filename = filename;
        this.text = text;
    }

    public void setPassageCount(int passageCount){
        this.passageCount = passageCount;
    }

    public String getId(){
        return id;
    }
    public String getFilename(){
        return filename;
    }
    public String getText(){
        return text;
    }
    public int getPassageCount(){
        return passageCount;
    }
}
