package com.hoohacks.widgetserverapp.data;

public class WebsiteData {

    double Acc, Flex,RE;
    long Activity;
    String Timestamp;

    public WebsiteData(double acc, double flex, long activity, double RE, String timestamp) {
        Acc = acc;
        Flex = flex;
        Activity = activity;
        this.RE = RE;
        Timestamp = timestamp;
    }

    public double getAcc() {
        return Acc;
    }

    public void setAcc(double acc) {
        Acc = acc;
    }

    public double getFlex() {
        return Flex;
    }

    public void setFlex(double flex) {
        Flex = flex;
    }

    public long getActivity() {
        return Activity;
    }

    public void setActivity(long activity) {
        Activity = activity;
    }

    public double getRE() {
        return RE;
    }

    public void setRE(double RE) {
        this.RE = RE;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }
}
