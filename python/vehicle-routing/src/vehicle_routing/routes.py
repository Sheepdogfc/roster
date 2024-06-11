from timefold.solver import SolverManager, SolverFactory, SolutionManager, set_class_output_directory
from timefold.solver.config import (SolverConfig, ScoreDirectorFactoryConfig,
                                    TerminationConfig, Duration)
from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
import pathlib

from .domain import VehicleRoutePlan, Vehicle, Visit, MatchAnalysisDTO, ConstraintAnalysisDTO
from .constraints import vehicle_routing_constraints
from .demo_data import DemoData, generate_demo_data

set_class_output_directory(pathlib.Path('target'))

solver_config = SolverConfig(
    solution_class=VehicleRoutePlan,
    entity_class_list=[Vehicle, Visit],
    score_director_factory_config=ScoreDirectorFactoryConfig(
        constraint_provider_function=vehicle_routing_constraints
    ),
    termination_config=TerminationConfig(
        spent_limit=Duration(seconds=30)
    )
)

solver_factory = SolverFactory.create(solver_config)
solver_manager = SolverManager.create(SolverFactory.create(solver_config))
solution_manager = SolutionManager.create(solver_manager)

app = FastAPI(docs_url='/q/swagger-ui')
data_sets: dict[str, VehicleRoutePlan] = {}


@app.get("/demo-data")
async def demo_data_list():
    return [e.name for e in DemoData]


@app.get("/demo-data/{dataset_id}", response_model_exclude_none=True)
async def get_demo_data(dataset_id: str) -> VehicleRoutePlan:
    demo_data = generate_demo_data(getattr(DemoData, dataset_id))
    return demo_data


@app.get("/route-plans/{problem_id}", response_model_exclude_none=True)
async def get_route(problem_id: str) -> VehicleRoutePlan:
    route = data_sets[problem_id]
    out = route.model_copy(update={
        'solver_status': solver_manager.get_solver_status(problem_id),
        'score_explanation': solution_manager.explain(route).summary
    })
    solution_manager.update(out)
    return out


def update_route(problem_id: str, route: VehicleRoutePlan):
    global data_sets
    data_sets[problem_id] = route


@app.post("/route-plans")
async def solve_route(route: VehicleRoutePlan) -> str:
    data_sets['ID'] = route
    solver_factory.build_solver().solve(route)
    # solver_manager.solve_and_listen('ID', route,
    #                                 lambda solution: update_route('ID', solution))
    return 'ID'


@app.put("/route-plans/analyze")
async def analyze_route(route: VehicleRoutePlan) -> dict:
    return {'constraints': [ConstraintAnalysisDTO(
        name=constraint.constraint_name,
        weight=str(constraint.weight),
        score=str(constraint.score),
        matches=[
            MatchAnalysisDTO(
                name=match.constraint_ref.constraint_name,
                score=str(match.score),
                justification=match.justification
            )
            for match in constraint.matches
        ]
    ) for constraint in solution_manager.analyze(route).constraint_analyses]}


app.mount("/", StaticFiles(directory="static", html=True), name="static")
