from domain import *

from timefold.solver import SolverFactory
from timefold.solver.config import (SolverConfig, ScoreDirectorFactoryConfig,
                                    TerminationConfig, Duration)

from constraints import define_constraints

boxes = [
    Box(12, 4, "green"),
    Box(2, 2, "blue"),
    Box(1, 1, "red"),
    Box(4, 10, "yellow"),
    Box(1, 2, "gray")
]

knapsack = Knapsack()
knapsack.capacity = 15
problem = KnapsackSolution(boxes, knapsack)
problem.range = [True, False]


solver_factory = SolverFactory.create(
    SolverConfig(
        solution_class=KnapsackSolution,
        entity_class_list=[Box],
        score_director_factory_config=ScoreDirectorFactoryConfig(
            constraint_provider_function=define_constraints
        ),
        termination_config=TerminationConfig(
            spent_limit=Duration(seconds=5)
        )
    )
)

solver = solver_factory.build_solver()
solution = solver.solve(problem)
print(solution.score)

# print selected boxes
for box in solution.boxes:
    if box.selected:
        print(box.to_dict())
