import { useState } from "react";
import {
    Alert,
    Button,
    Card,
    CardContent,
    FormControlLabel,
    Radio,
    RadioGroup,
    Stack,
    TextField,
    Typography
} from "@mui/material";
import type { ManagerDecisionInput, UserTask } from "../../api/client";

type ManagerDecisionFormProps = {
  task?: UserTask | undefined;
  isSubmitting: boolean;
  onSubmitDecision: (body: ManagerDecisionInput) => void;
};

export const ManagerDecisionForm = ({
  task,
  isSubmitting,
  onSubmitDecision,
}: ManagerDecisionFormProps) => {
  const [decision, setDecision] = useState<"approve" | "reject">("approve");
  const [comment, setComment] = useState("");

  const canDecide = Boolean(task?.urlaubsantrag);

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack
          component="form"
          spacing={2}
          onSubmit={(event) => {
            event.preventDefault();

            if (!canDecide) {
              return;
            }

            const trimmedComment = comment.trim();
            onSubmitDecision({
              genehmigt: decision === "approve",
              ...(trimmedComment ? { kommentar: trimmedComment } : {}),
            });
          }}
        >
          <Stack spacing={0.5}>
            <Typography variant="h5">Aktionen</Typography>
          </Stack>

          <RadioGroup value={decision} onChange={(_event, value) => setDecision(value as "approve" | "reject")}>
            <FormControlLabel value="approve" control={<Radio />} label="Genehmigen" />
            <FormControlLabel value="reject" control={<Radio />} label="Ablehnen" />
          </RadioGroup>

          <TextField
            label="Kommentar"
            multiline
            minRows={3}
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            placeholder="Optionaler Kommentar fuer die Statushistorie"
          />

          {!canDecide ? (
            <Alert severity="info">Fuer diese Aufgabe liegt kein Urlaubsantrag im Payload vor.</Alert>
          ) : null}

          <Button type="submit" variant="contained" color="secondary" disabled={!canDecide || isSubmitting}>
            {isSubmitting ? "Sende Entscheidung..." : "Entscheidung senden"}
          </Button>
        </Stack>
      </CardContent>
    </Card>
  );
};
