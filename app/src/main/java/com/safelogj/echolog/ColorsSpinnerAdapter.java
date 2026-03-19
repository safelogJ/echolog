package com.safelogj.echolog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ColorsSpinnerAdapter extends ArrayAdapter<ColorsPalette> {

    public ColorsSpinnerAdapter(Context context, ColorsPalette[] colorEnums) {
        super(context, R.layout.spinner_item, colorEnums);
        setDropDownViewResource(R.layout.spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view;
        ColorsPalette color = getItem(position);
        if (color != null) {
            textView.setBackgroundColor(color.getColor());
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        ColorsPalette color = getItem(position);
        if (color != null) {
            textView.setBackgroundColor(color.getColor());
        }
        return view;
    }
}
