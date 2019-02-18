package com.gdou.security.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LocationResult {

    public int state;

    public String msg;

    @SerializedName("data")
    public List<Data> dataList;

    public class Data {
        // 经度
        public String x;
        // 纬度
        public String y;
    }
}
