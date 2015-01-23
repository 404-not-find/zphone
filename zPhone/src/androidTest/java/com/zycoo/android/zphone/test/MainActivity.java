
package com.zycoo.android.zphone.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    private ListView mItemsListView;
    private String[] mItems = {
            "注册", "Call", "语音通话"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mItemsListView = (ListView) findViewById(R.id.id_main_lv);
        mItemsListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, mItems));
        mItemsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, CallActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, Main.class));
                        break;    
                        
                    default:
                        break;
                }

            }
        });

    }
}
