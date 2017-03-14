package com.fadh.fluxtest.Model;

/**
 * Created by fadhlika on 3/10/2017.
 */

public class Data {
    int level;
    float height;
    float interval;

    public Data(int l, float i) {
        level = l;
        height = (float) (2 - (l * 0.05));
        interval = i;
    }

    public int getLevel() {
        return level;
    }

    public float getHeight() {
        return height;
    }

    public float getInterval() {
        return interval;
    }
}
