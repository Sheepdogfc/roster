package org.acme.meetingschedule.domain;

public class PreferredAttendance extends Attendance {

    public PreferredAttendance() {
    }

    public PreferredAttendance(String id, Meeting meeting) {
        super(id, meeting);
    }
}
