from timefold.solver import SolverFactory
from timefold.solver.config import (SolverConfig, ScoreDirectorFactoryConfig,
                                    TerminationConfig, Duration, TerminationCompositionStyle)

from school_timetabling.domain import Timetable, Lesson
from school_timetabling.constraints import school_timetabling_constraints
from school_timetabling.demo_data import generate_demo_data, DemoData


def test_feasible():
    solver_factory = SolverFactory.create(
        SolverConfig(
            solution_class=Timetable,
            entity_class_list=[Lesson],
            score_director_factory_config=ScoreDirectorFactoryConfig(
                constraint_provider_function=school_timetabling_constraints
            ),
            termination_config=TerminationConfig(
                termination_config_list=[
                    TerminationConfig(best_score_feasible=True),
                    TerminationConfig(spent_limit=Duration(seconds=30)),
                ],
                termination_composition_style=TerminationCompositionStyle.OR
            )
        ))
    solver = solver_factory.build_solver()
    solution = solver.solve(generate_demo_data(DemoData.SMALL))
    assert solution.score.is_feasible
