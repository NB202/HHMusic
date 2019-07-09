package com.hhmusic.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.hhmusic.R;
import com.hhmusic.fragmentnet.SearchHotWordFragment;
import com.hhmusic.fragmentnet.SearchTabPagerFragment;
import com.hhmusic.fragmentnet.SearchWords;
import com.hhmusic.provider.SearchHistory;
import com.hhmusic.uitl.CommonUtils;

public class NetSearchWordsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnTouchListener, SearchWords {

    private SearchView mSearchView;
    private InputMethodManager mImm;
    private String queryString;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SearchHotWordFragment f = new SearchHotWordFragment();
        f.searchWords(NetSearchWordsActivity.this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.search_frame, f);
        ft.commit();

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getResources().getString(R.string.search_net_music));

        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.menu_search).expandActionView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {

        hideInputManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SearchTabPagerFragment fragment = SearchTabPagerFragment.newInstance(0, query);
        ft.replace(R.id.search_frame, fragment).commitAllowingStateLoss();

        return true;
    }


    @Override
    public boolean onQueryTextChange(final String newText) {

        if (newText.equals(queryString)) {
            return true;
        }
        queryString = newText;
        if (!queryString.trim().equals("")) {

        } else {

        }



        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideInputManager();
        return false;
    }

    public void hideInputManager() {
        if (mSearchView != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();

            SearchHistory.getInstance(this).addSearchString(mSearchView.getQuery().toString());
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onSearch(String t) {
        mSearchView.setQuery(t, true);
    }
}
