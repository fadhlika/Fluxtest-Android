package com.fadh.fluxtest;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fadh.fluxtest.Adapter.DataAdapter;
import com.fadh.fluxtest.Model.Data;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private final String ACTION_USB_PERMISSION = "com.fadh.fluxtest.USB_PERMISSION";
    DataAdapter adapter;
    double x = 0.0;
    BluetoothSPP bt;
    MenuItem item;
    private LineGraphSeries<DataPoint> mSeries1;
    private ArrayList<Data> dataList;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);


        bt = new BluetoothSPP(getContext());
        bt.setupService();
        bt.startService(BluetoothState.DEVICE_OTHER);

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                insertData(message);
                Log.d("onDataReceived", new String(data) + "," + message);
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {

            @Override
            public void onDeviceConnected(String name, String address) {
                Log.d("onDeviceConnected", name);
                Toast.makeText(getActivity().getApplicationContext(),
                        bt.getConnectedDeviceName() + " connected", Toast.LENGTH_SHORT).show();
                item.setTitle(R.string.disconnect);
            }

            @Override
            public void onDeviceDisconnected() {
                Log.d("onDeviceConnected", "disconnected");
                Toast.makeText(getContext(), "Device disconnected", Toast.LENGTH_SHORT).show();
                item.setTitle(R.string.connect);
            }

            @Override
            public void onDeviceConnectionFailed() {
                Log.d("onDeviceConnectedFailed", "failed");
            }
        });


        GraphView graph = (GraphView) view.findViewById(R.id.line_chart);

        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(50);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0.0);
        graph.getViewport().setMaxY(2.0);

        dataList = new ArrayList<>();
        adapter = new DataAdapter(getContext(), dataList);

        ListView dataListView = (ListView) view.findViewById(R.id.list_data);
        dataListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        item = menu.getItem(1);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        bt.disconnect();
        Log.d("onDestroy", "Destroy");
        super.onDestroy();
    }

    public void insertData(String d) {
        try {
            String[] v = d.split(";");
            if (v.length > 2) {
                int y = Integer.parseInt(v[0]);
                int ovf = Integer.parseInt(v[1]);
                long counter = Long.parseLong(v[2]);
                x += ((65535 * ovf) + counter) * 0.000064;
                Data newData = new Data(x, y);
                adapter.add(newData);
                mSeries1.appendData(new DataPoint(newData.interval, newData.height), false, 20);
                Log.d("insertData", x + " " + y);
            }
        } catch (Exception e) {
            Log.d("InsertData Exception", e.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect_action:
                if (!bt.isBluetoothEnabled()) {
                    new MaterialDialog.Builder(getContext())
                            .content("Please enable bluetooth")
                            .show();
                } else if (bt.isBluetoothEnabled()) {
                    if (bt.getConnectedDeviceName() == null) {
                        Intent intent = new Intent(getContext(), DeviceList.class);
                        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                    } else {
                        bt.disconnect();
                    }
                }
            case R.id.reset_action:
                mSeries1.resetData(new DataPoint[]{});
                adapter.clear();
                break;
            case R.id.setting_action:
            default:
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    bt.connect(data);
                } catch (Exception e) {
                    Log.d("Connect", e.toString());
                }
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                Log.d("Connecet", "setup");
            }
        }

    }
}
