import { useState } from "react";
import { Button } from "../Button";
import { Dialog } from "./Dialog";

export function ConfirmAction({
  triggerLabel,
  title,
  description,
  confirmLabel,
  danger,
  disabled,
  onConfirm,
}: {
  triggerLabel: string;
  title: string;
  description: string;
  confirmLabel: string;
  danger?: boolean;
  disabled?: boolean;
  onConfirm(): void;
}) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <Button
        variant={danger ? "danger" : "secondary"}
        type="button"
        disabled={disabled}
        onClick={() => setOpen(true)}
      >
        {triggerLabel}
      </Button>
      <Dialog
        open={open}
        title={title}
        description={description}
        confirmLabel={confirmLabel}
        danger={danger}
        onClose={() => setOpen(false)}
        onConfirm={() => {
          setOpen(false);
          onConfirm();
        }}
      />
    </>
  );
}
