package com.hoohacks.widgetserverapp.data;

public class ExcelData {

    String accX,accY,accZ,accFinal, flex, diffAcc, diffFlex, modFlex, modAcc, activity, re, modRe, diffRe;

    public ExcelData(String accX, String accY, String accZ, String accFinal,String re, String flex, String diffAcc, String diffFlex,String diffRe, String modFlex, String modAcc,String modRe, String activity) {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.accFinal = accFinal;
        this.flex = flex;
        this.diffAcc = diffAcc;
        this.diffFlex = diffFlex;
        this.modFlex = modFlex;
        this.modAcc = modAcc;
        this.activity = activity;
        this.re = re;
        this.modRe = modRe;
        this.diffRe = diffRe;
    }

    public String getRe() {
        return re;
    }

    public String getModRe() {
        return modRe;
    }

    public String getDiffRe() {
        return diffRe;
    }

    public String getAccX() {
        return accX;
    }

    public void setAccX(String accX) {
        this.accX = accX;
    }

    public String getAccY() {
        return accY;
    }

    public void setAccY(String accY) {
        this.accY = accY;
    }

    public String getAccZ() {
        return accZ;
    }

    public void setAccZ(String accZ) {
        this.accZ = accZ;
    }

    public String getAccFinal() {
        return accFinal;
    }

    public void setAccFinal(String accFinal) {
        this.accFinal = accFinal;
    }

    public String getFlex() {
        return flex;
    }

    public void setFlex(String flex) {
        this.flex = flex;
    }

    public String getDiffAcc() {
        return diffAcc;
    }

    public void setDiffAcc(String diffAcc) {
        this.diffAcc = diffAcc;
    }

    public String getDiffFlex() {
        return diffFlex;
    }

    public void setDiffFlex(String diffFlex) {
        this.diffFlex = diffFlex;
    }

    public String getModFlex() {
        return modFlex;
    }

    public void setModFlex(String modFlex) {
        this.modFlex = modFlex;
    }

    public String getModAcc() {
        return modAcc;
    }

    public void setModAcc(String modAcc) {
        this.modAcc = modAcc;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}
