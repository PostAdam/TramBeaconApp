package com.pp.beacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pp.model.BeaconData;

import java.util.ArrayList;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<BeaconData> items;

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(context)
                    .inflate(R.layout.beacon_data, parent, false);
        }

        BeaconData currentItem = (BeaconData) getItem(position);

        TextView beaconId = convertView.findViewById(R.id.beaconId);
        TextView beaconProximity = convertView.findViewById(R.id.beaconProximity);

        beaconId.setText(currentItem.getBeaconId());
        beaconProximity.setText(currentItem.getBeaconProximity());

        return convertView;
    }
}