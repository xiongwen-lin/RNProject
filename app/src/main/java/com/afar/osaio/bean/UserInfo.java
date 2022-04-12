package com.afar.osaio.bean;

public class UserInfo {

    private String level;
    private String account;
    private String nickname;
    private String photo;
    private String country;
    private int isDebug;
    private int twoAuth;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setIsdebug(int isDebug) {
        this.isDebug = isDebug;
    }

    public int getIsdebug() {
        return isDebug;
    }

    public int getTwoAuth() {
        return twoAuth;
    }

    public void setTwoAuth(int twoAuth) {
        this.twoAuth = twoAuth;
    }
}
