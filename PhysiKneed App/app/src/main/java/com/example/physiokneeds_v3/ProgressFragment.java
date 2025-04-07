package com.example.physiokneeds_v3;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


public class ProgressFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        BarChart barChart = view.findViewById(R.id.barChart);

        // Data points for scores
        // TODO get from server
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 100)); // Sunday
        entries.add(new BarEntry(1, 50));  // Monday
        entries.add(new BarEntry(2, 50));  // Tuesday
        entries.add(new BarEntry(3, 80)); // Wednesday
        entries.add(new BarEntry(4, 90)); // Thursday
        entries.add(new BarEntry(5, 50)); // Friday
        entries.add(new BarEntry(6, 10)); // Saturday

        BarDataSet dataSet = new BarDataSet(entries, "Score");
        dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.pink));
        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.animateY(500);

        // Customize X-axis labels
        String[] labels = {"S", "M", "T", "W", "T", "F", "S"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        // Set Y-axis range
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(100f);
        rightAxis.setLabelCount(5, true);
        rightAxis.setTextSize(16);
        barChart.getAxisLeft().setEnabled(false); // Disable right Y-axis

        barChart.getDescription().setEnabled(false); // Disable chart description
        barChart.getLegend().setEnabled(false);  // Disables the legend completely
        barChart.getXAxis().setDrawGridLines(false);  // Remove vertical grid lines (on X-axis)

        barChart.getXAxis().setTextSize(16);

        dataSet.setDrawValues(false);  // Disable the data values (labels) on top of the bars
        barChart.getBarData().setBarWidth(0.4f);

        barChart.invalidate(); // Refresh chart

        // Inflate the layout for this fragment
        return view;
    }
}