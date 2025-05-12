package com.attendance.model;


public class Employee {
    private int srl;
    private String payCode;
    private String cardNo;
    private String employeeName;
    private String inTime;
    private String outTime;
    private String status;
    private int ot;
    private double duration;
    private String dayShift;
    private String nightShift;

    // Getters and Setters
    public int getSrl() { return srl; }
    public void setSrl(int srl) { this.srl = srl; }
    public String getPayCode() { return payCode; }
    public void setPayCode(String payCode) { this.payCode = payCode; }
    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getInTime() { return inTime; }
    public void setInTime(String inTime) { this.inTime = inTime; }
    public String getOutTime() { return outTime; }
    public void setOutTime(String outTime) { this.outTime = outTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getOt() { return ot; }
    public void setOt(int ot) { this.ot = ot; }
    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }
    public String getDayShift() { return dayShift; }
    public void setDayShift(String dayShift) { this.dayShift = dayShift; }
    public String getNightShift() { return nightShift; }
    public void setNightShift(String nightShift) { this.nightShift = nightShift; }
}