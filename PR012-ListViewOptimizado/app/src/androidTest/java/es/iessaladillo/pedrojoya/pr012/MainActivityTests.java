package es.iessaladillo.pedrojoya.pr012;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTests {

    @SuppressWarnings({"unchecked", "CanBeFinal"})
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void validateAlumnoAdded() {
        onData(allOf(is(instanceOf(Alumno.class)), withNombreAlumno("Baldomero LLégate Ligero")))
                .inAdapterView(withId(R.id.lstAlumnos)).onChildView(withId(R.id.btnLlamar)).perform(click());
        // Se espera que desaparezca el Toast (para enlazar tests).
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //onView(withId(R.id.lstAlumnos)).check(matches(withAdaptedData(withNombreAlumno("Baldomero LLégate Ligero"))));
    }

    @Test
    public void validateNombreStillOnAdapterAfterRotation() {
        onData(allOf(is(instanceOf(Alumno.class)), withNombreAlumno("Baldomero LLégate Ligero")))
                .inAdapterView(withId(R.id.lstAlumnos)).perform(click());
        //onView(withId(R.id.lstAlumnos)).check(matches(withAdaptedData(withNombreAlumno("Baldomero LLégate Ligero"))));
        rotateScreen();
        onData(allOf(is(instanceOf(Alumno.class)), withNombreAlumno("Baldomero LLégate Ligero")))
                .inAdapterView(withId(R.id.lstAlumnos)).perform(click());
        //onView(withId(R.id.lstAlumnos)).check(matches(withAdaptedData(withNombreAlumno("Baldomero LLégate Ligero"))));
        // Se espera que desaparezca el Toast (para enlazar tests).
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void validateBtnLlamar() {
        onData(allOf(is(instanceOf(Alumno.class)), withNombreAlumno("Baldomero LLégate Ligero")))
                .inAdapterView(withId(R.id.lstAlumnos)).onChildView(withId(R.id.btnLlamar)).perform(click());
        //onView(withId(R.id.lstAlumnos)).check(matches(withAdaptedData(withNombreAlumno("Baldomero LLégate Ligero"))));
        onView(withText("Llamar a Baldomero LLégate Ligero"))
                .inRoot(withDecorView(not(mActivityRule.getActivity().getWindow()
                        .getDecorView()))).check(matches(isDisplayed()));
        // Se espera que desaparezca el Toast (para enlazar tests).
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void validateBtnNotas() {
        onData(allOf(is(instanceOf(Alumno.class)), withNombreAlumno("Baldomero LLégate Ligero")))
                .inAdapterView(withId(R.id.lstAlumnos)).onChildView(withId(R.id.btnNotas)).perform(click());
        //onView(withId(R.id.lstAlumnos)).check(matches(withAdaptedData(withNombreAlumno("Baldomero LLégate Ligero"))));
        onView(withText("Ver notas de Baldomero LLégate Ligero"))
                .inRoot(withDecorView(not(mActivityRule.getActivity().getWindow()
                        .getDecorView()))).check(matches(isDisplayed()));
        // Se espera que desaparezca el Toast (para enlazar tests).
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    private static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("with class name: ");
                dataMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof AdapterView)) {
                    return false;
                }
                @SuppressWarnings("rawtypes")
                Adapter adapter = ((AdapterView) view).getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (dataMatcher.matches(adapter.getItem(i))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static Matcher<Object> withNombreAlumno(final String nombreAlumno) {
        return new BoundedMatcher<Object, Alumno>(Alumno.class) {
            @Override
            protected boolean matchesSafely(Alumno alumno) {
                return nombreAlumno.equals(alumno.getNombre());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with student name: " + nombreAlumno);
            }
        };
    }

    // Rota la pantalla.
    private void rotateScreen() {
        Context context = InstrumentationRegistry.getTargetContext();
        int orientation
                = context.getResources().getConfiguration().orientation;
        Activity activity = mActivityRule.getActivity();
        activity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


}
