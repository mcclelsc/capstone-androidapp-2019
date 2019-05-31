package com.mcclelland.scott.derailmentreportchatbotservice;

public class ChatMessageRowDetails {
    private int position;
    private int alignment;

    public ChatMessageRowDetails(int position, int alignment){
        this.position = position;
        this.alignment = alignment;
    }

    public int getPosition(){
        return position;
    }
    public int getAlignment(){
        return alignment;
    }
}
