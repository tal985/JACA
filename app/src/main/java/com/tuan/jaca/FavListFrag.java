package com.tuan.jaca;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FavListFrag extends Fragment
{
    protected View rootView;
    protected LinearLayoutManager rv_layout_mgr;
    protected FavoritesAdapter fa;

    public static FavListFrag newInstance()
    {
        FavListFrag instance = new FavListFrag();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.favorites_layout, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.fav_rv);
        fa = new FavoritesAdapter(getContext(), rv);
        rv_layout_mgr = new LinearLayoutManager(getContext());
        rv.setLayoutManager(rv_layout_mgr);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(fa);
    }

    public void updateFavs(String key)
    {
        if(fa != null)
            fa.removeItemByKey(key);
    }
}
