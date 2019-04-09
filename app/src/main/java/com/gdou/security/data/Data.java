package com.gdou.security.data;

public class Data {
    // 经度
    public String x;
    // 纬度
    public String y;

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Data{" +
                "x='" + x + '\'' +
                ", y='" + y + '\'' +
                '}';
    }
}