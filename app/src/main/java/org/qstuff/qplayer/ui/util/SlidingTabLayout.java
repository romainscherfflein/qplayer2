/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qstuff.qplayer.ui.util;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.qstuff.qplayer.R;


/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this layout is being used for.
 * <p>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 *
 * providing the layout ID of your custom layout.
 */
public class SlidingTabLayout extends HorizontalScrollView {

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

    }

    private static final int TITLE_OFFSET_DIPS                = 0;

    private static final int TAB_VIEW_PADDING_BOTTOM_DIPS     = 10;
    private static final int TAB_VIEW_PADDING_TOP_DIPS        = 5;

    private static final int TAB_VIEW_PADDING_LEFT_RIGHT_DIPS = 0;
    
    private static final int TAB_VIEW_MARGIN_RIGHT_DIPS       = 10;
    private static final int TAB_VIEW_MARGIN_LEFT_DIPS        = 10;

    private static final int TAB_VIEW_MARGIN_BOttom_DIPS      = 10;

    private int mTitleOffset;
    private int numTabs;
    
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private final SlidingTabStrip mTabStrip;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);

        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);
        setBackgroundColor(getResources().getColor(R.color.white));
        
        mTabStrip = new SlidingTabStrip(context);

        FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
       
        
        addView(mTabStrip, layoutParams);
        mTabStrip.bringToFront();
    }

    /**
     * Set the custom {@link TabColorizer} to be used.
     *
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        mTabStrip.setDividerColors(colors);
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }

        // initial selection
        updateTextViewStates(0);
    }

    /**
     * Create a default view to be used for tabs.
     */
    protected TextView createDefaultTabView(Context context) {

        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_version_title)
            / getResources().getDisplayMetrics().density);

        textView.setTextColor(getResources().getColorStateList(R.color.white));
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.black_rounded_top_shape));
        
        int paddingBottom = (int) (TAB_VIEW_PADDING_BOTTOM_DIPS 
            * getResources().getDisplayMetrics().density);
        int paddingTop = (int) (TAB_VIEW_PADDING_TOP_DIPS
            * getResources().getDisplayMetrics().density);
        int paddingLeftRight = (int) (TAB_VIEW_PADDING_LEFT_RIGHT_DIPS 
            * getResources().getDisplayMetrics().density);

        textView.setPadding(paddingLeftRight, paddingTop,
            paddingLeftRight, paddingBottom);
        
        return textView;
    }

    private void populateTabStrip() {
        
        final PagerAdapter adapter = mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();

        numTabs = adapter.getCount();
        for (int i = 0; i < numTabs; i++) {
            
            TextView tabTitleView = createDefaultTabView(getContext());

            tabTitleView.setText(adapter.getPageTitle(i));
            tabTitleView.setOnClickListener(tabClickListener);

            // Make all tabs have the same width
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            
            if (i == 1)
                layoutParams.setMargins(TAB_VIEW_MARGIN_LEFT_DIPS, 0,
                    TAB_VIEW_MARGIN_RIGHT_DIPS, 0);
            else
                layoutParams.setMargins(0, 0, 0, 0);

            tabTitleView.setText(adapter.getPageTitle(i));
            tabTitleView.setOnClickListener(tabClickListener);

            mTabStrip.addView(tabTitleView, layoutParams);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();

        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        
        if (selectedChild != null) {
        
            int targetScrollX = selectedChild.getLeft() + positionOffset;
            if (tabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth()) : 0;
            scrollToTab(position, extraOffset);
            
            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
            
            setSelectedTabTextColor(position);
        }

        private void setSelectedTabTextColor(int position) {
            
            for (int i = 0; i < numTabs; i++) {
                TextView text = (TextView) mTabStrip.getChildAt(i);

                if (i == position) {
                    text.setTextColor(getResources().getColor(R.color.q_orange));                            
                } else {
                    text.setTextColor(getResources().getColor(R.color.white));
                }
            }
        }
        
        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {

            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }

            updateTextViewStates(position);
        }
    }

    private class TabClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {

                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

    private void updateTextViewStates(int position) {

        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            View child = mTabStrip.getChildAt(i);

            if (i == position)
                child.setSelected(true);
            else
                child.setSelected(false);
        }
    }
}