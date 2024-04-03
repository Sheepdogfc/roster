package org.acme.meetingschedule.domain;

public class RequiredAttendance extends Attendance {

    public RequiredAttendance() {
    }

    public RequiredAttendance(String id, Meeting meeting) {
        super(id, meeting);
    }

}
