package com.example.lmarczak.googlesuggestions;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l.marczak
 *
 * @since 8/30/16.
 */
public class PoiAdapter extends RecyclerView.Adapter<PoiAdapter.VH> {
    List<CharSequence> dataset = new ArrayList<>();

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_item, null, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        final CharSequence item = dataset.get(position);

        if (item instanceof PoiSequence) {
            String result = ((PoiSequence) item).primaryText + "\n" + ((PoiSequence) item).secondaryText;
            holder.tv.setText(result);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(holder.itemView.getContext(), IntegerstoString(((PoiSequence) item).placeTypes), Toast.LENGTH_SHORT).show();
                }
            });
            holder.itemView.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Toast.makeText(holder.itemView.getContext(), ((PoiSequence) item).placeId, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });

        } else {
            holder.tv.setText(item.toString());
        }

    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void refreshWith(List<CharSequence> newItems) {
        this.dataset = newItems;
        notifyItemRangeChanged(0, getItemCount());
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;

        public VH(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.text);
        }
    }

    public static String toString(List<?> array) {
        if (array == null) {
            return "null";
        }
        if (array.size() == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(array.size() * 6);
        sb.append('[');
        sb.append(array.get(0));
        for (int i = 1; i < array.size(); i++) {
            sb.append(", ");
            sb.append(String.valueOf(array.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    public static String IntegerstoString(List<Integer> array) {
        if (array == null) {
            return "null";
        }
        if (array.size() == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(array.size() * 6);
        sb.append('[');
        sb.append(array.get(0));
        for (int i = 1; i < array.size(); i++) {
            sb.append(", ");
            sb.append(String.valueOf(array.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

}
