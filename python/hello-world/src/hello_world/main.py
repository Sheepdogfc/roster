from timefold.solver.config import (SolverConfig, ScoreDirectorFactoryConfig,
                                    TerminationConfig, Duration)
from timefold.solver import SolverFactory
from enum import Enum
from datetime import time
import logging

from .domain import Lesson, Timeslot, Room, Timetable
from .constraints import school_timetabling_constraints


logging.basicConfig(level=logging.INFO)
LOGGER = logging.getLogger('app')


def main():
    solver_factory = SolverFactory.create(
        SolverConfig(
            solution_class=Timetable,
            entity_class_list=[Lesson],
            score_director_factory_config=ScoreDirectorFactoryConfig(
                constraint_provider_function=school_timetabling_constraints
            ),
            termination_config=TerminationConfig(
                # The solver runs only for 5 seconds on this small dataset.
                # It's recommended to run for at least 5 minutes ("5m") otherwise.
                spent_limit=Duration(seconds=5)
            )
        ))

    # Load the problem
    problem = generate_demo_data(DemoData.SMALL)

    # Solve the problem
    solver = solver_factory.build_solver()
    solution = solver.solve(problem)

    # Visualize the solution
    print_timetable(solution)


def generate_demo_data(demo_data: 'DemoData') -> Timetable:
    timeslots = [
        Timeslot(day, start, start.replace(hour=start.hour + 1))
        for day in ('MONDAY', 'TUESDAY')
        for start in (time(8, 30), time(9, 30), time(10, 30), time(13, 30), time(14, 30))
    ]
    rooms = [Room(f'Room {name}') for name in ('A', 'B', 'C')]

    lessons = []

    def id_generator():
        current = 0
        while True:
            yield str(current)
            current += 1

    ids = id_generator()
    lessons.append(Lesson(next(ids), "Math", "A. Turing", "9th grade"))
    lessons.append(Lesson(next(ids), "Math", "A. Turing", "9th grade"))
    lessons.append(Lesson(next(ids), "Physics", "M. Curie", "9th grade"))
    lessons.append(Lesson(next(ids), "Chemistry", "M. Curie", "9th grade"))
    lessons.append(Lesson(next(ids), "Biology", "C. Darwin", "9th grade"))
    lessons.append(Lesson(next(ids), "History", "I. Jones", "9th grade"))
    lessons.append(Lesson(next(ids), "English", "I. Jones", "9th grade"))
    lessons.append(Lesson(next(ids), "English", "I. Jones", "9th grade"))
    lessons.append(Lesson(next(ids), "Spanish", "P. Cruz", "9th grade"))
    lessons.append(Lesson(next(ids), "Spanish", "P. Cruz", "9th grade"))

    lessons.append(Lesson(next(ids), "Math", "A. Turing", "10th grade"))
    lessons.append(Lesson(next(ids), "Math", "A. Turing", "10th grade"))
    lessons.append(Lesson(next(ids), "Math", "A. Turing", "10th grade"))
    lessons.append(Lesson(next(ids), "Physics", "M. Curie", "10th grade"))
    lessons.append(Lesson(next(ids), "Chemistry", "M. Curie", "10th grade"))
    lessons.append(Lesson(next(ids), "French", "M. Curie", "10th grade"))
    lessons.append(Lesson(next(ids), "Geography", "C. Darwin", "10th grade"))
    lessons.append(Lesson(next(ids), "History", "I. Jones", "10th grade"))
    lessons.append(Lesson(next(ids), "English", "P. Cruz", "10th grade"))
    lessons.append(Lesson(next(ids), "Spanish", "P. Cruz", "10th grade"))

    return Timetable(demo_data.name, timeslots, rooms, lessons)


def print_timetable(time_table: Timetable) -> None:
    LOGGER.info("")
    rooms = time_table.rooms
    timeslots = time_table.timeslots
    lessons = time_table.lessons
    lesson_map = {
        (lesson.room.name, lesson.timeslot.day_of_week, lesson.timeslot.start_time): lesson
        for lesson in lessons
        if lesson.room is not None and lesson.timeslot is not None
    }
    row_format ="|{:<15}" * (len(rooms) + 1) + "|"
    sep_format = "+" + ((("-" * 15) + "+") * (len(rooms) + 1))

    LOGGER.info(sep_format)
    LOGGER.info(row_format.format('', *[room.name for room in rooms]))
    LOGGER.info(sep_format)

    for timeslot in timeslots:
        def get_row_lessons():
            for room in rooms:
                yield lesson_map.get((room.name, timeslot.day_of_week, timeslot.start_time),
                                     Lesson('', '', '', ''))

        row_lessons = [*get_row_lessons()]
        LOGGER.info(row_format.format(str(timeslot), *[lesson.subject for lesson in row_lessons]))
        LOGGER.info(row_format.format('', *[lesson.teacher for lesson in row_lessons]))
        LOGGER.info(row_format.format('', *[lesson.student_group for lesson in row_lessons]))
        LOGGER.info(sep_format)

    unassigned_lessons = [lesson for lesson in lessons if lesson.room is None or lesson.timeslot is None]
    if len(unassigned_lessons) > 0:
        LOGGER.info("")
        LOGGER.info("Unassigned lessons")
        for lesson in unassigned_lessons:
            LOGGER.info(f'    {lesson.subject} - {lesson.teacher} - {lesson.student_group}')


class DemoData(Enum):
    SMALL = 'SMALL'
    LARGE = 'LARGE'


if __name__ == '__main__':
    main()
