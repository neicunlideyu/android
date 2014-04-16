/*
 * Copyright (C) 2007 The Android Open Source Project
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

package cn.onboard.android.app.widget.calendar;

import android.provider.CalendarContract.Attendees;
import android.text.format.DateUtils;

// TODO: should Event be Parcelable so it can be passed via Intents?
public class CalendarEvent implements Cloneable {

    /**
     * The sort order is:
     * 1) events with an earlier start (begin for normal events, startday for allday)
     * 2) events with a later end (end for normal events, endday for allday)
     * 3) the title (unnecessary, but nice)
     *
     * The start and end day is sorted first so that all day events are
     * sorted correctly with respect to events that are >24 hours (and
     * therefore show up in the allday area).
     */

    private long id;
    public int color;
    public CharSequence title;
    private CharSequence location;
    public boolean allDay;
    private String organizer;
    private boolean guestsCanModify;

    public int startDay;       // start Julian day
    public int endDay;         // end Julian day
    public int startTime;      // Start and end time are in minutes since midnight
    public int endTime;

    public long startMillis;   // UTC milliseconds since the epoch
    public long endMillis;     // UTC milliseconds since the epoch
    private int mColumn;
    private int mMaxColumns;

    private boolean hasAlarm;
    private boolean isRepeating;

    public int selfAttendeeStatus;



    @Override
    public final Object clone() throws CloneNotSupportedException {
        super.clone();
        CalendarEvent e = new CalendarEvent();

        e.title = title;
        e.color = color;
        e.location = location;
        e.allDay = allDay;
        e.startDay = startDay;
        e.endDay = endDay;
        e.startTime = startTime;
        e.endTime = endTime;
        e.startMillis = startMillis;
        e.endMillis = endMillis;
        e.hasAlarm = hasAlarm;
        e.isRepeating = isRepeating;
        e.selfAttendeeStatus = selfAttendeeStatus;
        e.organizer = organizer;
        e.guestsCanModify = guestsCanModify;

        return e;
    }

    public final void copyTo(CalendarEvent dest) {
        dest.id = id;
        dest.title = title;
        dest.color = color;
        dest.location = location;
        dest.allDay = allDay;
        dest.startDay = startDay;
        dest.endDay = endDay;
        dest.startTime = startTime;
        dest.endTime = endTime;
        dest.startMillis = startMillis;
        dest.endMillis = endMillis;
        dest.hasAlarm = hasAlarm;
        dest.isRepeating = isRepeating;
        dest.selfAttendeeStatus = selfAttendeeStatus;
        dest.organizer = organizer;
        dest.guestsCanModify = guestsCanModify;
    }

    public static final CalendarEvent newInstance() {
        CalendarEvent e = new CalendarEvent();

        e.id = 0;
        e.title = null;
        e.color = 0;
        e.location = null;
        e.allDay = false;
        e.startDay = 0;
        e.endDay = 0;
        e.startTime = 0;
        e.endTime = 0;
        e.startMillis = 0;
        e.endMillis = 0;
        e.hasAlarm = false;
        e.isRepeating = false;
        e.selfAttendeeStatus = Attendees.ATTENDEE_STATUS_NONE;

        return e;
    }



    public String getTitleAndLocation() {
        String text = title.toString();

        // Append the location to the title, unless the title ends with the
        // location (for example, "meeting in building 42" ends with the
        // location).
        if (location != null) {
            String locationString = location.toString();
            if (!text.endsWith(locationString)) {
                text += ", " + locationString;
            }
        }
        return text;
    }

    public void setColumn(int column) {
        mColumn = column;
    }

    public int getColumn() {
        return mColumn;
    }

    public void setMaxColumns(int maxColumns) {
        mMaxColumns = maxColumns;
    }

    public int getMaxColumns() {
        return mMaxColumns;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public boolean drawAsAllday() {
        // Use >= so we'll pick up Exchange allday events
        return allDay || endMillis - startMillis >= DateUtils.DAY_IN_MILLIS;
    }
}
