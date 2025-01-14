package eu.faircode.xlua;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.cpu.XMockCpuIO;

public class AdapterCpu extends RecyclerView.Adapter<AdapterCpu.ViewHolder> {
    private static final String TAG = "XLua.ADCpu";

    private List<XMockCpuIO> maps = new ArrayList<>();
    private XMockCpuIO lastEnabled = null;
    private Object lock = new Object();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public class ViewHolder extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

        final View itemView;

        final ImageView ivExpanderCpu;
        final TextView tvCpuName;
        final TextView tvCpuManName;
        final TextView tvCpuModelName;

        final TextView tvCpuMapContents;

        final ImageView ivCpuIcon;
        final CheckBox cbCpuSelected;

        private HashMap<String, Boolean> expanded = new HashMap<>();

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            ivExpanderCpu = itemView.findViewById(R.id.ivCpuExpander);
            tvCpuName = itemView.findViewById(R.id.tvCpuName);
            tvCpuManName = itemView.findViewById(R.id.tvCpuManName);
            tvCpuModelName = itemView.findViewById(R.id.tvCpuModelName);
            tvCpuMapContents = itemView.findViewById(R.id.tvCpuMapContents);
            ivCpuIcon = itemView.findViewById(R.id.ivCpuIcon);
            cbCpuSelected = itemView.findViewById(R.id.cbCpuSelected);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            cbCpuSelected.setOnCheckedChangeListener(null);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            cbCpuSelected.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(final View view) {
            Log.i(TAG, "onClick");
            final XMockCpuIO map = maps.get(getAdapterPosition());
            int id = view.getId();
            String name = map.name;

            switch (view.getId()) {
                case R.id.itemViewCpu:
                    if(!expanded.containsKey(name))
                        expanded.put(name, false);

                    expanded.put(name, !expanded.get(name));
                    updateExpanded();
                    break;
            }
        }

        @Override
        public void onCheckedChanged(final CompoundButton cButton, boolean isChecked) {
            Log.i(TAG, "onCheckedChanged");
            final XMockCpuIO cpu = maps.get(getAdapterPosition());
            final int id = cButton.getId();
            Log.i(TAG, "Item Checked=" + id + "==" + cpu.name);
            boolean changed = false;

            switch (id) {
                case R.id.cbCpuSelected:
                    /*if(lastEnabled != null) {
                        if((lastEnabled.equals(cpu) && !isChecked) || isChecked) {
                            lastEnabled.selected = false;
                            lastEnabled = null;
                            changed = true;
                            XMockProxyApi.callPutMockCpuMap(cButton.getContext(), lastEnabled);
                        }
                    } else if(isChecked) {
                        XMockCpuIO last = getLastEnabled(cButton.getContext());
                        if(last != null) {
                            //last.selected = false;
                            for(XMockCpuIO map : maps)
                                if(map.name.equals(last.name)) {
                                    map.selected = false;
                                    changed = true;
                                }

                            XMockProxyApi.callPutMockCpuMap(cButton.getContext(), last);
                        }
                    }*/

                    prepareCheck(cButton.getContext());
                    cpu.selected = isChecked;
                    //if(isChecked) lastEnabled = cpu;
                    //if(changed)
                    notifyDataSetChanged();

                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            XMockProxyApi.callPutMockCpuMap(cButton.getContext(), cpu);
                            //Update UI to uncheck last item
                        }
                    });

                    break;
            }
        }

        public void prepareCheck(Context context) {
            for(XMockCpuIO map : XMockProxyApi.queryGetMockCpuMaps(context)) {
                if(map.selected) {
                    for(XMockCpuIO m : maps)
                        if(map.name.equals(m.name)) {
                            Log.i(TAG, " FOUND INTERNAL : " + map);
                            m.selected = false;
                            XMockProxyApi.callPutMockCpuMap(context, m);
                        }
                }
            }

            for(XMockCpuIO map : maps)
                if(map.selected) {
                    Log.i(TAG, "DISABLING: " + map);
                    map.selected = false;
                }

        }

        void updateExpanded() {
            XMockCpuIO map = maps.get(getAdapterPosition());
            String name = map.name;

            boolean isExpanded = expanded.containsKey(name) && expanded.get(name);
            ivExpanderCpu.setImageLevel(isExpanded ? 1 : 0);

            tvCpuMapContents.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

    }

    AdapterCpu() { setHasStableIds(true); }

    void set(List<XMockCpuIO> maps_c) {
        maps.clear();
        Log.i(TAG, "Set has Init=" + maps_c.size());
        maps.addAll(maps_c);
        Log.i(TAG, "Internal Count=" + maps_c.size());
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) { return maps.get(position).hashCode(); }

    @Override
    public int getItemCount() { return maps.size(); }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AdapterCpu.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cpu, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unwire();
        XMockCpuIO cpu = maps.get(position);
        holder.tvCpuName.setText(cpu.name);
        holder.tvCpuModelName.setText(cpu.model);
        holder.tvCpuManName.setText(cpu.manufacturer);
        holder.tvCpuMapContents.setText(cpu.contents);
        holder.cbCpuSelected.setChecked(cpu.selected);
        holder.updateExpanded();
        holder.wire();
    }
}
