package com.example.nogotochki;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    List<ServiceProfile> services;

    public RecyclerAdapter(List<ServiceProfile> services){
        this.services = services;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.name.setText(services.get(position).title);
        if (services.get(position).title == null)
        {
            viewHolder.name.setText(services.get(position).userId);
        }
        viewHolder.service.setText(services.get(position).type);
        viewHolder.phone.setText(services.get(position).phoneNumber);
    }

    @Override
    public int getItemCount() {
        if (services == null) {
            return 0;
        }
        else {
            return services.size();
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.row_item;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView name, service, phone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.nameTextView);
            service = itemView.findViewById(R.id.serviceTextView);
            phone = itemView.findViewById(R.id.infoTextView);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //тут можно вставить что-то по долгому тапу
                    Toast.makeText(v.getContext(), "Text2", Toast.LENGTH_SHORT).show();

                    return true;
                }
            });


        }

        @Override
        public void onClick(View v) {
            //в этом методе будет показываться более подробная информация о мастере/клиенте
            //будет в виде диалогового окна
            Toast.makeText(v.getContext(), "Text", Toast.LENGTH_SHORT).show();
        }
    }
}
