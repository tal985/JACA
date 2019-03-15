package com.tuan.jaca;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tuan on 3/26/2018.
 */

public class ChampPostFrag extends Fragment
{
    protected View rootView;
    protected ChampRec cr;
    protected DatabaseReference userDBChamp;
    protected MainActivity ma;
    protected JSONArray champArr = null;

    public static ChampPostFrag newInstance(ChampRec cr)
    {
        ChampPostFrag instance = new ChampPostFrag();
        instance.cr = cr;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.champ_post, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        ma = (MainActivity) getActivity();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FloatingActionButton fab = rootView.findViewById(R.id.favoriteFAB);
        ImageView splashArt = rootView.findViewById(R.id.champSplash);
        String splashURL = "http://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + cr.key +
                "_0.jpg";

        //Default off
        fab.setImageResource(android.R.drawable.star_big_off);

        //Get database if logged in
        if(user != null)
        {
            userDBChamp = ma.fbdb.getReference(user.getUid()).child("champions");

            //Set FAB sprite to the corresponding value
            userDBChamp.addListenerForSingleValueEvent
            (
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child(cr.key).exists())
                            fab.setImageResource(android.R.drawable.star_big_on);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        //Error, cancelled
                    }
                }
            );
        }

        //FAB functionality
        fab.setOnClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(userDBChamp != null && user != null)
                    {
                        userDBChamp.addListenerForSingleValueEvent
                        (
                            new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    //If champion's already favorited, remove
                                    if(dataSnapshot.child(cr.key).exists())
                                    {
                                        fab.setImageResource(android.R.drawable.star_big_off);
                                        userDBChamp.child(cr.key).removeValue();

                                    }
                                    //Else, add the champion to favorites
                                    else
                                    {
                                        fab.setImageResource(android.R.drawable.star_big_on);
                                        userDBChamp.child(cr.key).setValue(true);
                                    }
                                    ma.flf.updateFavs(cr.key);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {
                                    //Error, cancelled
                                }
                            }
                        );
                    }
                    else
                    Snackbar.make(rootView,"Please log in to save champion to favorites!",
                        Snackbar.LENGTH_LONG).show();
                }
            }
        );

        //Splash art
        RequestOptions rq = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_sync_black_24dp)
            .error(android.R.drawable.stat_notify_error);

        System.out.println("Before Glide.with!");

        Glide.with(this)
            .load(splashURL)
            .apply(rq)
            .into(splashArt);

        System.out.println("After Glide.with!");

        //Items
        //"http://ddragon.leagueoflegends.com/cdn/8.5.1/img/item/" + itemNum + ".png"

        //Runes
        //"https://s3.amazonaws.com/solomid-cdn/league/runes_reforged/" + runeNum + ".png"

        //Example for annie w/ hashes of items
        //http://api.champion.gg/v2/champions/1?&champData=hashes&api_key=

        //TODO: REMOVE API KEY

        //Build url for fetching champion data
        System.out.println("CRID: " + cr.id);
        String url = "https://api.champion.gg/v2/champions/" + cr.id + "?&champData=hashes&api_key=";
        System.out.println("URL: " + url);

        //Get JSONArray
        try
        {
            getCharJSONArrayFromUrl(url);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

    }

    public void getCharJSONArrayFromUrl(String url) throws JSONException
	{
		JsonArrayRequest jar = new JsonArrayRequest
        (
            Request.Method.GET,
            url,
            null,
            new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    champArr = response;
                    System.out.println("champArr: " + champArr.toString());
                    fetchComplete();
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    error.printStackTrace();
                }
            }
        )
        {
            //User Agent
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-Agent", "CS371M-tal985-JACA");
                return headers;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(ma);
        rq.add(jar);
	}

	//After fetching data is complete, create role fragments for tabs
	public void fetchComplete()
    {
        //Iterate through JSONArray and create a role frag for each role
        ViewPager rolesVP = rootView.findViewById(R.id.rolesVP);
        RolesTabAdapter rta = new RolesTabAdapter(getChildFragmentManager());
        JSONObject tempRoleObject;
        for(int i = 0; i < champArr.length(); i++)
            try
            {
                tempRoleObject = champArr.getJSONObject(i);
                System.out.println("TRO: " + tempRoleObject.toString());
                if(tempRoleObject.get("role").equals("DUO_SUPPORT"))
                    rta.addFragToList(RolesFrag.newInstance(tempRoleObject, cr.id), "Support");
                else if(tempRoleObject.get("role").equals("DUO_CARRY"))
                    rta.addFragToList(RolesFrag.newInstance(tempRoleObject, cr.id), "ADC");
                else if(tempRoleObject.get("role").equals("TOP"))
                    rta.addFragToList(RolesFrag.newInstance(tempRoleObject, cr.id), "Top");
                else if(tempRoleObject.get("role").equals("MIDDLE"))
                    rta.addFragToList(RolesFrag.newInstance(tempRoleObject, cr.id), "Middle");
                else
                    rta.addFragToList(RolesFrag.newInstance(tempRoleObject, cr.id), "Jungle");
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        rolesVP.setAdapter(rta);

        //Set up tabs
        TabLayout tabLayout = rootView.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(rolesVP);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }

}
