import { fireEvent, render, screen } from "@testing-library/react";
import { ManagerDecisionForm } from "./ManagerDecisionForm";
import type { UserTask } from "../../api/client";

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
};

describe("ManagerDecisionForm", () => {
  it("submits a decision without a separate assign action", () => {
    const onSubmitDecision = vi.fn();

    render(<ManagerDecisionForm task={task} isSubmitting={false} onSubmitDecision={onSubmitDecision} />);

    expect(screen.queryByRole("button", { name: "Mir zuweisen" })).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText("Kommentar"), { target: { value: "Passt fuer das Team." } });
    fireEvent.click(screen.getByRole("button", { name: "Entscheidung senden" }));

    expect(onSubmitDecision).toHaveBeenCalledWith({
      genehmigt: true,
      kommentar: "Passt fuer das Team.",
    });
  });
});
