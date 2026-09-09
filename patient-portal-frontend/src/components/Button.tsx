import React from "react";

export function Button({
  variant = "primary",
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "danger" | "ghost";
}) {
  return (
    <button
      {...props}
      className={`button button--${variant} ${props.className ?? ""}`}
    />
  );
}
