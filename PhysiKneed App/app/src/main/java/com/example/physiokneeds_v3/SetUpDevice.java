package com.example.physiokneeds_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class InstructionItem {
    private final int imageResId;
    private final String instructionText;

    public InstructionItem(int imageResId, String instructionText) {
        this.imageResId = imageResId;
        this.instructionText = instructionText;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getInstructionText() {
        return instructionText;
    }
}

public class SetUpDevice extends AppCompatActivity {

    boolean readyToBegin = false;
    boolean inProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_up_device);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // instructions panel
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        WormDotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);

        List<InstructionItem> instructions = new ArrayList<>();
        instructions.add(new InstructionItem(R.drawable.setup_1, "Place the tracking mount on a flat surface in front of your TV."));
        instructions.add(new InstructionItem(R.drawable.setup_2, "Plug in the power for the mount."));
        instructions.add(new InstructionItem(R.drawable.setup_3, "Plug in the HDMI cable to your TV or monitor."));

        InstructionsAdapter adapter = new InstructionsAdapter(instructions);
        viewPager.setAdapter(adapter);

        dotsIndicator.attachTo(viewPager);

        Button nextButton = findViewById(R.id.next_button);

        ImageButton backButton = findViewById(R.id.back_button);

        TextView skipText = findViewById(R.id.skip_button);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView titleTop = findViewById(R.id.title_top);

        skipText.setOnClickListener(v -> {
            titleTop.setText("Ready to begin");
            List<InstructionItem> instructionsNew = new ArrayList<>();
            instructionsNew.add(new InstructionItem(R.drawable.setup_4, "Plug your phone into the mount. Turn the knob to secure it tightly."));
            InstructionsAdapter adapterNew = new InstructionsAdapter(instructionsNew);
            viewPager.setAdapter(adapterNew);
            nextButton.setText("Begin Exercises");
            skipText.setVisibility(View.GONE);
            dotsIndicator.setVisibility(View.GONE);
            readyToBegin = true;
        });

        DisplayManager displayManager = (DisplayManager) getSystemService(this.DISPLAY_SERVICE);

        RoutineConfig routineConfig = (RoutineConfig) getIntent().getSerializableExtra(HomeScreen.ROUTINE_TAG);

        if (getIntent().getSerializableExtra("FROM_EXERCISES") != null) {
            inProgress = true;
            skipText.setVisibility(View.GONE);
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int nextItem = viewPager.getCurrentItem() + 1;
                if (nextItem < viewPager.getAdapter().getItemCount()) {
                    viewPager.setCurrentItem(nextItem);
                } else if (readyToBegin && displayManager.getDisplays().length >= 2){
                    Intent intent = new Intent(SetUpDevice.this, PerfromExercises.class);
                    intent.putExtra(HomeScreen.ROUTINE_TAG, routineConfig);
                    SetUpDevice.this.startActivity(intent);
                } else if (readyToBegin && displayManager.getDisplays().length < 2) {
                    Toast.makeText(getApplicationContext(), "No External Display Connected", Toast.LENGTH_SHORT).show();
                } else if (!inProgress){
                    titleTop.setText("Ready to begin");
                    List<InstructionItem> instructions = new ArrayList<>();
                    instructions.add(new InstructionItem(R.drawable.setup_4, "Plug your phone into the mount. Turn the knob to secure it tightly."));
                    InstructionsAdapter adapter = new InstructionsAdapter(instructions);
                    viewPager.setAdapter(adapter);
                    nextButton.setText("Begin Exercises");
                    skipText.setVisibility(View.GONE);
                    dotsIndicator.setVisibility(View.GONE);
                    readyToBegin = true;
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int prevItem = viewPager.getCurrentItem() - 1;
                if (readyToBegin) {
                    titleTop.setText("Setup your device");
                    List<InstructionItem> instructions = new ArrayList<>();
                    instructions.add(new InstructionItem(R.drawable.setup_1, "Place the tracking mount on a flat surface in front of your TV."));
                    instructions.add(new InstructionItem(R.drawable.setup_2, "Plug in the power for the mount."));
                    instructions.add(new InstructionItem(R.drawable.setup_3, "Plug in the HDMI cable to your TV or monitor."));
                    InstructionsAdapter adapter = new InstructionsAdapter(instructions);
                    viewPager.setAdapter(adapter);
                    dotsIndicator.attachTo(viewPager);
                    viewPager.setCurrentItem(viewPager.getAdapter().getItemCount() - 1);
                    nextButton.setText("Next");
                    skipText.setVisibility(View.VISIBLE);
                    dotsIndicator.setVisibility(View.VISIBLE);
                    readyToBegin = false;
                } else if (prevItem >= 0){
                    viewPager.setCurrentItem(prevItem);
                } else {
                    finish();
                }
            }
        });
    }
}