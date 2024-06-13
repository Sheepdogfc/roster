from timefold.solver import SolverStatus
from timefold.solver.domain import *
from timefold.solver.score import HardSoftScore
from datetime import datetime, date, timedelta
from typing import Annotated, Any
from pydantic import BaseModel, ConfigDict, Field, PlainSerializer, BeforeValidator, ValidationInfo
from pydantic.alias_generators import to_camel

ScoreSerializer = PlainSerializer(lambda score: str(score) if score is not None else None,
                                  return_type=str | None)


def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardSoftScore) or v is None:
        return v
    if isinstance(v, str):
        hard_part, soft_part = v.split('/')
        hard = int(hard_part.rstrip('hard'))
        soft = int(soft_part.rstrip('soft'))
        return HardSoftScore.of(hard, soft)
    raise ValueError('"score" should be a string')


ScoreValidator = BeforeValidator(validate_score)


DESIRED = 'DESIRED'
UNDESIRED = 'UNDESIRED'
UNAVAILABLE = 'UNAVAILABLE'


class BaseSchema(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


class Employee(BaseSchema):
    name: Annotated[str, PlanningId]
    skills: set[str]


def shift_pinning_filter(employee_schedule, shift):
    schedule_state = employee_schedule.schedule_state
    return False and not schedule_state.is_draft(shift)


@planning_entity(pinning_filter=shift_pinning_filter)
class Shift(BaseSchema):
    id: Annotated[str, PlanningId]

    start: datetime
    end: datetime

    location: str
    required_skill: str

    employee: Annotated[Employee | None,
                        PlanningVariable,
                        Field(default=None)]


class Availability(BaseSchema):
    id: Annotated[str, PlanningId]
    employee: Employee
    date: date
    availability_type: str


class ScheduleState(BaseSchema):
    tenant_id: str
    publish_length: int  # In number of days
    draft_length: int  # In number of days
    first_draft_date: date
    last_historic_date: date

    def is_historic(self, date_time) -> bool:
        if isinstance(date_time, Shift):
            return self.is_historic(date_time.start)

        return date_time < datetime.combine(self.first_published_date(), datetime.min.time())

    def is_draft(self, date_time) -> bool:
        if isinstance(date_time, Shift):
            return self.is_draft(date_time.start)
        return date_time >= datetime.combine(self.first_draft_date, datetime.min.time())

    def is_published(self, date_time) -> bool:
        if isinstance(date_time, Shift):
            return self.is_published(date_time.start)
        return not self.is_historic(date_time) and not self.is_draft(date_time)

    def first_published_date(self) -> date:
        return self.last_historic_date + timedelta(days=1)

    def first_unplanned_date(self) -> date:
        return self.first_draft_date + timedelta(days=self.draft_length)


@planning_solution
class EmployeeSchedule(BaseSchema):
    availabilities: Annotated[list[Availability], ProblemFactCollectionProperty]
    employees: Annotated[list[Employee], ProblemFactCollectionProperty, ValueRangeProvider]
    shifts: Annotated[list[Shift], PlanningEntityCollectionProperty]
    schedule_state: ScheduleState
    score: Annotated[HardSoftScore | None,
                     PlanningScore,
                     ScoreSerializer,
                     ScoreValidator,
                     Field(default=None)]
    solver_status: Annotated[SolverStatus | None, Field(default=None)]
