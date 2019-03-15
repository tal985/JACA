package com.tuan.jaca;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class RolesTabAdapter extends FragmentPagerAdapter
{
    private final List<Fragment> mfl = new ArrayList<Fragment>();
    private final List<String> mftl = new ArrayList<String>();
    //protected Context mc;

    RolesTabAdapter(FragmentManager fm)
    {
        super(fm);
    }

    void addFragToList(Fragment frag, String role)
    {
        mfl.add(frag);
        mftl.add(role);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int pos)
    {
        return mftl.get(pos);
    }

    @Override
    public Fragment getItem(int pos)
    {
        return mfl.get(pos);
    }

    @Override
    public int getCount()
    {
        return mfl.size();
    }
}
