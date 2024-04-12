package org.acme.projectjobschedule.domain.solver;

import ai.timefold.solver.core.api.domain.entity.PinningFilter;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.Schedule;

public class NotSourceOrSinkAllocationFilter implements PinningFilter<Schedule, Allocation> {

    @Override
    public boolean accept(Schedule schedule, Allocation allocation) {
        JobType jobType = allocation.getJob().getJobType();
        return jobType == JobType.SOURCE || jobType == JobType.SINK;
    }

}
