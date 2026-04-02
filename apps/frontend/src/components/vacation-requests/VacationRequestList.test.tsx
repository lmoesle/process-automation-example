import { render, screen } from "@testing-library/react";
import { VacationRequestList } from "./VacationRequestList";
import type { VacationRequest } from "../../api/client";

const requests: VacationRequest[] = [
  {
    id: "request-1",
    von: "2026-07-01",
    bis: "2026-07-05",
    antragsteller: {
      name: "Ada Lovelace",
      email: "ada@example.com",
    },
    vertretung: {
      name: "Grace Hopper",
      email: "grace@example.com",
    },
    vorgesetzter: null,
    status: "ANTRAG_GESTELLT",
    statusHistorie: [
      {
        status: "ANTRAG_GESTELLT",
        kommentar: "Vertretung ist organisiert.",
      },
    ],
  },
];

describe("VacationRequestList", () => {
  it("renders the request details and history", () => {
    render(<VacationRequestList requests={requests} />);

    expect(screen.getByText("Ada Lovelace")).toBeInTheDocument();
    expect(screen.getByText("Grace Hopper")).toBeInTheDocument();
    expect(screen.getAllByText("Antrag gestellt")[0]).toBeInTheDocument();
    expect(screen.getByText("Vertretung ist organisiert.")).toBeInTheDocument();
  });
});
