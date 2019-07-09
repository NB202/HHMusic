package com.hhmusic.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.hhmusic.R;
import com.hhmusic.activity.LocalSearchActivity;
import com.hhmusic.uitl.CommonUtils;

import java.util.ArrayList;
import java.util.List;


public class TabPagerFragment extends AttachDialogFragment {

    private ViewPager viewPager;
    private int page = 0;
    private ActionBar ab;
    private String[] title;


    public static final TabPagerFragment newInstance(int page, String[] title) {
        TabPagerFragment f = new TabPagerFragment();
        Bundle bdl = new Bundle(1);
        bdl.putInt("page_number", page);
        bdl.putStringArray("title", title);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setCurrentItem(page);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            page = getArguments().getInt("page_number");
            title = getArguments().getStringArray("title");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_tab, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) mContext).setSupportActionBar(toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(mContext), 0, 0);

        ab = ((AppCompatActivity) mContext).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.onBackPressed();
            }
        });
        ImageView search = rootView.findViewById(R.id.bar_search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(mContext, LocalSearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mContext.startActivity(intent);
            }
        });


        viewPager = rootView.findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
            viewPager.setOffscreenPageLimit(3);
        }

        final TabLayout tabLayout = rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(R.color.text_color, ThemeUtils.getThemeColorStateList(mContext, R.color.theme_color_primary).getDefaultColor());


        tabLayout.setSelectedTabIndicatorColor(ThemeUtils.getThemeColorStateList(mContext, R.color.theme_color_primary).getDefaultColor());


        return rootView;

    }


    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new MusicFragment(), title[0]);
        adapter.addFragment(new ArtistFragment(), title[1]);
        adapter.addFragment(new AlbumFragment(), title[2]);
        adapter.addFragment(new FolderFragment(), title[3]);

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (title[0].equals("单曲"))
            ab.setTitle("本地音乐");
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            if(mFragments.size() > position)
            return mFragments.get(position);

            return null;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {

        }
    }
}
