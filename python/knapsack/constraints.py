from timefold.solver.score import (
    constraint_provider, ConstraintFactory, HardSoftScore, ConstraintCollectors)

from domain import *


@constraint_provider
def define_constraints(factory: ConstraintFactory):
    return [
        max_weight(factory),
        max_profit(factory)
    ]


def max_weight(factory: ConstraintFactory):
    return (factory
            .for_each(Box)
            .filter(lambda box: box.selected)
            .group_by(ConstraintCollectors.sum(lambda box: box.weight))
            .join(Knapsack)
            .filter(lambda weight_sum, knapsack: weight_sum > knapsack.capacity)
            .penalize(HardSoftScore.ONE_HARD)
            .as_constraint("capacity constraint")
    )

def max_profit(factory: ConstraintFactory):
    return (factory
            .for_each(Box)
            .filter(lambda box: box.selected)
            .reward(HardSoftScore.ONE_SOFT, lambda box: box.profit)
            .as_constraint("max profit")
    )
