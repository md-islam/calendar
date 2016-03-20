package io.github.hidroh.calendar;

import android.app.Dialog;
import android.content.Intent;
import android.content.ShadowAsyncQueryHandler;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.content.ShadowContentResolverCompatJellybean;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboCursor;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.util.ActivityController;

import java.util.Arrays;
import java.util.List;

import io.github.hidroh.calendar.content.CalendarCursor;
import io.github.hidroh.calendar.widget.EventEditView;

import static junit.framework.Assert.assertNotNull;
import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@SuppressWarnings("ConstantConditions")
@Config(shadows = {ShadowContentResolverCompatJellybean.class, ShadowAsyncQueryHandler.class})
@RunWith(RobolectricGradleTestRunner.class)
public class EditActivityTest {
    private ActivityController<EditActivity> controller;
    private EditActivity activity;

    @Before
    public void setUp() {
        RoboCursor cursor = new TestRoboCursor();
        cursor.setResults(new Object[][]{
                new Object[]{1L, "My Calendar"},
                new Object[]{2L, "Birthdays"},
        });
        shadowOf(ShadowApplication.getInstance().getContentResolver())
                .setCursor(CalendarContract.Calendars.CONTENT_URI, cursor);
        controller = Robolectric.buildActivity(EditActivity.class);
        activity = controller.get();
    }

    @Test
    public void testCreateNewEvent() {
        controller.create().start().resume().visible();
        assertThat(activity).hasTitle(R.string.create_event);
    }

    @Config(qualifiers = "w820dp-land")
    @Test
    public void testCreateNewEventLandscape() {
        controller.create().start().resume().visible();
        assertThat((TextView) activity.findViewById(R.id.form_title))
                .hasTextString(R.string.create_event);
    }

    @Test
    public void testEditEvent() {
        controller.withIntent(new Intent()
                .putExtra(EditActivity.EXTRA_EVENT, new EventEditView.Event.Builder()
                        .id(1L)
                        .calendarId(1L)
                        .title("title")
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .allDay(false)
                        .build()))
                .create().start().resume().visible();
        assertThat(activity).hasTitle(R.string.edit_event);
    }

    @Config(qualifiers = "w820dp-land")
    @Test
    public void testEditEventLandscape() {
        controller.withIntent(new Intent()
                .putExtra(EditActivity.EXTRA_EVENT, new EventEditView.Event.Builder()
                        .id(1L)
                        .calendarId(1L)
                        .title("title")
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .allDay(false)
                        .build()))
                .create().start().resume().visible();
        assertThat((TextView) activity.findViewById(R.id.form_title))
                .hasTextString(R.string.edit_event);
    }

    @Test
    public void testInsertMissingTitle() {
        controller.create().start().resume().visible();

        // assume that user has set event details via UI controls
        ((EventEditView) activity.findViewById(R.id.event_edit_view))
                .setEvent(new EventEditView.Event.Builder()
                        .calendarId(1L)
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .allDay(false)
                        .build());

        // saving event without title should not generate an insert statement
        shadowOf(activity).clickMenuItem(R.id.action_save);
        assertThat(shadowOf(ShadowApplication.getInstance()
                .getContentResolver())
                .getInsertStatements())
                .isEmpty();
        assertThat(activity).isNotFinishing();
    }

    @Test
    public void testInsertMissingCalendar() {
        controller.create().start().resume().visible();

        // assume that user has set event details via UI controls
        ((EventEditView) activity.findViewById(R.id.event_edit_view))
                .setEvent(new EventEditView.Event.Builder()
                        .title("title")
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .allDay(false)
                        .build());

        // saving event without calendar chosen should not generate an insert statement
        shadowOf(activity).clickMenuItem(R.id.action_save);
        assertThat(shadowOf(ShadowApplication.getInstance()
                .getContentResolver())
                .getInsertStatements())
                .isEmpty();
        assertThat(activity).isNotFinishing();
    }

    @Test
    public void testInsert() {
        controller.create().start().resume().visible();

        // assume that user has set event details via UI controls
        ((EventEditView) activity.findViewById(R.id.event_edit_view))
                .setEvent(new EventEditView.Event.Builder()
                        .calendarId(1L)
                        .title("title")
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .allDay(false)
                        .build());

        // saving new event should generate an insert statement
        shadowOf(activity).clickMenuItem(R.id.action_save);
        assertThat(shadowOf(ShadowApplication.getInstance()
                .getContentResolver())
                .getInsertStatements())
                .hasSize(1);
        assertThat(activity).isFinishing();
    }

    @Test
    public void testUpdate() {
        controller.create().start().resume().visible();

        // assume that user has set event details via UI controls
        ((EventEditView) activity.findViewById(R.id.event_edit_view))
                .setEvent(new EventEditView.Event.Builder()
                        .id(1L)
                        .calendarId(1L)
                        .title("title")
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .allDay(false)
                        .build());

        // saving new event should generate an update statement
        shadowOf(activity).clickMenuItem(R.id.action_save);
        assertThat(shadowOf(ShadowApplication.getInstance()
                .getContentResolver())
                .getUpdateStatements())
                .hasSize(1);
        assertThat(activity).isFinishing();
    }

    @Test
    public void testDelete() {
        controller.withIntent(new Intent()
                .putExtra(EditActivity.EXTRA_EVENT, new EventEditView.Event.Builder()
                        .id(1L)
                        .calendarId(1L)
                        .title("title")
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .allDay(false)
                        .build()))
                .create().start().resume().visible();

        // deleting event should generate a delete statement
        shadowOf(activity).clickMenuItem(R.id.action_delete);
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        shadowOf(dialog).clickOn(android.R.id.button1); // BUTTON_POSITIVE
        assertThat(shadowOf(ShadowApplication.getInstance()
                .getContentResolver())
                .getDeleteStatements())
                .hasSize(1);
        assertThat(activity).isFinishing();
    }

    @Test
    public void testPressBack() {
        controller.create().start().resume().visible();

        // clicking back but not confirming discard changes should not quit
        shadowOf(activity).clickMenuItem(android.R.id.home);
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        shadowOf(dialog).clickOn(android.R.id.button2); // BUTTON_NEGATIVE
        assertThat(dialog).isNotShowing();
        assertThat(activity).isNotFinishing();

        // clicking back and confirming discard changes should quit
        shadowOf(activity).clickMenuItem(android.R.id.home);
        shadowOf(ShadowDialog.getLatestDialog()).clickOn(android.R.id.button1); // BUTTON_POSITIVE
        assertThat(activity).isFinishing();
    }

    @Test
    public void testStateRestoration() {
        controller.create().start().resume().visible();

        // assume that user has set event details via UI controls
        ((EventEditView) activity.findViewById(R.id.event_edit_view))
                .setEvent(new EventEditView.Event.Builder()
                        .title("title")
                        .start(CalendarUtils.today())
                        .end(CalendarUtils.today())
                        .build());

        shadowOf(activity).recreate();
        assertThat(((EventEditView) activity.findViewById(R.id.event_edit_view))
                .getEvent().getTitle()).contains("title");
    }

    @Test
    public void testCreateLocalCalendar() {
        shadowOf(ShadowApplication.getInstance().getContentResolver())
                .setCursor(CalendarContract.Calendars.CONTENT_URI, new TestRoboCursor());
        controller.create().start().resume().visible();
        List<ShadowContentResolver.InsertStatement> inserts =
                shadowOf(ShadowApplication.getInstance()
                        .getContentResolver())
                        .getInsertStatements();
        assertThat(inserts).hasSize(1);
        assertThat(inserts.get(0).getUri().toString())
                .contains(CalendarContract.Calendars.CONTENT_URI.toString());
    }
    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }

    static class TestRoboCursor extends RoboCursor {
        public TestRoboCursor() {
            setColumnNames(Arrays.asList(CalendarCursor.PROJECTION));
        }

        @Override
        public void registerContentObserver(ContentObserver observer) {
            // no op
        }

        @Override
        public void unregisterContentObserver(ContentObserver observer) {
            // no op
        }

        @Override
        public void setExtras(Bundle extras) {
            // no op
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }
}