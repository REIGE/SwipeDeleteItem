package com.reige.swipedeleteitem;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ListView listView;

    private ArrayList<String> dataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);

        for (int i = 0; i < 10; i++) {
            dataList.add("我是第" + i + "条数据");
        }

        listView.setAdapter(new MyAdapter());

    }



    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item_swipe_delete, null);
            }
            ViewHolder holder = ViewHolder.getHolder(convertView);
            holder.name.setText(dataList.get(position));
            return convertView;
        }
    }

    static class ViewHolder {
        TextView name, top, delete;

        ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.tv_name);
            top = (TextView) convertView.findViewById(R.id.tv_top);
            delete = (TextView) convertView.findViewById(R.id.tv_delete);
        }

        static ViewHolder getHolder(View convertView) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            return holder;
        }
    }
}
