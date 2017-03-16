package com.fadh.fluxtest.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fadh.fluxtest.Model.Data;
import com.fadh.fluxtest.R;

import java.util.ArrayList;

/**
 * Created by fadhlika on 3/14/2017.
 */

public class DataAdapter extends ArrayAdapter<Data> {
    public DataAdapter(Context context, ArrayList<Data> datas) {
        super(context, 0, datas);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Data data = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_data_list, parent, false);
        }

        TextView tvLevel = (TextView) convertView.findViewById(R.id.level_textview);
        TextView tvHeight = (TextView) convertView.findViewById(R.id.height_textview);
        TextView tvInterval = (TextView) convertView.findViewById(R.id.interval_textview);

        tvLevel.setText("" + data.level);
        tvHeight.setText("" + data.height);
        tvInterval.setText(String.format("%.2f", data.interval));

        return convertView;
    }
}
