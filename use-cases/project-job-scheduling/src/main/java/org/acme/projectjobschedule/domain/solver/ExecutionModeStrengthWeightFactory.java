package org.acme.projectjobschedule.domain.solver;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.domain.Schedule;
import org.acme.projectjobschedule.domain.resource.Resource;

public class ExecutionModeStrengthWeightFactory implements SelectionSorterWeightFactory<Schedule, ExecutionMode> {

    @Override
    public ExecutionModeStrengthWeight createSorterWeight(Schedule schedule, ExecutionMode executionMode) {
        Map<Resource, Integer> requirementTotalMap = new HashMap<>(
                executionMode.getResourceRequirementList().size());
        for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
            requirementTotalMap.put(resourceRequirement.getResource(), 0);
        }
        for (ResourceRequirement resourceRequirement : schedule.getResourceRequirements()) {
            Resource resource = resourceRequirement.getResource();
            Integer total = requirementTotalMap.get(resource);
            if (total != null) {
                total += resourceRequirement.getRequirement();
                requirementTotalMap.put(resource, total);
            }
        }
        double requirementDesirability = 0.0;
        for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
            Resource resource = resourceRequirement.getResource();
            int total = requirementTotalMap.get(resource);
            if (total > resource.getCapacity()) {
                requirementDesirability += (total - resource.getCapacity())
                        * (double) resourceRequirement.getRequirement()
                        * (resource.isRenewable() ? 1.0 : 100.0);
            }
        }
        return new ExecutionModeStrengthWeight(executionMode, requirementDesirability);
    }

    public static class ExecutionModeStrengthWeight implements Comparable<ExecutionModeStrengthWeight> {

        private static final Comparator<ExecutionModeStrengthWeight> COMPARATOR = comparingDouble(
                (ExecutionModeStrengthWeight weight) -> weight.requirementDesirability)
                .thenComparing(weight -> weight.executionMode, comparing(ExecutionMode::getId));

        private final ExecutionMode executionMode;
        private final double requirementDesirability;

        public ExecutionModeStrengthWeight(ExecutionMode executionMode, double requirementDesirability) {
            this.executionMode = executionMode;
            this.requirementDesirability = requirementDesirability;
        }

        @Override
        public int compareTo(ExecutionModeStrengthWeight other) {
            return COMPARATOR.compare(this, other);
        }
    }
}
