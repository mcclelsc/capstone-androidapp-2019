package com.mcclelland.scott.derailmentreportchatbotservice;

import java.io.Serializable;

public class DocumentDetails implements Serializable {
    String id;
    String filename;
    String text;
    int passageCount;

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
