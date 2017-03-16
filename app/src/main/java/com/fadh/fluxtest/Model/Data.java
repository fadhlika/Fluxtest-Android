package com.fadh.fluxtest.Model;

/**
 * Created by fadhlika on 3/10/2017.
 */

public class Data {
    public int level;
    public double height;
    public double interval;

    public Data(double i, int l) {
        level = l;
        height = (2 - (l * 0.05));
        interval = i;
    }
}
