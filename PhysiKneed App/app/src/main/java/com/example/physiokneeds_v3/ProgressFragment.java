package com.example.physiokneeds_v3;

import static com.example.physiokneeds_v3.HomeScreen.routineConfig;
import static com.example.physiokneeds_v3.HomeScreen.routineData;
import static com.example.physiokneeds_v3.HomeScreen.routineDataLoaded;

import static java.lang.Math.round;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ProgressFragment extends Fragment {

    BarChart barChart;
    LinearLayout progressLayout;
    LinearLayout layout;
    ProgressBar loading;
    int currentWeek = 0;
    int maxWeek = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        barChart = view.findViewById(R.id.barChart);
        progressLayout = view.findViewById(R.id.cards_layout);
        layout = view.findViewById(R.id.progress_linear_layout);
        loading = view.findViewById(R.id.loading_progress);

        ImageButton lastWeek = view.findViewById(R.id.back_button);
        ImageButton nextWeek = view.findViewById(R.id.next_button);

        // get current week
        ZonedDateTime now;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = ZonedDateTime.now();
            currentWeek = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            maxWeek = currentWeek;
        }

        lastWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek > 0) {
                    currentWeek--;
                    progressLayout.removeAllViews();
                    doneLoading();
                }
            }
        });

        nextWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek < maxWeek) {
                    currentWeek++;
                    progressLayout.removeAllViews();
                    doneLoading();
                }
            }
        });

        if (routineDataLoaded) {
            doneLoading();
        } else {
            loading.setVisibility(View.VISIBLE);
        }

        // Inflate the layout for this fragment
        return view;
    }

    private void updateGraph(HashMap<Integer, List<RoutineData>> graphData, int week) {

        // calculate score for the days of the week
        HashMap<String, List<Double>> daysScore = new HashMap<>();

        ArrayList<BarEntry> entries = new ArrayList<>();

        if (!graphData.containsKey(week)) {
            entries.add(new BarEntry(0, 0)); // Sunday
            entries.add(new BarEntry(1, 0));  // Monday
            entries.add(new BarEntry(2, 0));  // Tuesday
            entries.add(new BarEntry(3, 0)); // Wednesday
            entries.add(new BarEntry(4, 0)); // Thursday
            entries.add(new BarEntry(5, 0)); // Friday
            entries.add(new BarEntry(6, 0)); // Saturday
        } else {
            for (RoutineData data: graphData.get(week)) {
                DateTimeFormatter formatter;
                ZonedDateTime dateTime;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                    dateTime = ZonedDateTime.parse(data.getCreated_at(), formatter);

                    // calculate score of data
                    Double score = calculateScore(data);
                    daysScore.computeIfAbsent(dateTime.getDayOfWeek().toString(), k -> new ArrayList<>()).add(score);
                }
            }

            List<String> daysOfWeek = Arrays.asList("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY");

            // Data points for scores
            for (int index = 0; index < daysOfWeek.size(); index++) {
                if (daysScore.containsKey(daysOfWeek.get(index))) {
                    Log.d("PROGRESS_DEBUG", daysOfWeek.get(index));
                    Log.d("PROGRESS_DEBUG", String.valueOf(daysScore.get(daysOfWeek.get(index)).stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0)));

                    entries.add(new BarEntry(index, (float) daysScore.get(daysOfWeek.get(index)).stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0)));
                } else {
                    Log.d("PROGRESS_DEBUG", "in the else: " + daysOfWeek.get(index));
                    entries.add(new BarEntry(index, 0));
                }
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Score");
        dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.pink));
        dataSet.setDrawValues(false);  // Disable the data values (labels) on top of the bars

        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.animateY(500);
        barChart.invalidate();
    }

    private void showPastWorkouts(List<RoutineData> workouts) {

        if (workouts == null || workouts.isEmpty()) {
            TextView dateText = new TextView(getContext());
            dateText.setText("No Workouts Completed");
            dateText.setTextSize(16);
            dateText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.source_sans));
            dateText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            dateText.setGravity(Gravity.CENTER);
            progressLayout.addView(dateText);
            return;
        }

        for (RoutineData workout : workouts) {
            FrameLayout frameLayout = new FrameLayout(getContext());
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            frameLayout.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            ZonedDateTime dateTime;
            String formatted;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dateTime = ZonedDateTime.parse(workout.getCreated_at());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, MMM d", Locale.ENGLISH);
                formatted = dateTime.format(formatter);
            } else {
                formatted = "";
            }

            TextView dateText = new TextView(getContext());
            dateText.setLayoutParams(textParams);
            dateText.setText(formatted);
            dateText.setTextSize(16);
            dateText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.source_sans));
            dateText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            dateText.setGravity(Gravity.CENTER);

            ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
            FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(dpToPx(40), dpToPx(40));
            progressParams.gravity = Gravity.END;
            progressBar.setLayoutParams(progressParams);
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            int score = (int) round(calculateScore(workout)*100);
            progressBar.setProgress(score);
            progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circular_progress));

            TextView scoreText = new TextView(getContext());
            FrameLayout.LayoutParams scoreTextParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            scoreTextParams.topMargin = dpToPx(10);
            if (score == 100) {
                scoreTextParams.setMarginEnd(dpToPx(8));
            } else if (score == 0) {
                scoreTextParams.setMarginEnd(dpToPx(16));
            } else {
                scoreTextParams.setMarginEnd(dpToPx(12));
            }
            scoreTextParams.gravity = Gravity.END;

            scoreText.setLayoutParams(scoreTextParams);
            scoreText.setText(String.valueOf(score));
            scoreText.setTextSize(16);
            scoreText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.source_sans), Typeface.BOLD);
            scoreText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            scoreText.setGravity(Gravity.CENTER);

            frameLayout.addView(dateText);
            frameLayout.addView(progressBar);
            frameLayout.addView(scoreText);

            // create cardView for each exercise
            CardView cardView = new CardView(getContext());
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            cardView.setLayoutParams(layoutParams);
            cardView.setCardElevation(dpToPx(4));
            cardView.setRadius(dpToPx(12));
            cardView.setClickable(true);
            cardView.setFocusable(true);
            cardView.setCardBackgroundColor(Color.WHITE);

            cardView.addView(frameLayout);

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), WorkoutSummary.class);
                intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig);
                intent.putExtra("RoutineDataFromProgress", workout);
                intent.putExtra("FromProgress", formatted);

                startActivity(intent);
            });

            progressLayout.addView(cardView);
        }
    }

    public void doneLoading() {

        loading.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);

        // populate the progress screen with the server routineData
        // sort each routineData into individual weeks
        HashMap<Integer, List<RoutineData>> dataWeeksSorted = new HashMap<>();
        for (RoutineData data : routineData) {
            DateTimeFormatter formatter;
            ZonedDateTime dateTime;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                dateTime = ZonedDateTime.parse(data.getCreated_at(), formatter);
                Integer weekNumber = dateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                dataWeeksSorted.computeIfAbsent(weekNumber, k -> new ArrayList<>()).add(data);
                Log.d("ROUTINE_DATA_PROGRESS", dateTime.getDayOfWeek().toString());
            }
        }

        updateGraph(dataWeeksSorted, currentWeek);

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
        rightAxis.setTextSize(14);
        barChart.getAxisLeft().setAxisMinimum(0f);
//        barChart.getAxisLeft().setAxisMinimum(100f);
        barChart.getAxisLeft().setEnabled(false); // Disable right Y-axis
        barChart.setExtraOffsets(10f, 10f, 10f, 20f);

        barChart.getDescription().setEnabled(false); // Disable chart description
        barChart.getLegend().setEnabled(false);  // Disables the legend completely
        barChart.getXAxis().setDrawGridLines(false);  // Remove vertical grid lines (on X-axis)

        barChart.getXAxis().setTextSize(16);

        barChart.getBarData().setBarWidth(0.4f);

        barChart.invalidate(); // Refresh chart

        // show past workouts
        showPastWorkouts(dataWeeksSorted.get(currentWeek));
    }

    private Double calculateScore(RoutineData data) {
        List<Double> exerciseScores = new ArrayList<>();
        double repScore;
        double exerciseScore;

        for(int i = 0; i < data.getRoutineComponentData().size(); i++) {
            exerciseScore = 0;
            for (int j = 0; j < data.getRoutineComponentData().get(i).getRepData().size(); j++) {
                RepData repData = data.getRoutineComponentData().get(i).getRepData().get(j);
                boolean romPenalty = !repData.isGoalExtensionMet() || !repData.isGoalFlexionMet();
                repScore = repData.getScore();

                if (romPenalty) {
                    repScore += 0.2;
                }
                repScore -= repData.getAlerts().size()*0.2;

                exerciseScore += repScore;
            }
            // Convert from total rep scores to average rep score.
            exerciseScore /= data.getRoutineComponentData().get(i).getRepData().size();

            exerciseScores.add(exerciseScore);
        }

        // calculate overall score
        double overallSum = 0;
        for (double score : exerciseScores) {
            overallSum += score;
        }

        double overallScore = overallSum / exerciseScores.size();

        if (overallScore < 0) {
            return 0.0;
        } else {
            return overallScore;
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}