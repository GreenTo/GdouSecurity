package com.gdou.security.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LocationResult {

    public int state;

    public String msg;

    @SerializedName("data")
    public List<Data> dataList = new ArrayList<>();

}
