package com.example.healthclubapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DatasourceFragment()).commit();
            navigationView.setCheckedItem((R.id.nav_datasources));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_datasources) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DatasourceFragment()).commit();
        } else if (id == R.id.nav_sync) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SyncFragment()).commit();
        } else if (id == R.id.nav_daily) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DailyFragment()).commit();
        } else if (id == R.id.nav_cdc_info) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CDCInfoFragment()).commit();
        } else if (id == R.id.nav_benchmark) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BenchmarkFragment()).commit();
        } else if (id == R.id.nav_recommendation) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RecommendationFragment()).commit();
        } else if (id == R.id.nav_incentive) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new IncentiveFragment()).commit();
        } else if (id == R.id.nav_exercise) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExerciseFragment()).commit();
        } else if (id == R.id.nav_food) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DietFragment()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
