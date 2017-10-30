package com.nativetemplate.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    RecyclerView srecycler_items;
    ArrayList<String> home_topics = new ArrayList<>();
    String[] topics_strings;
    HomeAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        srecycler_items = (RecyclerView) findViewById(R.id.xrecycler_items);
        topics_strings = new String[]{"SearchView", "RecyclerView", "ListView", "", "", "", "", "", "", "", "", "", "", "", "", "", "Google maps", "Dialogs & Pickers", "Gridview", "Expandable Listview", "Cardview", "Widgets"};

        for(int ts=0; ts<topics_strings.length; ts++){
            home_topics.add(topics_strings[ts]);
        }

        adapter = new HomeAdapter(home_topics);

        /*//To show only one column per row
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        srecycler_items.setLayoutManager(mLayoutManager);*/

        //To show multiple columns (like gridview) per row*/
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplication(), 2);
        srecycler_items.setLayoutManager(mLayoutManager);

        /*//To show staggered grid layout
        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);
        srecycler_items.setLayoutManager(mLayoutManager);*/

        srecycler_items.setItemAnimator(new DefaultItemAnimator());
        srecycler_items.setAdapter(adapter);

    }

    public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

        private ArrayList<String> button_list = new ArrayList<>();

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public Button sbtn_home_item;

            public MyViewHolder(View view) {
                super(view);
                sbtn_home_item = (Button) view.findViewById(R.id.xbtn_home_item);
            }
        }

        public HomeAdapter(ArrayList<String> button_list) {
            this.button_list = button_list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_recycle_layout, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.sbtn_home_item.setText(button_list.get(position));
        }

        @Override
        public int getItemCount() {
            return button_list.size();
        }
    }
}
