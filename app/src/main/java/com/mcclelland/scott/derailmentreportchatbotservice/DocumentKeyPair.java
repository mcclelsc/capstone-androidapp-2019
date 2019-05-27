package com.mcclelland.scott.derailmentreportchatbotservice;

public class DocumentKeyPair {
    private int viewId;
    private int documentListId;
    public DocumentKeyPair(int viewId, int documentListId){
        this.viewId = viewId;
        this.documentListId = documentListId;
    }

    public int getViewId(){
        return viewId;
    }
    public int getDocumentListId(){
        return documentListId;
    }
}
