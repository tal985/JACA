package com.tuan.jaca;

//import android.support.v4.app.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class RolesFrag extends Fragment
{
    protected int id;
    protected JSONObject oneRole;
    protected MainActivity ma;

    public static RolesFrag newInstance(JSONObject oneRole, int id) throws JSONException
    {
        RolesFrag instance = new RolesFrag();
        instance.oneRole = oneRole;
        instance.id = id;
        return instance;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.role_layout, container, false);
        String[] tempSArr;
        ma = (MainActivity) getActivity();

        RequestOptions rq = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_sync_black_24dp)
            .error(android.R.drawable.stat_notify_error);

        try
        {
            ImageView tempIV;
            double temp;
            JSONObject hashes = oneRole.getJSONObject("hashes");

            //Win, play, and ban rate
            temp = oneRole.getDouble("winRate");
            temp = Math.floor(temp * 10000) / 100;
            TextView wr = view.findViewById(R.id.winRateNumber);
            wr.setText(Double.toString(temp) + "%");

            temp = oneRole.getDouble("playRate");
            temp = Math.floor(temp * 10000) / 100;
            TextView pr = view.findViewById(R.id.playRateNumber);
            pr.setText(Double.toString(temp) + "%");

            temp = oneRole.getDouble("banRate");
            temp = Math.floor(temp * 10000) / 100;
            TextView br = view.findViewById(R.id.banRateNumber);
            br.setText(Double.toString(temp) + "%");

            //Item builds
            JSONObject tempObj = hashes.getJSONObject("finalitemshashfixed");

            JSONObject hc = tempObj.getJSONObject("highestCount");
            tempSArr = getItems(hc.getString("hash"));

            //For each entry in the array, use glide to load the url into the imageviews
            for(int i = 0; i < tempSArr.length; i++)
            {
                //Insert frequently completed into ImageView via Glide
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("fcitem" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[i])
                    .apply(rq)
                    .into(tempIV);
            }

			JSONObject hw = tempObj.getJSONObject("highestWinrate");
            tempSArr = getItems(hw.getString("hash"));

            for(int i = 0; i < tempSArr.length; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("hwitem" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[i])
                    .apply(rq)
                    .into(tempIV);
            }

            //Skills
            tempObj = hashes.getJSONObject("skillorderhash");

            hc = tempObj.getJSONObject("highestCount");
            DrawSkillGrid sg = view.findViewById(R.id.foskillgrid);
            sg.insertHashstring(hc.get("hash").toString());

            hw = tempObj.getJSONObject("highestWinrate");
            sg = view.findViewById(R.id.hwskillgrid);
            sg.insertHashstring(hw.get("hash").toString());

            //Starting items
            tempObj = hashes.getJSONObject("firstitemshash");

            hc = tempObj.getJSONObject("highestCount");
            tempSArr = getStartingItems(hc.getString("hash"));

            for(int i = 0; i < tempSArr.length; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("fstart" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[i])
                    .apply(rq)
                    .into(tempIV);
            }

            hw = tempObj.getJSONObject("highestWinrate");
            tempSArr = getStartingItems(hw.getString("hash"));

            if(!tempSArr[0].equals(tempSArr[1]))
            {
                for (int i = 0; i < tempSArr.length; i++)
                {
                    tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                        ("hwstart" + (i + 1), "id", ma.getPackageName()));

                    Glide.with(this)
                        .load(tempSArr[i])
                        .apply(rq)
                        .into(tempIV);
                }
            }
            else
            {
                tempIV = (ImageView) view.findViewById(R.id.hwstart1);

                    Glide.with(this)
                        .load(tempSArr[0])
                        .apply(rq)
                        .into(tempIV);

                view.findViewById(R.id.hwstart2).setVisibility(View.INVISIBLE);
            }

            //Summoner spells
            tempObj = hashes.getJSONObject("summonershash");

            hc = tempObj.getJSONObject("highestCount");
            tempSArr = getSums(hc.getString("hash"));

            for(int i = 0; i < tempSArr.length; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("fsum" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[i])
                    .apply(rq)
                    .into(tempIV);
            }

            hw = tempObj.getJSONObject("highestWinrate");
            tempSArr = getSums(hw.getString("hash"));

            for(int i = 0; i < tempSArr.length; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("hwsum" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[i])
                    .apply(rq)
                    .into(tempIV);
            }

            //Evo for Viktor and Kha'zix; Kai'sa isn't included because her evo is dependent on items
            if(id == 112 || id == 121)
            {
                tempObj = hashes.getJSONObject("evolveskillorder");

                hc = tempObj.getJSONObject("highestCount");
                tempSArr = getEvo(hc.getString("hash"));

                for(int i = 0; i < tempSArr.length; i++)
                {
                    tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                        ("fevo" + (i + 1), "id", ma.getPackageName()));
                    tempIV.setVisibility(View.VISIBLE);

                    Glide.with(this)
                        .load(tempSArr[i])
                        .apply(rq)
                        .into(tempIV);
                }

                hw = tempObj.getJSONObject("highestWinrate");
                tempSArr = getEvo(hw.getString("hash"));

                for(int i = 0; i < tempSArr.length; i++)
                {
                    tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                        ("hwevo" + (i + 1), "id", ma.getPackageName()));
                    tempIV.setVisibility(View.VISIBLE);
                    Glide.with(this)
                        .load(tempSArr[i])
                        .apply(rq)
                        .into(tempIV);
                }
            }

            //Runes
            tempObj = hashes.getJSONObject("runehash");

            hc = tempObj.getJSONObject("highestCount");
            tempSArr = getRunes(hc.getString("hash"));

            //Primary Runes
            for(int i = 0; i < 4; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("fprune" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[i])
                    .apply(rq)
                    .into(tempIV);
            }

            //Secondary Runes
            for(int i = 0; i < 2; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("fsrune" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[4+i])
                    .apply(rq)
                    .into(tempIV);
            }

            hw = tempObj.getJSONObject("highestWinrate");
            tempSArr = getRunes(hw.getString("hash"));

            for(int i = 0; i < 4; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("hwprune" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[i])
                    .apply(rq)
                    .into(tempIV);
            }

            //Secondary Runes
            for(int i = 0; i < 2; i++)
            {
                tempIV = (ImageView) view.findViewById(getResources().getIdentifier
                    ("hwsrune" + (i + 1), "id", ma.getPackageName()));

                Glide.with(this)
                    .load(tempSArr[4+i])
                    .apply(rq)
                    .into(tempIV);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return view;
    }

    //Returns a size 6 string array of a six item build URLs
	public String[] getItems(String hashItems)
	{
		String[] items = new String[6];

		for(int i = 0; i < items.length; i++)
		{
			items[i] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/item/" + hashItems.substring((5 * i) + 6, (5 * i) + 10) + ".png";
		}

		return items;
	}

	//Return a size 2 string array of starting items URLs
	public String[] getStartingItems(String hashStarter)
	{
		Set<String> ts = new LinkedHashSet<String>(Arrays.asList(hashStarter.substring(6).split("-")));
		String[] start = ts.toArray(new String[ts.size()]);
		String[] start2 = new String[2];
		//Only do first 2 entries
		start2[0] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/item/" + start[0] + ".png";
		start2[1] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/item/" + start[start.length - 1] + ".png";
		return start2;
	}

	//Returns a size 2 string array of summoners URL. The string is based on a hashmap's values.
	public String[] getSums(String hashSums)
	{
		String[] temp = new String[2];
		temp[0] = "" + hashSums.charAt(0);
		temp[1] = hashSums.substring(2);
		temp[0] = ma.numsToSums.get(Integer.valueOf(temp[0]));
		temp[1] = ma.numsToSums.get(Integer.valueOf(temp[1]));
		temp[0] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/Summoner" + temp[0] + ".png";
		temp[1] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/Summoner" + temp[1] + ".png";
		return temp;
	}

	//Return size 3 string array of evo URLs for khazix or viktor
    public String[] getEvo(String hashEvo)
    {
        StringBuilder temp = new StringBuilder();
        String[] urlArr = new String[3];
        int count = 0;
        for(char i: hashEvo.toCharArray())
        {
            if(i == 'Q' || i == 'W' || i == 'E' || i == 'R')
                temp.append(i);
        }

        for(char i: temp.toString().toCharArray())
        {
            //viktor
            if(id == 112)
            {
                if(i == 'Q')
                    urlArr[count] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/ViktorPowerTransfer.png";
                else if(i == 'W')
                    urlArr[count] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/ViktorGravitonField.png";
                else
                    urlArr[count] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/ViktorDeathRay.png";
            }
            //khazix
            else if(id == 121)
            {
                if(i == 'Q')
                    urlArr[count] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/KhazixQ.png";
                else if(i == 'W')
                    urlArr[count] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/KhazixW.png";
                else if(i == 'E')
                    urlArr[count] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/KhazixE.png";
                else
                    urlArr[count] = "http://ddragon.leagueoflegends.com/cdn/" + ma.version + "/img/spell/KhazixR.png";
            }
            count++;
        }

        return urlArr;
    }

    //Returns a size 6 string array of runes URLs
    public static String[] getRunes(String hashRunes)
    {
        //TODO: switch from solomid to riot's runes
        /*
        5001 = hp
        5002 = armor
        5003 = adaptive force
        5004, 5006 = doesn't exist
        5005 = as
        5007 = cdr
        5008 = mr
        8000 = precision
        8100 = domination
        8200 = sorcery
        8300 = inspiration
        8400 = resolve
        */


        for(int i = 0; i < 5; i++)
            hashRunes = hashRunes.replace(8000 + 100 * i + "-", "");

        String[] runes = hashRunes.split("-");

        //Fix the adaptive force and mr rune
        for(int i = runes.length - 1; i > runes.length - 4; i--)
        {
            if(runes[i].equals("5008"))
                runes[i] = "5003";
            else if(runes[i].equals("5003"))
                runes[i] = "5008";
        }

        System.out.println("Rune Array: " + Arrays.toString(runes));

        //Cannot be a for each loop!
        for(int i = 0; i < runes.length; i++)
            if(Integer.parseInt(runes[i]) - 5000 > 100)
                runes[i] = "https://s3.amazonaws.com/solomid-cdn/league/runes_reforged/" + runes[i]
                        + ".png";
            else if(!runes[i].equals(84))
                runes[i] = "https://s3.amazonaws.com/solomid-resources/probuildsnet/rune-shards/"
                        + runes[i] + ".png";

        System.out.println("Rune Array: " + Arrays.toString(runes));

        return runes;
    }
}
