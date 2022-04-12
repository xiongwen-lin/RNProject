package com.afar.osaio.widget.bean;

public class GifBean {

    private int raw;
    private float speed;
    private int count;

    public GifBean(int raw, float speed, int count) {
        this.raw = raw;
        this.speed = speed;
        this.count = count;
    }

    public int getRaw() {
        return raw;
    }

    public void setRaw(int raw) {
        this.raw = raw;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
