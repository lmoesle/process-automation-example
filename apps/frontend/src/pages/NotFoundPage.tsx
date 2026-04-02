import { Button, Stack, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router";
import { Page } from "../components/layout/Page";

export const NotFoundPage = () => (
  <Page
    title="Seite nicht gefunden"
    subtitle="Die angeforderte Route existiert in diesem Frontend nicht. Nutze die Navigation oder springe direkt zur Startseite."
  >
    <Stack spacing={2} alignItems="flex-start">
      <Typography color="text.secondary">
        Die App besitzt aktuell nur die Bereiche Uebersicht, Urlaubsantraege und Aufgaben.
      </Typography>
      <Button component={RouterLink} to="/" variant="contained" color="secondary">
        Zur Startseite
      </Button>
    </Stack>
  </Page>
);
