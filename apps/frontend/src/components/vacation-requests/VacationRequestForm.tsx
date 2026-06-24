import { useState } from "react";
import { Alert, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import type { UserSelection } from "../../api/client";

type VacationRequestFormValues = {
  von: string;
  bis: string;
  vertretungId?: string;
};

type VacationRequestFormProps = {
  isPending: boolean;
  users: UserSelection[];
  usersError?: Error | null;
  usersPending: boolean;
  inline?: boolean;
  onSubmit: (values: VacationRequestFormValues) => void;
};

export const VacationRequestForm = ({
  isPending,
  users,
  usersError,
  usersPending,
  inline = false,
  onSubmit,
}: VacationRequestFormProps) => {
  const [von, setVon] = useState("");
  const [bis, setBis] = useState("");
  const [vertretungId, setVertretungId] = useState("");

  const invalidRange = Boolean(von && bis && von > bis);
  const validVertretungId = users.some((user) => user.id === vertretungId) ? vertretungId : "";

  return (
    <Card variant="outlined" sx={{ width: "100%" }}>
      <CardContent>
        <Stack
          component="form"
          spacing={2}
          onSubmit={(event) => {
            event.preventDefault();

            if (invalidRange) {
              return;
            }

            const trimmedVertretungId = validVertretungId.trim();
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

          <Stack
            direction={inline ? { xs: "column", md: "row" } : "column"}
            spacing={2}
            alignItems={inline ? { xs: "stretch", md: "flex-start" } : "stretch"}
          >
            <TextField
              label="Von"
              type="date"
              value={von}
              onChange={(event) => setVon(event.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              required
              sx={{ flex: 1 }}
            />

            <TextField
              label="Bis"
              type="date"
              value={bis}
              onChange={(event) => setBis(event.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              required
              sx={{ flex: 1 }}
            />

            <TextField
              select
              value={validVertretungId}
              onChange={(event) => setVertretungId(event.target.value)}
              disabled={usersPending || Boolean(usersError)}
              helperText={usersPending ? "Benutzer werden geladen..." : ""}
              slotProps={{
                select: {
                  native: true,
                  inputProps: { "aria-label": "Vertretung" },
                },
              }}
              sx={{ flex: 1 }}
            >
              <option value="">Keine Vertretung</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name} ({user.email})
                </option>
              ))}
            </TextField>

            <Button
              type="submit"
              variant="contained"
              color="secondary"
              disabled={isPending || invalidRange}
              sx={{ minWidth: { md: 220 } }}
            >
              Urlaubsantrag stellen
            </Button>
          </Stack>

          {usersError ? (
            <Alert severity="warning">Benutzer konnten nicht geladen werden: {usersError.message}</Alert>
          ) : null}

          {invalidRange ? <Alert severity="warning">`Von` muss vor oder gleich `Bis` liegen.</Alert> : null}

        </Stack>
      </CardContent>
    </Card>
  );
};
