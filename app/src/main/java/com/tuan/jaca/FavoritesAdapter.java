package com.tuan.jaca;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>
{
    public class FavoritesViewHolder extends RecyclerView.ViewHolder
    {
        protected ImageView favProfile;
        protected TextView favName;
        protected String nameOnly, titleOnly, keyOnly;

        public FavoritesViewHolder(View v)
        {
            super(v);
            favProfile = v.findViewById(R.id.fav_profile);
            favName = v.findViewById(R.id.fav_name);
            //Swipe and click functionality
            rv.addOnItemTouchListener(sd);

            v.setOnTouchListener
            (
                new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        //Go only on release of button press
                        if(event.getAction() == MotionEvent.ACTION_UP &&
                            sd.getAction() == SwipeDetectorRL.Action.None)
                        {
                            ma.goToChamp(nameOnly, titleOnly);
                        }
                        else if(event.getAction() == MotionEvent.ACTION_UP &&
                            sd.getAction() == SwipeDetectorRL.Action.RL)
                        {
                            removeItem(getAdapterPosition());
                            userDBChamp.child(keyOnly).removeValue();
                        }
                        return true;
                    }
                }
            );
        }
    }

    protected List<String> FavChampKeys;
    protected Context mc;
    protected MainActivity ma;
    protected SwipeDetectorRL sd = new SwipeDetectorRL();
    protected RecyclerView rv;
    protected DatabaseReference userDBChamp;

    //public FavoritesAdapter(Context c, List<String> keyList, RecyclerView rvOrig)
    public FavoritesAdapter(Context c, RecyclerView rvOrig)
    {
        mc = c;
        ma = (MainActivity) mc;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userDBChamp = ma.fbdb.getReference(user.getUid()).child("champions");
        rv = rvOrig;
        FavChampKeys = ma.favChampKeys;
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        //Create new view and inflate
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.favorites_entry, parent, false);
        FavoritesViewHolder fvh = new FavoritesViewHolder(v);
        return fvh;
    }

    @Override
    public void onBindViewHolder(FavoritesViewHolder fvh, int pos)
    {
        String temp;
        StringBuilder picURL = new StringBuilder("http://ddragon.leagueoflegends.com/cdn/");
        picURL.append(ma.version);
        picURL.append("/img/champion/");
        picURL.append(FavChampKeys.get(pos));
        picURL.append(".png");

        //Set and display some views
        fvh.keyOnly = FavChampKeys.get(pos);
        fvh.nameOnly = ma.findNameByKeyName(fvh.keyOnly);
        fvh.titleOnly = ma.findTitleByKeyName(fvh.keyOnly);
        temp = fvh.nameOnly + "\n" + fvh.titleOnly;

        fvh.favName.setTextColor(Color.WHITE);
        fvh.favName.setText(temp);
        fvh.favProfile.getLayoutParams().height = 200;
        fvh.favProfile.getLayoutParams().width = 200;

        RequestOptions rq = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_sync_black_24dp)
                .error(android.R.drawable.stat_notify_error);

        Glide.with(mc)
            .load(picURL.toString())
            .apply(rq)
            .into(fvh.favProfile);
    }

    @Override
    public int getItemCount()
    {
        return FavChampKeys.size();
    }

    //Remove by position in the listview
    public void removeItem(int pos)
    {
        FavChampKeys.remove(pos);
        notifyItemRemoved(pos);
        notifyItemChanged(pos, FavChampKeys.size());
    }

    //Remove a favorites entry by champion key
    public void removeItemByKey(String key)
    {
        int pos = FavChampKeys.indexOf(key);
        FavChampKeys.remove(key);
        notifyItemRemoved(pos);
        notifyItemChanged(pos, FavChampKeys.size());
    }

    @Override
    public long getItemId(int pos)
    {
        return FavChampKeys.get(pos).hashCode();
    }

}
