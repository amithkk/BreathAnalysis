package com.amithkk.breathsampler;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.amithkk.breathsampler.pojo.BreathResult;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.skydoves.expandablelayout.ExpandableLayout;
import com.skydoves.expandablelayout.OnExpandListener;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BreathHistoryActivity extends AppCompatActivity {

    RecyclerView breathRecycler;
    ProgressBar loadingProgress;
    private final int REQUEST_BREATH = 120;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Your Past Samples");
        loadingProgress = findViewById(R.id.loadingProgress);



        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent i = new Intent(BreathHistoryActivity.this, BreathRecorderActivity.class);
                startActivityForResult(i, REQUEST_BREATH);
            }
        });

        breathRecycler = findViewById(R.id.breathRecycler);
        Query breathQuery = FirebaseDatabase.getInstance()
                .getReference()
                .child("recordLogs")
                .orderByChild("timeAdded");
        FirebaseRecyclerOptions<BreathResult> options =
                new FirebaseRecyclerOptions.Builder<BreathResult>()
                        .setQuery(breathQuery, BreathResult.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<BreathResult, BreathResultHolder>(options) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                loadingProgress.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @NonNull
            @Override
            public BreathResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.breath_card, parent, false);
                return new BreathResultHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull BreathResultHolder holder, int position, @NonNull BreathResult model) {

                holder.result.setText(model.getResult());

                holder.statusBG.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getAudioURL())));
                        return true;
                    }
                });
                Date date = new java.util.Date(model.getTimeAdded()*1000L);
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+5:30"));
                String formattedDate = sdf.format(date);
                holder.timestamp.setText(formattedDate);
                holder.sampleSize.setText(String.valueOf(model.getSampleLength()/1000)+" seconds");


                if(model.getStatus().equals("OK_PROBLEM")){
                    holder.statusBG.setImageTintList(ColorStateList.valueOf((0xffd67e00)));
                    holder.statusIcon.setImageDrawable(getDrawable(R.drawable.ic_warning));
                    holder.moreInfoButton.setVisibility(View.VISIBLE);
                    holder.moreInfoTV.setVisibility(View.VISIBLE);
                }
            }
        };

        breathRecycler.setAdapter(adapter);
        LinearLayoutManager ll = new LinearLayoutManager(BreathHistoryActivity.this);
        ll.setStackFromEnd(true);
        ll.setReverseLayout(true);
        breathRecycler.setLayoutManager(ll);
        adapter.startListening();

    }



    public class BreathResultHolder extends RecyclerView.ViewHolder {

        TextView result, timestamp, sampleSize, moreInfoTV;
        ImageView statusBG, statusIcon, moreInfoButton;
        ExpandableLayout expandableLayout;


        public BreathResultHolder(@NonNull View v) {
            super(v);

            result = v.findViewById(R.id.result_tv);
            timestamp = v.findViewById(R.id.date_tv);
            sampleSize = v.findViewById(R.id.sample_tv);
            statusBG = v.findViewById(R.id.status_bg);
            statusIcon = v.findViewById(R.id.status_icon);
            moreInfoButton = v.findViewById(R.id.tapForMore);
            moreInfoTV = v.findViewById(R.id.tapForMoreTV);
            expandableLayout  = v.findViewById(R.id.expandable);

            moreInfoButton.setOnClickListener(view -> {
                Log.d("AppTag", "expanded2");
                expandableLayout.setExpanded(true);
                Snackbar.make(findViewById(R.id.fab), "Report Will Be Shown (Not Implemented)", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show();
                expandableLayout.expand(100);
            });

            expandableLayout.setOnExpandListener(new OnExpandListener() {
                @Override
                public void onExpand(boolean b) {

                    Log.d("AppTag", "EXp:"+b);

                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_BREATH){
            if(resultCode == RESULT_OK)
            {
                Snackbar.make(findViewById(R.id.fab), "Successfully Submitted Sample", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show();
            }
            else {
                Snackbar.make(findViewById(R.id.fab), "Failed to Submit Sample", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show();
            }
        }
    }
}
