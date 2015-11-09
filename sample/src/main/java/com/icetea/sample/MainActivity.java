package com.icetea.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.icetea.restful_cient.api.ApiCallback;
import com.icetea.restful_cient.api.ApiLoader;
import com.icetea.restful_cient.utilities.Method;

public class MainActivity extends AppCompatActivity {

    private TextView tv;

    private LoaderManager.LoaderCallbacks<String> listPostCallback = new ApiCallback() {
        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            return new ApiLoader
                    (MainActivity.this, Method.GET, ServerConfig.LIST_POST)
                    .putParam("userId", "1");
        }

        @Override
        public void onNetworkError(Loader<String> loader) {
            tv.setText("Network error");
        }

        @Override
        public void onError(Loader<String> loader) {
            tv.setText("Error");
        }

        @Override
        public void onSuccess(Loader<String> loader, String data) {
            tv.setText(data);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv_resource);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        tv.setText("Loading...");
        getSupportLoaderManager().restartLoader(0, null, listPostCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
