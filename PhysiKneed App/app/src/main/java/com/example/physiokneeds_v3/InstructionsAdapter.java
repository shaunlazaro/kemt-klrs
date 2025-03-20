package com.example.physiokneeds_v3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.InstructionViewHolder> {

    private final List<InstructionItem> instructions;

    public InstructionsAdapter(List<InstructionItem> instructions) {
        this.instructions = instructions;
    }

    public static class InstructionViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView instructionText;

        public InstructionViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            instructionText = itemView.findViewById(R.id.instructionText);
        }
    }

    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setup_panel_1, parent, false);
        return new InstructionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {
        InstructionItem item = instructions.get(position);
        holder.imageView.setImageResource(item.getImageResId());
        holder.instructionText.setText(item.getInstructionText());
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }
}
