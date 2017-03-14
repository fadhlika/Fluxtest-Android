package com.fadh.fluxtest.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fadh.fluxtest.LogFragment;
import com.fadh.fluxtest.MainFragment;

/**
 * Created by fadhlika on 3/10/2017.
 */

public class FluxFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"Main", "Log"};
    private Context context;

    public FluxFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                MainFragment mainFragment = new MainFragment();
                return mainFragment;
            case 1:
                LogFragment logFragment = new LogFragment();
                return logFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
