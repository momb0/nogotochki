package com.example.nogotochki;

import android.view.LayoutInflater;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //View view = layoutInflater.inflate(R.layout.row_item, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        //viewHolder.getName().setText(String.valueOf(123));


    }

    @Override
    public int getItemCount() {
        return 20;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView imageView;
        private TextView name, service, info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //imageView = itemView.findViewById(R.id.imageView2);
            name = itemView.findViewById(R.id.nameTextView);
            //service = itemView.findViewById(R.id.serviceTextView);
            //info = itemView.findViewById(R.id.infoTextView);


        }

        public TextView getName(){
            return name;
        }
    }
}
