import React, { useEffect, useRef } from "react";

export function Dialog({
  open,
  title,
  description,
  children,
  confirmLabel,
  danger,
  onClose,
  onConfirm,
}: {
  open: boolean;
  title: string;
  description: string;
  children?: React.ReactNode;
  confirmLabel: string;
  danger?: boolean;
  onClose(): void;
  onConfirm(): void;
}) {
  const dialogRef = useRef<HTMLDivElement>(null);
  const previousFocus = useRef<HTMLElement | null>(null);
  useEffect(() => {
    if (!open) return;
    previousFocus.current = document.activeElement as HTMLElement;
    dialogRef.current
      ?.querySelector<HTMLElement>("button,input,select,textarea")
      ?.focus();
    return () => previousFocus.current?.focus();
  }, [open]);
  if (!open) return null;
  return (
    <div
      className="dialog-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="dialog-title"
        aria-describedby="dialog-description"
        ref={dialogRef}
        onKeyDown={(event) => {
          if (event.key === "Escape") onClose();
        }}
      >
        <header>
          <h2 id="dialog-title">{title}</h2>
          <p id="dialog-description">{description}</p>
        </header>
        {children && <div className="dialog-body">{children}</div>}
        <footer>
          <button
            className="button button--secondary"
            type="button"
            onClick={onClose}
          >
            Cancel
          </button>
          <button
            className={`button ${danger ? "button--danger" : "button--primary"}`}
            type="button"
            onClick={onConfirm}
          >
            {confirmLabel}
          </button>
        </footer>
      </div>
    </div>
  );
}
