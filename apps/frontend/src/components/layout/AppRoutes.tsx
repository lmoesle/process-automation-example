import { Route, Routes } from "react-router";
import { AppShell } from "./AppShell";
import { HomePage } from "../../pages/HomePage";
import { VacationRequestsPage } from "../../pages/VacationRequestsPage";
import { TasksPage } from "../../pages/TasksPage";
import { NotFoundPage } from "../../pages/NotFoundPage";

export const AppRoutes = () => (
  <Routes>
    <Route element={<AppShell />}>
      <Route path="/" element={<HomePage />} />
      <Route path="/urlaubsantraege" element={<VacationRequestsPage />} />
      <Route path="/tasks" element={<TasksPage />} />
      <Route path="/tasks/:taskId" element={<TasksPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Route>
  </Routes>
);
