import { useState } from "react";
import { Alert, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";

type VacationRequestFormValues = {
  von: string;
  bis: string;
  vertretungId?: string | undefined;
};

type VacationRequestFormProps = {
  isPending: boolean;
  onSubmit: (values: VacationRequestFormValues) => void;
};

export const VacationRequestForm = ({ isPending, onSubmit }: VacationRequestFormProps) => {
  const [von, setVon] = useState("");
  const [bis, setBis] = useState("");
  const [vertretungId, setVertretungId] = useState("");

  const invalidRange = Boolean(von && bis && von > bis);

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack
          component="form"
          spacing={2}
          onSubmit={(event) => {
            event.preventDefault();

            if (invalidRange) {
              return;
            }

            const trimmedVertretungId = vertretungId.trim();
            onSubmit({
              von,
              bis,
              ...(trimmedVertretungId ? { vertretungId: trimmedVertretungId } : {}),
            });
          }}
        >
          <Stack spacing={0.5}>
            <Typography variant="h5">Neuen Urlaubsantrag stellen</Typography>
          </Stack>

          <TextField
            label="Von"
            type="date"
            value={von}
            onChange={(event) => setVon(event.target.value)}
            InputLabelProps={{ shrink: true }}
            required
          />

          <TextField
            label="Bis"
            type="date"
            value={bis}
            onChange={(event) => setBis(event.target.value)}
            InputLabelProps={{ shrink: true }}
            required
          />

          <TextField
            label="Vertretung (optional)"
            placeholder="UUID der Vertretung"
            value={vertretungId}
            onChange={(event) => setVertretungId(event.target.value)}
          />

          {invalidRange ? <Alert severity="warning">`Von` muss vor oder gleich `Bis` liegen.</Alert> : null}

          <Button type="submit" variant="contained" color="secondary" disabled={isPending || invalidRange}>
            Urlaubsantrag stellen
          </Button>
        </Stack>
      </CardContent>
    </Card>
  );
};
