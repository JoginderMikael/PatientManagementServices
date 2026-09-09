import React, { useRef, useState } from "react";

export interface TabItem {
  id: string;
  label: string;
  panel: React.ReactNode;
}

export function Tabs({
  label,
  items,
}: {
  label: string;
  items: readonly TabItem[];
}) {
  const [active, setActive] = useState(items[0]?.id);
  const refs = useRef<Record<string, HTMLButtonElement | null>>({});
  function onKeyDown(event: React.KeyboardEvent, index: number) {
    if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) return;
    event.preventDefault();
    const next =
      event.key === "Home"
        ? 0
        : event.key === "End"
          ? items.length - 1
          : (index + (event.key === "ArrowRight" ? 1 : -1) + items.length) %
            items.length;
    setActive(items[next].id);
    refs.current[items[next].id]?.focus();
  }
  return (
    <div>
      <div className="tabs" role="tablist" aria-label={label}>
        {items.map((item, index) => (
          <button
            key={item.id}
            ref={(node) => {
              refs.current[item.id] = node;
            }}
            id={`${item.id}-tab`}
            className="tab"
            role="tab"
            aria-selected={active === item.id}
            aria-controls={`${item.id}-panel`}
            tabIndex={active === item.id ? 0 : -1}
            onClick={() => setActive(item.id)}
            onKeyDown={(event) => onKeyDown(event, index)}
          >
            {item.label}
          </button>
        ))}
      </div>
      {items.map(
        (item) =>
          active === item.id && (
            <div
              key={item.id}
              id={`${item.id}-panel`}
              role="tabpanel"
              aria-labelledby={`${item.id}-tab`}
              className="tab-panel"
            >
              {item.panel}
            </div>
          ),
      )}
    </div>
  );
}
