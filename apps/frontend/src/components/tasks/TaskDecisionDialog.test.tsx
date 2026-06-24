import { fireEvent, render, screen } from "@testing-library/react";
import type { UserTask } from "../../api/client";
import { TaskDecisionDialog } from "./TaskDecisionDialog";

const task: UserTask = {
  taskId: "task-1",
  urlaubsantrag: {
    id: "request-1",
    von: "2026-08-01",
    bis: "2026-08-10",
    antragsteller: {
      name: "Ada Lovelace",
      email: "ada@example.com",
    },
    vertretung: {
      name: "Grace Hopper",
      email: "grace@example.com",
    },
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
};

describe("TaskDecisionDialog", () => {
  it("shows the selected approval and submits the chosen decision", () => {
    const onSubmitDecision = vi.fn();

    render(
      <TaskDecisionDialog
        open
        task={task}
        isLoading={false}
        isSubmitting={false}
        onClose={vi.fn()}
        onSubmitDecision={onSubmitDecision}
      />,
    );

    expect(screen.getByRole("dialog", { name: "Genehmigung bearbeiten" })).toBeInTheDocument();
    expect(screen.getByText("Genehmigung task-1")).toBeInTheDocument();
    expect(screen.getByText(/Ada Lovelace/)).toBeInTheDocument();

    fireEvent.click(screen.getByLabelText("Ablehnen"));
    fireEvent.change(screen.getByLabelText("Kommentar"), { target: { value: "Passt zeitlich nicht." } });
    fireEvent.click(screen.getByRole("button", { name: "Entscheidung senden" }));

    expect(onSubmitDecision).toHaveBeenCalledWith({
      genehmigt: false,
      kommentar: "Passt zeitlich nicht.",
    });
  });

  it("shows a loading state while the selected approval is loaded", () => {
    render(
      <TaskDecisionDialog
        open
        isLoading
        isSubmitting={false}
        onClose={vi.fn()}
        onSubmitDecision={vi.fn()}
      />,
    );

    expect(screen.getByText("Genehmigung wird geladen...")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Entscheidung senden" })).not.toBeInTheDocument();
  });
});
