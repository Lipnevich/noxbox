package by.nicolay.lipnevich.noxbox.pages;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import by.nicolay.lipnevich.noxbox.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AuthPageTest {

    @Rule
    public ActivityTestRule<AuthPage> mActivityTestRule = new ActivityTestRule<>(AuthPage.class);

    @Test
    public void authPageTest() throws Exception {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000);
        onView(withId(R.id.checkbox)).check(matches(not(isChecked())));
        onView(withId(R.id.googleAuth)).check(matches(not(isEnabled())));

        ViewInteraction appCompatCheckBox = onView(
allOf(withId(R.id.checkbox),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
5),
0),
isDisplayed()));
        appCompatCheckBox.perform(click());
        
        ViewInteraction appCompatCheckBox2 = onView(
allOf(withId(R.id.checkbox),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
5),
0),
isDisplayed()));
        appCompatCheckBox2.perform(click());
        
        ViewInteraction appCompatCheckBox3 = onView(
allOf(withId(R.id.checkbox),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
5),
0),
isDisplayed()));
        appCompatCheckBox3.perform(click());
        
        ViewInteraction appCompatCheckBox4 = onView(
allOf(withId(R.id.checkbox),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
5),
0),
isDisplayed()));
        appCompatCheckBox4.perform(click());
        
         // Added a sleep statement to match the app's execution delay.
 // The recommended way to handle such scenarios is to use Espresso idling resources:
  // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
try {
 Thread.sleep(60000);
 } catch (InterruptedException e) {
 e.printStackTrace();
 }
        
        ViewInteraction appCompatCheckBox5 = onView(
allOf(withId(R.id.checkbox),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
5),
0),
isDisplayed()));
        appCompatCheckBox5.perform(click());
        
        ViewInteraction appCompatButton = onView(
allOf(withId(R.id.googleAuth), withText("Sign in with Google"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
3),
isDisplayed()));
        appCompatButton.perform(click());
        
         // Added a sleep statement to match the app's execution delay.
 // The recommended way to handle such scenarios is to use Espresso idling resources:
  // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
try {
 Thread.sleep(3599944);
 } catch (InterruptedException e) {
 e.printStackTrace();
 }
        
        ViewInteraction appCompatImageView = onView(
allOf(withId(R.id.menu),
childAtPosition(
allOf(withId(R.id.mapId),
childAtPosition(
withId(R.id.map_layout),
0)),
8),
isDisplayed()));
        appCompatImageView.perform(click());
        
        ViewInteraction recyclerView = onView(
allOf(withId(R.id.material_drawer_recycler_view),
childAtPosition(
withId(R.id.material_drawer_slider_layout),
1)));
        recyclerView.perform(actionOnItemAtPosition(3, click()));
        
        ViewInteraction appCompatButton2 = onView(
allOf(withId(android.R.id.button1), withText("Logout"),
childAtPosition(
allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
3)),
3),
isDisplayed()));
        appCompatButton2.perform(click());
        
        }

        private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }
    }
