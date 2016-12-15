package org.qstuff.qplayer;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class QPlayerUITest1 {

    @Rule
    public ActivityTestRule<QPlayerMainActivity> mActivityTestRule = new ActivityTestRule<>(QPlayerMainActivity.class);

    @Test
    public void qPlayerUITest1() {
        ViewInteraction textView = onView(
            allOf(withText("filebrowser"), isDisplayed()));
        textView.perform(click());

        ViewInteraction textView2 = onView(
            allOf(withText("playlists"), isDisplayed()));
        textView2.perform(click());

        ViewInteraction imageButton = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_repeat),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_two)),
                isDisplayed()));
        imageButton.perform(click());

        ViewInteraction imageButton2 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_repeat),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_two)),
                isDisplayed()));
        imageButton2.perform(click());

        ViewInteraction imageButton3 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_repeat),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_two)),
                isDisplayed()));
        imageButton3.perform(click());

        ViewInteraction imageButton4 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_shuffle),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_two)),
                isDisplayed()));
        imageButton4.perform(click());

        ViewInteraction imageButton5 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_shuffle),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_two)),
                isDisplayed()));
        imageButton5.perform(click());

        ViewInteraction imageView = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_play),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_one)),
                isDisplayed()));
        imageView.perform(click());

        ViewInteraction imageView2 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_next),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_one)),
                isDisplayed()));
        imageView2.perform(click());

        ViewInteraction imageView3 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_previous),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_one)),
                isDisplayed()));
        imageView3.perform(click());

        ViewInteraction imageView4 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_play),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_one)),
                isDisplayed()));
        imageView4.perform(click());

        ViewInteraction imageView5 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_button_play),
                withParent(withId(org.qstuff.qplayer.R.id.player_buttons_row_one)),
                isDisplayed()));
        imageView5.perform(click());

        ViewInteraction textView3 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_text_dynamic_time), isDisplayed()));
        textView3.perform(click());

        ViewInteraction textView4 = onView(
            allOf(withId(org.qstuff.qplayer.R.id.player_text_dynamic_time), isDisplayed()));
        textView4.perform(click());

    }

}
