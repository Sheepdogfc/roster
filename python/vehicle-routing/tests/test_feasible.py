from timefold.solver import SolverFactory
from timefold.solver.config import (SolverConfig, ScoreDirectorFactoryConfig,
                                    TerminationConfig, Duration, TerminationCompositionStyle)

from vehicle_routing.domain import VehicleRoutePlan, Vehicle, Visit
from vehicle_routing.constraints import vehicle_routing_constraints
from vehicle_routing.demo_data import generate_demo_data, DemoData


def test_feasible():
    solver_factory = SolverFactory.create(
        SolverConfig(
            solution_class=VehicleRoutePlan,
            entity_class_list=[Vehicle, Visit],
            score_director_factory_config=ScoreDirectorFactoryConfig(
                constraint_provider_function=vehicle_routing_constraints
            ),
            termination_config=TerminationConfig(
                termination_config_list=[
                    TerminationConfig(best_score_feasible=True),
                    TerminationConfig(spent_limit=Duration(seconds=120)),
                ],
                termination_composition_style=TerminationCompositionStyle.OR
            )
        ))
    solver = solver_factory.build_solver()
    solution = solver.solve(generate_demo_data(DemoData.PHILADELPHIA))
    assert solution.score.is_feasible
