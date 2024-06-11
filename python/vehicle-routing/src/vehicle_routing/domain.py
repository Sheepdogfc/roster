from timefold.solver import SolverStatus
from timefold.solver.score import HardSoftScore, ScoreDirector
from timefold.solver.domain import *

from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import Annotated, Optional
from pydantic import BaseModel, ConfigDict, PlainSerializer, BeforeValidator, Field, computed_field
from pydantic.alias_generators import to_camel


class BaseSchema(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


LocationSerializer = PlainSerializer(lambda location: [
    location.latitude,
    location.longitude,
], return_type=list)
ScoreSerializer = PlainSerializer(lambda score: str(score), return_type=str)
IdSerializer = PlainSerializer(lambda item: item.id if item is not None else None, return_type=str | None)
IdListSerializer = PlainSerializer(lambda items: [item.id for item in items], return_type=list)

LocationDeserializer = BeforeValidator(lambda location: location if isinstance(location, Location)
                                       else Location(latitude=location[0], longitude=location[1]))


class Location(BaseSchema):
    latitude: float
    longitude: float
    driving_time_seconds: Annotated[dict[int, int], Field(default_factory=dict)]

    def driving_time_to(self, other: 'Location') -> int:
        if id(other) not in self.driving_time_seconds:
            self.driving_time_seconds[id(other)] = round((
                                                                 (self.latitude - other.latitude) ** 2 +
                                                                 (self.longitude - other.longitude) ** 2
                                                         ) ** 0.5 * 1000)
        return self.driving_time_seconds[id(other)]

    def __str__(self):
        return f'[{self.latitude}, {self.longitude}]'

    def __repr__(self):
        return f'Location({self.latitude}, {self.longitude})'


class ArrivalTimeUpdatingVariableListener(VariableListener):
    def after_variable_changed(self, score_director: ScoreDirector, visit) -> None:
        if visit.vehicle is None:
            if visit.arrival_time is not None:
                score_director.before_variable_changed(visit, 'arrival_time')
                visit.arrival_time = None
                score_director.after_variable_changed(visit, 'arrival_time')
            return
        previous_visit = visit.previous_visit
        departure_time = visit.vehicle.departure_time if previous_visit is None else (
            previous_visit.calculate_departure_time())
        next_visit = visit
        arrival_time = ArrivalTimeUpdatingVariableListener.calculate_arrival_time(next_visit, departure_time)
        while next_visit is not None and next_visit.arrival_time != arrival_time:
            score_director.before_variable_changed(next_visit, 'arrival_time')
            next_visit.arrival_time = arrival_time
            score_director.after_variable_changed(next_visit, 'arrival_time')
            departure_time = next_visit.calculate_departure_time()
            next_visit = next_visit.next_visit
            arrival_time = ArrivalTimeUpdatingVariableListener.calculate_arrival_time(next_visit, departure_time)

    @staticmethod
    def calculate_arrival_time(visit, previous_departure_time: Optional[datetime]) \
            -> datetime | None:
        if visit is None or previous_departure_time is None:
            return None
        return previous_departure_time + timedelta(seconds=visit.driving_time_seconds_from_previous_standstill())


@planning_entity
class Visit(BaseSchema):
    id: Annotated[str, PlanningId]
    name: str
    location: Annotated[Location, LocationSerializer, LocationDeserializer]
    demand: int
    min_start_time: datetime
    max_end_time: datetime
    service_duration: timedelta
    vehicle: Annotated[Optional['Vehicle'],
                       InverseRelationShadowVariable(source_variable_name='visits'),
                       IdSerializer,
                       Field(default=None)]
    previous_visit: Annotated[Optional['Visit'],
                              PreviousElementShadowVariable(source_variable_name='visits'),
                              IdSerializer,
                              Field(default=None)]
    next_visit: Annotated[Optional['Visit'],
                          NextElementShadowVariable(source_variable_name='visits'),
                          IdSerializer,
                          Field(default=None)]
    arrival_time: Annotated[Optional[datetime],
                            ShadowVariable(variable_listener_class=ArrivalTimeUpdatingVariableListener,
                                           source_variable_name='vehicle'),
                            ShadowVariable(variable_listener_class=ArrivalTimeUpdatingVariableListener,
                                           source_variable_name='previous_visit'),
                            Field(default=None)]

    @computed_field
    @property
    def departure_time(self) -> Optional[datetime]:
        return self.calculate_departure_time()

    def calculate_departure_time(self) -> Optional[datetime]:
        if self.arrival_time is None:
            return None

        return self.arrival_time + self.service_duration

    @computed_field
    @property
    def start_service_time(self) -> Optional[datetime]:
        if self.arrival_time is None:
            return None
        return self.min_start_time if (self.min_start_time < self.arrival_time) else self.arrival_time

    def is_service_finished_after_max_end_time(self) -> bool:
        return self.arrival_time is not None and self.calculate_departure_time() > self.max_end_time

    def service_finished_delay_in_minutes(self) -> int:
        if self.arrival_time is None:
            return 0
        return (self.max_end_time - self.calculate_departure_time()).seconds // 60

    def driving_time_seconds_from_previous_standstill(self) -> int:
        if self.vehicle is None:
            raise ValueError("This method must not be called when the shadow variables are not initialized yet.")

        if self.previous_visit is None:
            return self.vehicle.home_location.driving_time_to(self.location)
        else:
            return self.previous_visit.location.driving_time_to(self.location)

    def driving_time_seconds_from_previous_standstill_or_none(self) -> Optional[int]:
        if self.vehicle is None:
            return None
        return self.driving_time_seconds_from_previous_standstill()

    def __str__(self):
        return self.id

    def __repr__(self):
        return f'Visit({self.id})'


@planning_entity
class Vehicle(BaseSchema):
    id: Annotated[str, PlanningId]
    capacity: int
    home_location: Annotated[Location, LocationSerializer, LocationDeserializer]
    departure_time: datetime
    visits: Annotated[list[Visit],
                      PlanningListVariable,
                      IdListSerializer,
                      Field(default_factory=list)]

    @computed_field
    @property
    def total_demand(self) -> int:
        return self.calculate_total_demand()

    @computed_field
    @property
    def total_driving_time_seconds(self) -> int:
        return self.calculate_total_driving_time_seconds()

    def calculate_total_demand(self) -> int:
        total_demand = 0
        for visit in self.visits:
            total_demand += visit.demand
        return total_demand

    def calculate_total_driving_time_seconds(self) -> int:
        if len(self.visits) == 0:
            return 0
        total_driving_time_seconds = 0
        previous_location = self.home_location

        for visit in self.visits:
            total_driving_time_seconds += previous_location.driving_time_to(visit.location)
            previous_location = visit.location

        total_driving_time_seconds += previous_location.driving_time_to(self.home_location)
        return total_driving_time_seconds

    def arrival_time(self):
        if len(self.visits) == 0:
            return self.departure_time

        last_visit = self.visits[-1]
        return (last_visit.calculate_departure_time() +
                timedelta(seconds=last_visit.location.driving_time_to(self.home_location)))

    def __str__(self):
        return self.id

    def __repr__(self):
        return f'Vehicle({self.id})'


@planning_solution
class VehicleRoutePlan(BaseSchema):
    name: str
    south_west_corner: Annotated[Location, LocationSerializer, LocationDeserializer]
    north_east_corner: Annotated[Location, LocationSerializer, LocationDeserializer]
    start_date_time: datetime
    end_date_time: datetime
    vehicles: Annotated[list[Vehicle], PlanningEntityCollectionProperty]
    visits: Annotated[list[Visit], PlanningEntityCollectionProperty, ValueRangeProvider]
    score: Annotated[Optional[HardSoftScore],
                     PlanningScore,
                     ScoreSerializer,
                     Field(default=None)]
    solver_status: Annotated[Optional[SolverStatus],
                             Field(default=None)]
    score_explanation: Annotated[Optional[str],
                                 Field(default=None)]

    def __str__(self):
        return f'VehicleRoutePlan(name={self.id}, vehicles={self.vehicles}, visits={self.visits})'


@dataclass
class MatchAnalysisDTO:
    name: str
    score: str
    justification: object


@dataclass
class ConstraintAnalysisDTO:
    name: str
    weight: str
    matches: list[MatchAnalysisDTO]
    score: str
