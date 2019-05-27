package com.mcclelland.scott.derailmentreportchatbotservice;

public class PassageDetails {
    String passageScore;
    String passageText;
    public PassageDetails(String passageScore, String passageText){
        this.passageScore = passageScore;
        this.passageText = passageText;
    }
    public String getPassageScore(){
        return passageScore;
    }
    public String getPassageText(){
        return passageText;
    }
}
