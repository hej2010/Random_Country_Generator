package se.swecookie.randomcountrygenerator;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<CountryHistory> history;

    HistoryAdapter(@NonNull List<CountryHistory> history) {
        this.history = history;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        final CountryHistory c = history.get(position);
        final Context context = holder.txtCountry.getContext();
        if (context == null) {
            return;
        }
        String countryCode = c.getCode();
        String countryName = c.getName();
        String continent = c.getContinent();

        if (countryCode.equals("DO")) {
            countryCode = "do1";
        }
        holder.txtCountry.setText(context.getString(R.string.history_item, countryName, countryCode, MainActivity.getContinentLong(continent)));
        holder.txtId.setText(String.valueOf(history.size() - position));

        Glide.with(context)
                .load(c.getDrawableID(context))
                .into(holder.imgIcon);
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCountry, txtId;
        ImageView imgIcon;

        ViewHolder(View v) {
            super(v);
            txtCountry = v.findViewById(R.id.txtCountry);
            imgIcon = v.findViewById(R.id.imgIcon);
            txtId = v.findViewById(R.id.txtId);
        }

    }
}
