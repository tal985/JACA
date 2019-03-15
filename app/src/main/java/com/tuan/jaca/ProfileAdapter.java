package com.tuan.jaca;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * Created by Tuan on 3/20/2018.
 */

public class ProfileAdapter extends BaseAdapter
{
    private class ProfileViewHolder
    {
        protected TextView champName;
        protected ImageView champPic;

        public ProfileViewHolder(TextView nametv, ImageView piciv)
        {
            champName = nametv;
            champPic = piciv;
        }
    }

    protected Context mc;


    public ProfileAdapter(Context context)
    {
        mc = context;
    }

    @Override
    public int getCount()
    {
        return ((MainActivity) mc).CRArr.length;
    }

    @Override
    public long getItemId(int pos)
    {
        return 0;
    }

    @Override
    public Object getItem(int pos)
    {
        return null;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        String tempKey;
        StringBuilder picURL;
        MainActivity ma = (MainActivity) mc;

        if(convertView == null)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(mc);
            convertView = layoutInflater.inflate(R.layout.grid_entry, null);
            ImageView iv = convertView.findViewById(R.id.champPic);
            TextView tv = convertView.findViewById(R.id.champName);
            ProfileViewHolder pvh = new ProfileViewHolder(tv, iv);
            convertView.setTag(pvh);
        }

        ProfileViewHolder pvh = (ProfileViewHolder) convertView.getTag();

        pvh.champName.setTextColor(Color.WHITE);
        pvh.champName.setText(ma.CRArr[pos].name);
        pvh.champPic.getLayoutParams().height = 200;
        pvh.champPic.getLayoutParams().width = 200;

        //tempKey = (ma.findKeyByName(champNameArr[pos]));
        tempKey = ma.CRArr[pos].key;

        picURL = new StringBuilder("http://ddragon.leagueoflegends.com/cdn/");
        picURL.append(ma.version);
        picURL.append("/img/champion/");
        picURL.append(tempKey);
        picURL.append(".png");

        RequestOptions rq = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_sync_black_24dp)
                .error(android.R.drawable.stat_notify_error);

        Glide.with(mc)
            .load(picURL.toString())
            .apply(rq)
            .into(pvh.champPic);

        return convertView;
    }
}
