const dateFormatter = new Intl.DateTimeFormat("de-DE", {
  day: "2-digit",
  month: "short",
  year: "numeric",
});

export const formatDate = (value?: string | null) => {
  if (!value) {
    return "Keine Angabe";
  }

  return dateFormatter.format(new Date(value));
};

export const formatDateRange = (from: string, to: string) => `${formatDate(from)} bis ${formatDate(to)}`;
