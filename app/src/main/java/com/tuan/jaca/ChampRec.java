package com.tuan.jaca;

import java.util.List;

/**
 * Created by Tuan on 3/19/2018.
 */

public class ChampRec
{
    public String key;
    public String name;
    public String title;
    public int id;
    //public List<String> roles;


    @Override
    public String toString()
    {
        return "[Key: " + key + ", Name: " + name + ", Title: " + title + ", ID: " + id + "]";
    }
}
