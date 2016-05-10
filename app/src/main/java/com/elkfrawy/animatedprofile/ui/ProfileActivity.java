package com.elkfrawy.animatedprofile.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import com.elkfrawy.animatedprofile.R;
import com.elkfrawy.animatedprofile.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    private static final String LOG_TAG = "FRIEND_PROFILE";
    PagerAdapter mPagerAdapter;
    TextView mToolbarTitle;
    Toolbar mToolbarView;
    TextView mProfileUsernameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbarView = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbarView);
        mToolbarTitle = (TextView) mToolbarView.findViewById(R.id.toolbar_title);

        mToolbarView.bringToFront();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        List<Fragment> fragments = getFragments();
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        VerticalViewPager pager = (VerticalViewPager) findViewById(R.id.pager);
        pager.setAdapter(mPagerAdapter);
        pager.setPageTransformer(false, this);

        changeBlurredBackground(BitmapFactory.decodeResource(getResources(), R.drawable.profile_background), 2f);
    }


    private void changeBlurredBackground(Bitmap image, float blurRange) {
        Bitmap blurredImage = (Utility.blur(this, image, 1.0f, blurRange));
        findViewById(R.id.profile_background).setBackgroundDrawable(new BitmapDrawable(getResources(), blurredImage));
    }

    public List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new ProfileCoverFragment());
        fragments.add(new ProfileDetailsFragment());
        return fragments;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Save last blur for performance
    float lastBlur = 2;
    // Variable used for animation
    float initialNameSize;
    float initialNameY;
    float initialNameX;
    int initialBlur = 2;
    int maximumBlur = 7;

    boolean setCoordinates = false;
    @Override
    public void transformPage(View page, float position) {
        float blur = 0;
        if (position <= -1.0F || position >= 1.0F) { // Page is out of view (too up or too down)
            if (page.getTag() == HEADER_PAGE_TAG)
                blur = maximumBlur;
        } else if (position == 0.0F) { // Page is the front page now
            if (page.getTag() == HEADER_PAGE_TAG)
                blur = initialBlur;
            resetPage(page); // make sure everything back to its initial state
        } else { // Page is in transition (from up to down or from down to up)
            if (page.getTag() == HEADER_PAGE_TAG)
                blur = initialBlur + Math.abs(position) * (maximumBlur - initialBlur);
            animatePage(page, position);
        }
        // Change blur only when difference is 1 for performance
        if (Math.abs(lastBlur - blur) > 1 && page.getTag() == HEADER_PAGE_TAG) {
            changeBlurredBackground(BitmapFactory.decodeResource(getResources(), R.drawable.profile_background), blur);
            lastBlur = blur;
        }
    }

    private void animatePage(View page, float position) {
        float alpha = 1.0F - Math.abs(position); // 1 if page in middle and 0 if out of view
        if (page.getTag() == HEADER_PAGE_TAG) { // Animate the header page
            ///////// STEP 1:  Fade all but Person name //////////
            ViewCompat.setAlpha(page.findViewById(R.id.tvPersonality), alpha);
            ViewCompat.setAlpha(page.findViewById(R.id.tvPersonJob), alpha);
            ViewCompat.setAlpha(page.findViewById(R.id.imgHeadImage), alpha*alpha); // alpha*alpha to accelerate fading
            ViewCompat.setAlpha(page.findViewById(R.id.butConnect), alpha*alpha);

            ////////  STEP 2: Change person name textview text size /////////////
            float textSize, personNameRation;
            // Start username animation after 25% of its animation distance (0.75 = 1 - 0.25)
            if (getYOf(mProfileUsernameTV) <= initialNameY * .75f) {
                personNameRation = (getYOf(mProfileUsernameTV) - getYOf(mToolbarTitle))/(initialNameY * .75f- getYOf(mToolbarTitle));
                textSize = mToolbarTitle.getTextSize() + (initialNameSize - mToolbarTitle.getTextSize()) * Math.max(Math.min(personNameRation, 1), 0);
            } else {
                textSize = initialNameSize;
                personNameRation = 1;
            }

            int width = mProfileUsernameTV.getWidth();
            int height = mProfileUsernameTV.getHeight();
            // The weird setTextSize default is sp while getTextSize returns px!
            mProfileUsernameTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.min(initialNameSize, textSize));
            mProfileUsernameTV.setWidth(width); // Maintain the same width to prevent flicker
            mProfileUsernameTV.setHeight(height); // Maintain the same height to prevent flicker

//            ViewCompat.setAlpha(page, 1);
            ////////// STEP 3: Set Person name X location ////////////
            float newNameX = initialNameX + (ViewCompat.getX(mToolbarTitle) - initialNameX) * (1-personNameRation);
            ViewCompat.setX(mProfileUsernameTV, newNameX);

            ///////// STEP 4: Show/Hide toolbar title and username //////////
            if (getYOf(mProfileUsernameTV) <= getYOf(mToolbarTitle)) {
                mProfileUsernameTV.setVisibility(View.INVISIBLE);
                mToolbarTitle.setVisibility(View.VISIBLE);
            } else {
                mProfileUsernameTV.setVisibility(View.VISIBLE);
                mToolbarTitle.setVisibility(View.INVISIBLE);
            }

            ////////// STEP 5: Change background, person name and back arrow color
            int colorGrad = (int) Utility.clamp(255 * personNameRation, 0, 255);
            mProfileUsernameTV.setTextColor(Color.rgb(colorGrad, colorGrad, colorGrad));
            // Change color of back arrow
            changeBackButtonColor(Color.rgb(colorGrad, colorGrad, colorGrad));
            // Change background overlay color
            colorGrad = (int) (255 * (1 - Utility.clamp(alpha, 0, 1)));
            findViewById(R.id.overlayLayout).setBackgroundColor(Color.argb(145, colorGrad, colorGrad, colorGrad));

            Log.w(LOG_TAG, "Set Size= " + textSize + "/" + initialNameSize + "Set X=" + newNameX + "/" + initialNameX);

        } else // if not header page, just fade it
            ViewCompat.setAlpha(page, alpha);
    }

    private void resetPage(View page) {
        ViewCompat.setAlpha(page, 1.0F);
        if (page.getTag() == HEADER_PAGE_TAG) {
            // Best chance I got to get initial coordinates
            if (!setCoordinates) {
                initialNameY = getYOf(mProfileUsernameTV);
                initialNameX = ViewCompat.getX(mProfileUsernameTV);
                setCoordinates = true;
            }

            // Make sure values are reset
            mProfileUsernameTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialNameSize);
            mProfileUsernameTV.setTextColor(Color.WHITE);
            ViewCompat.setX(mProfileUsernameTV, initialNameX);
            changeBackButtonColor(Color.WHITE);
        }
    }

    private void changeBackButtonColor(int color) {
        // Search for back button which would be ImageButton
        for (int i = 0; i < mToolbarView.getChildCount(); i++) {
            final View v = mToolbarView.getChildAt(i);
            if (v instanceof ImageButton) {
                ((ImageButton) v).getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }

        }
    }

    public void setProfileNameTV(TextView profileNameTV) {
        mProfileUsernameTV = profileNameTV;
        initialNameSize = profileNameTV.getTextSize();
    }

    private float getYOf(View view) {
        return Utility.getAbsoluteXY(view)[1];
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        List<Fragment> mFragmentList;

        public PagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.mFragmentList = fragmentList;
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
    }

    private static final String HEADER_PAGE_TAG = "HEADER";
    public static class ProfileCoverFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Add tag to the cover page to be able to identify it later
            View fragmentView = inflater.inflate(R.layout.fragment_profile_cover, container, false);
            fragmentView.setTag(HEADER_PAGE_TAG);
            ((ProfileActivity) getActivity()).setProfileNameTV((TextView) fragmentView.findViewById(R.id.tvPersonName));
            return fragmentView;
        }
    }

    public static class ProfileDetailsFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_profile_details, container, false);
        }
    }
}
