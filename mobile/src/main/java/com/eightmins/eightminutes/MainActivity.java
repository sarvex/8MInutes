package com.eightmins.eightminutes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.eightmins.eightminutes.advocate.dash.DashFragment;
import com.eightmins.eightminutes.advocate.dash.DashFragment.OnFragmentInteractionListener;
import com.eightmins.eightminutes.advocate.refer.ReferralFragment;
import com.eightmins.eightminutes.advocate.team.AddActivity;
import com.eightmins.eightminutes.advocate.team.MemberFragment;
import com.eightmins.eightminutes.advocate.video.VideoFragment;
import com.eightmins.eightminutes.login.LoginActivity;
import com.eightmins.eightminutes.login.ProfileActivity;
import com.eightmins.eightminutes.utility.PagerAdapter;
import com.eightmins.eightminutes.utility.Utils;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeader.OnAccountHeaderListener;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.parse.LogOutCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;

public class MainActivity extends AppCompatActivity implements ReferralFragment.OnFragmentInteractionListener,
    MemberFragment.OnFragmentInteractionListener, VideoFragment.OnFragmentInteractionListener,
    OnFragmentInteractionListener {

  private static final int ADD_REFERRAL = 12834;
  private static final int ADD_MEMBER = 12835;
  private static final String HOME = "Home";
  private static final String REFERRALS = "Referrals";
  private static final String TEAM = "Team";
  private static final String VIDEOS = "Videos";

  @Bind(R.id.toolbar) Toolbar toolbar;
  @Bind(R.id.viewpager) ViewPager viewPager;
  @Bind(R.id.tabs) TabLayout tabLayout;
  @Bind(R.id.add_button) FloatingActionButton addButton;

  private AccountHeader accountHeader;
  private Drawer drawer;
  private ShareActionProvider shareActionProvider;
  private ProgressDialog progress;
  private DashFragment dashFragment;
  private ReferralFragment referralFragment;
  private MemberFragment memberFragment;
  private VideoFragment videoFragment;
  private OnFilterChangedListener onFilterChangedListener;

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);

    Icepick.restoreInstanceState(this, savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    setupToolbar();

    setupDrawer();

    setupViewPager();

    setupCollapsingToolbar();

    ParseAnalytics.trackAppOpenedInBackground(getIntent());
    if (ParseUser.getCurrentUser() == null) {
      toLoginActivity();
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowTitleEnabled(false);
    }
  }

  private void setupDrawer() {

    String username = "";
    String email = "";

    if (ParseUser.getCurrentUser() != null) {
      username = ParseUser.getCurrentUser().getUsername();
      email = ParseUser.getCurrentUser().getEmail();
    }

    accountHeader = new AccountHeaderBuilder()
        .withActivity(this)
        .withHeaderBackground(R.drawable.header)
        .addProfiles(new ProfileDrawerItem().withName(username).withEmail(email))
        .withOnAccountHeaderListener(new OnAccountHeaderListener() {
          @Override
          public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
            return false;
          }
        })
        .build();

    drawer = new DrawerBuilder()
        .withActivity(this)
        .withToolbar(toolbar)
        .withAccountHeader(accountHeader)
        .withHeader(R.layout.header)
        .addDrawerItems(
            new PrimaryDrawerItem().withName("Order T-Shirts").withIcon(Icon.gmd_local_florist),
            new PrimaryDrawerItem().withName("Profile").withIcon(Icon.gmd_person)
                .withOnDrawerItemClickListener(new OnDrawerItemClickListener() {
                  @Override
                  public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    toProfileActivity();
                    return true;
                  }
                }),
            new PrimaryDrawerItem().withName("Settings").withIcon(Icon.gmd_settings),
            new PrimaryDrawerItem().withName("Logout").withIcon(FontAwesome.Icon.faw_sign_out)
                .withOnDrawerItemClickListener(new OnDrawerItemClickListener() {
                  @Override
                  public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    if (ParseUser.getCurrentUser() != null) {
                      Utils.showProgressBar(getParent(), progress, getString(R.string.logging_out));
                      ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                          Utils.hideProgressBar(progress);
                          if (e == null) {
                            toLoginActivity();
                          }
                        }
                      });
                    }
                    return true;
                  }
                }))
        .build();

    drawer.getRecyclerView().setVerticalScrollBarEnabled(false);
  }

  private void setupViewPager() {
    if (viewPager != null) {
      PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());

      dashFragment = new DashFragment();
      adapter.addFragment(dashFragment, MainActivity.HOME);
      referralFragment = new ReferralFragment();
      adapter.addFragment(referralFragment, MainActivity.REFERRALS);
      memberFragment = new MemberFragment();
      adapter.addFragment(memberFragment, MainActivity.TEAM);
      videoFragment = new VideoFragment();
      adapter.addFragment(videoFragment, MainActivity.VIDEOS);
      viewPager.setAdapter(adapter);
    }

    tabLayout.setupWithViewPager(viewPager);
    tabLayout.setOnTabSelectedListener(new ViewPagerOnTabSelectedListener(viewPager) {
      @Override
      public void onTabSelected(Tab tab) {
        super.onTabSelected(tab);
        switch (tab.getText().toString()) {
          case MainActivity.HOME:
            addButton.hide();
            break;
          case MainActivity.REFERRALS:
            addButton.show();
            break;
          case MainActivity.TEAM:
            addButton.show();
            break;
          case MainActivity.VIDEOS:
            addButton.hide();
            break;
        }
      }

      @Override
      public void onTabUnselected(Tab tab) {
        super.onTabUnselected(tab);
      }

      @Override
      public void onTabReselected(Tab tab) {
        super.onTabReselected(tab);
      }
    });
  }

  private void setupCollapsingToolbar() {
    CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);

    collapsingToolbar.setTitleEnabled(false);
    collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
    collapsingToolbar.setContentScrimColor(getResources().getColor(R.color.primary));
    collapsingToolbar.setStatusBarScrimColor(getResources().getColor(R.color.primary));
  }

  protected void toLoginActivity() {
    startActivity(new Intent(this, LoginActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
  }

  private void toProfileActivity() {
    startActivity(new Intent(this, ProfileActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
  }

  @OnClick(R.id.add_button)
  public void onAddButtonClicked(View view) {
    switch (viewPager.getCurrentItem()) {
      case 1:
        startActivityForResult(new Intent(this, AddActivity.class), MainActivity.ADD_REFERRAL);
        break;
      case 2:
        startActivityForResult(new Intent(this, AddActivity.class), MainActivity.ADD_MEMBER);
        break;
      default:
        break;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == MainActivity.ADD_REFERRAL && resultCode == Activity.RESULT_OK) {
      referralFragment.load();
      // deal with the item yourself
    }

    if (requestCode == MainActivity.ADD_MEMBER && resultCode == Activity.RESULT_OK) {
      memberFragment.load();
      // deal with the item yourself
    }

  }

  public void setOnFilterChangedListener(OnFilterChangedListener onFilterChangedListener) {
    this.onFilterChangedListener = onFilterChangedListener;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);

    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    MenuItem searchItem = menu.findItem(R.id.action_search);
    SearchView searchView = null;
    if (searchItem != null) {
      searchView = (SearchView) searchItem.getActionView();
    }
    if (searchView != null) {
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    MenuItem shareItem = menu.findItem(R.id.action_share);
    shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
    shareActionProvider.setShareIntent(doShare());
    return super.onCreateOptionsMenu(menu);
  }

  private Intent doShare() {
    return null;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    boolean result = false;

    switch (id) {
      case R.id.action_settings:
        result = true;
        break;
      default:
        break;
    }

    return result || super.onOptionsItemSelected(item);
  }

  @Override
  public void onFragmentInteraction(Uri uri) {

  }

  public interface OnFilterChangedListener {
    void onFilterChanged(int filter);
  }
}
