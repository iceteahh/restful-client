package com.icetea.icetealibrary.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.icetea.icetealibrary.IceteaActivity;
import com.icetea.icetealibrary.IceteaApplication;
import com.icetea.icetealibrary.R;
import com.icetea.icetealibrary.api.ApiService;
import com.icetea.icetealibrary.ui.LoadingView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by icetea on 3/24/2015.
 */

public abstract class MFragment extends Fragment implements ApiService.ServiceCallback,
        DialogInterface.OnClickListener,
        LoadingView.Callback, SwipeRefreshLayout.OnRefreshListener {

    protected String title;
    protected boolean canBack = false;
    protected IceteaActivity activity;
    protected Gson gson;
    protected Bundle requestBundle;
    protected boolean isError = true;
    protected List<Future> futures;
    protected boolean isDestroyed;
    protected RelativeLayout parentView;
    protected LoadingView loadingView;
    protected View content;
    protected boolean cache = true;
    protected boolean isRetain = true;
    protected boolean isLoaded;
    protected View emptyView;
    protected boolean isEmpty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        gson = new Gson();
        futures = new ArrayList<>();
        activity = (IceteaActivity) getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!isLoaded) {
            parentView = (RelativeLayout) inflater.inflate(R.layout.m_fragment, container, false);
            loadingView = (LoadingView) parentView.findViewById(R.id.loading_view1);
            emptyView = parentView.findViewById(R.id.tv_empty);
            loadingView.setCallback(this);
            attachContent(initView(inflater, container, savedInstanceState));
            setRetainInstance(isRetain);
        } else {
            changeLayout(getResources().getConfiguration());
        }
        if(title != null)
            activity.setTitle(title);
        else{
            activity.setTitle(R.string.app_name);
        }
        return parentView;
    }

    public void attachContent(View content) {
        this.content = content;
        content.setVisibility(View.GONE);
        parentView.addView(content,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public boolean isCanBack() {
        return canBack;
    }

    public void setCanBack(boolean canBack) {
        this.canBack = canBack;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void onPreExecute() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingView.setVisibility(View.VISIBLE);
                loadingView.showProgressBar(true);
                if (content != null)
                    content.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSuccess(String result) {
        isError = result == null;
        parseData(result);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed && !isLoaded) {
                    renderData();
                    showContent();
                }
            }
        });
    }

    protected abstract void parseData(String data);

    @Override
    public void onError(final int code, final String msg) {
        isError = true;
        if (!isDestroyed) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code != 200) {
                        loadingView.setVisibility(View.VISIBLE);
                        loadingView.showProgressBar(false);
                        if (content != null) {
                            content.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onNetworkError() {
        isError = true;
        if (!isDestroyed) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingView.setVisibility(View.VISIBLE);
                    loadingView.showProgressBar(false);
                    if (content != null) {
                        content.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    public void onPostExecute() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError)
                    loadingView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (requestBundle != null)
                    futures.add(IceteaApplication.getApiService().requestAPI(requestBundle, this, cache));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Future future : futures) {
            if (future != null && !future.isDone()) {
                Log.e("TASK", "KILL TASK " + future.cancel(true));
            }
        }
        isDestroyed = true;
        IceteaApplication.getRefWatcher().watch(this);
    }

    @Override
    public void onReloadPress() {
        futures.add(IceteaApplication.getApiService().requestAPI(requestBundle, this, cache));
    }

    public void showContent() {
        if(!isEmpty) {
            content.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
        isLoaded = true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isLoaded) {
            if (isError) {
                requestAPI();
            } else if (!isDestroyed) {
                renderData();
                showContent();
            }
        }
    }

    protected abstract void requestAPI();

    protected abstract void renderData();

    @Override
    public void onRefresh() {
        isLoaded = false;
        content.setVisibility(View.GONE);
        futures.add(IceteaApplication.getApiService().requestAPI(requestBundle, this, cache));
    }

    protected abstract void changeLayout(Configuration configuration);

    protected abstract View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
}
