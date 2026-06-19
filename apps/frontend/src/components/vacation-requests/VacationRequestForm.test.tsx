import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { VacationRequestForm } from "./VacationRequestForm";
import type { UserSelection } from "../../api/client";

const users: UserSelection[] = [
  {
    id: "user-1",
    name: "Ada Lovelace",
    email: "ada@example.com",
  },
  {
    id: "user-2",
    name: "Carla Gomez",
    email: "carla@example.com",
  },
];

describe("VacationRequestForm", () => {
  it("submits the selected substitute user", () => {
    const onSubmit = vi.fn();

    render(
      <VacationRequestForm
        isPending={false}
        users={users}
        usersPending={false}
        onSubmit={onSubmit}
      />,
    );

    fireEvent.change(screen.getByLabelText(/Von/), { target: { value: "2026-07-01" } });
    fireEvent.change(screen.getByLabelText(/Bis/), { target: { value: "2026-07-10" } });
    fireEvent.change(screen.getByLabelText(/Vertretung/), { target: { value: "user-2" } });
    fireEvent.click(screen.getByRole("button", { name: "Urlaubsantrag stellen" }));

    expect(onSubmit).toHaveBeenCalledWith({
      von: "2026-07-01",
      bis: "2026-07-10",
      vertretungId: "user-2",
    });
  });

  it("clears the substitute user when available users change", async () => {
    const onSubmit = vi.fn();

    const { rerender } = render(
      <VacationRequestForm
        isPending={false}
        users={users}
        usersPending={false}
        onSubmit={onSubmit}
      />,
    );

    fireEvent.change(screen.getByLabelText(/Von/), { target: { value: "2026-07-01" } });
    fireEvent.change(screen.getByLabelText(/Bis/), { target: { value: "2026-07-10" } });
    fireEvent.change(screen.getByLabelText(/Vertretung/), { target: { value: "user-2" } });

    rerender(
      <VacationRequestForm
        isPending={false}
        users={users.slice(0, 1)}
        usersPending={false}
        onSubmit={onSubmit}
      />,
    );

    await waitFor(() => expect(screen.getByLabelText(/Vertretung/)).toHaveValue(""));
    fireEvent.click(screen.getByRole("button", { name: "Urlaubsantrag stellen" }));

    expect(onSubmit).toHaveBeenCalledWith({
      von: "2026-07-01",
      bis: "2026-07-10",
    });
  });
});
