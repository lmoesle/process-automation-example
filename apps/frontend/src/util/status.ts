import type { VacationStatus } from "../api/client";

const vacationStatusLabels: Record<VacationStatus, string> = {
  ANTRAG_GESTELLT: "Antrag gestellt",
  AUTOMATISCHE_PRUEFUNG: "Automatische Pruefung",
  VORGESETZTEN_PRUEFUNG: "Vorgesetztenpruefung",
  ABGELEHNT: "Abgelehnt",
  GENEHMIGT: "Genehmigt",
};

export const formatVacationStatus = (status: VacationStatus) => vacationStatusLabels[status];
