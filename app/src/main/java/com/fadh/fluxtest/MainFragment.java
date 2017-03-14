package com.fadh.fluxtest;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.ref.WeakReference;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private static String data = "";
    private final String ACTION_USB_PERMISSION = "com.fadh.fluxtest.USB_PERMISSION";
    private LineGraphSeries<DataPoint> mSeries1;
    private SerialPort serialPort;
    private SerialHandler handler;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        handler = new SerialHandler(this);
        serialPort = new SerialPort(getContext(), handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        GraphView graph = (GraphView) view.findViewById(R.id.line_chart);

        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);

        return view;
    }

    public void connectSerial() {
        if (serialPort != null) {
            serialPort.getPort();
        } else {
            Log.d(TAG, "No serial port instance");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void insertData(String d) {
        try {
            //Data newData = new Data(Integer.parseInt(d[0]), Integer.parseInt(d[1]));
            String[] v = d.split(",");
            if (v.length > 1) {
                long x = Long.parseLong(v[0]);
                double y = Double.parseDouble(v[1]);
                mSeries1.appendData(new DataPoint(x, y), true, 40);
                Log.d("insertData", x + " " + y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect_action:
                if (!serialPort.serialConnected) {
                    connectSerial();
                    item.setTitle(R.string.disconnect);
                } else {
                    item.setTitle(R.string.connect);
                    serialPort.closeSerial();
                }
                break;
            case R.id.reset_action:
                //usbService.reset();
                break;
            case R.id.setting_action:
                new MaterialDialog.Builder(getContext())
                        .title(R.string.setting)
                        .customView(R.layout.setting_view, true)
                        .positiveText(R.string.Ok)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                EditText baudText = (EditText) dialog.getCustomView().findViewById(R.id.baud_rate_label);
                                serialPort.changeBaud(Integer.parseInt(baudText.getText().toString()));
                            }
                        })
                        .show();
            default:
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serialPort.destroyReceiver();
    }

    private static class SerialHandler extends Handler {
        private final WeakReference<MainFragment> mFragment;

        public SerialHandler(MainFragment fragment) {
            mFragment = new WeakReference<MainFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            byte[] bytes = (byte[]) msg.obj;
            String d = new String(bytes);
            data += d;
            Log.d("handeMessage", data);
            try {
                String[] splitter = data.split("\n");
                String s = splitter[splitter.length - 1];
                if (s.contains(",") && s.contains("\r")) {
                    Log.d("handleMessage", s);
                    mFragment.get().insertData(s.trim());
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }
    }

}
