from timefold.solver.domain import planning_entity
from typing import Annotated
from timefold.solver.domain import *
from timefold.solver.score import *


@planning_entity
class Box:
    weight: int
    profit: int
    color: str
    selected: Annotated[bool, PlanningVariable]
    
    def __init__(self, weight: int, profit: int, color: str):
        self.weight = weight
        self.profit = profit
        self.color = color

    def to_dict(self):
        return {
            "weight": self.weight,
            "profit": self.profit,
            "color": self.color,
            "selected": self.selected
        }

class Knapsack:
    capacity: int



@planning_solution
class KnapsackSolution:
    boxes: Annotated[list[Box], PlanningEntityCollectionProperty]
    knapsack: Annotated[Knapsack, ProblemFactProperty]
    range: Annotated[list[bool], ValueRangeProvider]
    score: Annotated[HardSoftScore, PlanningScore]

    def __init__(self, boxes: list[Box], knapsack: Knapsack):
        self.boxes = boxes
        self.knapsack = knapsack
        