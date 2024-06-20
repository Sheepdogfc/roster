from timefold.solver.score import (constraint_provider, ConstraintFactory, Joiners, HardSoftScore)
from datetime import datetime, time, timedelta, date

from .domain import Employee, Shift


def get_minute_overlap(shift1: Shift, shift2: Shift) -> int:
    return (min(shift1.end, shift2.end) - max(shift1.start, shift2.start)).total_seconds() // 60


def overlapping_in_minutes(first_start_datetime: datetime, first_end_datetime: datetime,
                           second_start_datetime: datetime, second_end_datetime: datetime) -> int:
    latest_start = max(first_start_datetime, second_start_datetime)
    earliest_end = min(first_end_datetime, second_end_datetime)
    delta = (earliest_end - latest_start).total_seconds() / 60
    return max(0, delta)


def get_shift_overlapping_duration_in_minutes(shift: Shift, dates: set[date]) -> int:
    filtered_dates = [date for date in dates if shift.start.date() == date or (
        # The in check is ignored for a shift ends at midnight (00:00:00).
            shift.end.time() != datetime.min.time()
            and shift.end.date() == date)]
    overlap = 0
    for date in filtered_dates:
        start_date_time = datetime.combine(date, datetime.max.time())
        end_date_time = datetime.combine(date, datetime.min.time())
        overlap += overlapping_in_minutes(start_date_time, end_date_time, shift.start, shift.end)
    return overlap


@constraint_provider
def define_constraints(constraint_factory: ConstraintFactory):
    return [
        # Hard constraints
        required_skill(constraint_factory),
        no_overlapping_shifts(constraint_factory),
        at_least_10_hours_between_two_shifts(constraint_factory),
        one_shift_per_day(constraint_factory),
        unavailable_employee(constraint_factory),
        # Soft constraints
        undesired_day_for_employee(constraint_factory),
        desired_day_for_employee(constraint_factory),
    ]


def required_skill(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .filter(lambda shift: shift.required_skill not in shift.employee.skills)
            .penalize(HardSoftScore.ONE_HARD)
            .as_constraint("Missing required skill")
            )


def no_overlapping_shifts(constraint_factory: ConstraintFactory):
    return (constraint_factory
            .for_each_unique_pair(Shift,
                                  Joiners.equal(lambda shift: shift.employee.name),
                                  Joiners.overlapping(lambda shift: shift.start, lambda shift: shift.end))
            .penalize(HardSoftScore.ONE_HARD, get_minute_overlap)
            .as_constraint("Overlapping shift")
            )


def at_least_10_hours_between_two_shifts(constraint_factory: ConstraintFactory):
    return (constraint_factory
            .for_each(Shift)
            .join(Shift,
                  Joiners.equal(lambda shift: shift.employee.name),
                  Joiners.less_than_or_equal(lambda shift: shift.end, lambda shift: shift.start)
                  )
            .filter(lambda first_shift, second_shift:
                    (second_shift.start - first_shift.end).total_seconds() // (60 * 60) < 10)
            .penalize(HardSoftScore.ONE_HARD,
                      lambda first_shift, second_shift:
                      600 - ((second_shift.start - first_shift.end).total_seconds() // 60))
            .as_constraint("At least 10 hours between 2 shifts")
            )


def one_shift_per_day(constraint_factory: ConstraintFactory):
    return (constraint_factory
            .for_each_unique_pair(Shift,
                                  Joiners.equal(lambda shift: shift.employee.name),
                                  Joiners.equal(lambda shift: shift.start.date()))
            .penalize(HardSoftScore.ONE_HARD)
            .as_constraint("Max one shift per day")
            )


def unavailable_employee(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .filter(lambda shift: shift.start.date() in shift.employee.unavailable_dates or (
        # The in check is ignored for a shift ends at midnight (00:00:00).
            shift.end.time() != datetime.min.time()
            and shift.end.date() in shift.employee.unavailable_dates)
                    )
            .penalize(HardSoftScore.ONE_HARD,
                      lambda shift: get_shift_overlapping_duration_in_minutes(shift, shift.employee.unavailable_dates))
            .as_constraint("Unavailable employee")
            )


def undesired_day_for_employee(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .filter(lambda shift: shift.start.date() in shift.employee.undesired_dates or (
        # The in check is ignored for a shift ends at midnight (00:00:00).
            shift.end.time() != datetime.min.time()
            and shift.end.date() in shift.employee.undesired_dates)
                    )
            .penalize(HardSoftScore.ONE_SOFT,
                      lambda shift: get_shift_overlapping_duration_in_minutes(shift, shift.employee.undesired_dates))
            .as_constraint("Undesired day for employee")
            )


def desired_day_for_employee(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .filter(lambda shift: shift.start.date() in shift.employee.desired_dates or (
        # The in check is ignored for a shift ends at midnight (00:00:00).
            shift.end.time() != datetime.min.time()
            and shift.end.date() in shift.employee.desired_dates)
                    )
            .reward(HardSoftScore.ONE_SOFT,
                    lambda shift: get_shift_overlapping_duration_in_minutes(shift, shift.employee.desired_dates))
            .as_constraint("Desired day for employee")
            )
