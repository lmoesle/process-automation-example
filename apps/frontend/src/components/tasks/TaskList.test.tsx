import { fireEvent, render, screen } from "@testing-library/react";
import { TaskList } from "./TaskList";
import type { UserTask } from "../../api/client";

const tasks: UserTask[] = [
  {
    taskId: "task-1",
    urlaubsantrag: null,
    candidateUsers: [],
    bearbeiter: null,
  },
  {
    taskId: "task-2",
    urlaubsantrag: {
      id: "request-2",
      von: "2026-08-01",
      bis: "2026-08-10",
      antragsteller: {
        name: "Ada Lovelace",
        email: "ada@example.com",
      },
      vertretung: null,
      vorgesetzter: null,
      status: "VORGESETZTEN_PRUEFUNG",
      statusHistorie: [],
    },
    candidateUsers: [
      {
        name: "Grace Hopper",
        email: "grace@example.com",
      },
    ],
    bearbeiter: null,
  },
];

describe("TaskList", () => {
  it("calls onSelectTask for the clicked entry", () => {
    const onSelectTask = vi.fn();

    render(<TaskList tasks={tasks} onSelectTask={onSelectTask} selectedTaskId="task-1" />);

    fireEvent.click(screen.getByText("Aufgabe task-2"));

    expect(onSelectTask).toHaveBeenCalledWith("task-2");
  });
});
