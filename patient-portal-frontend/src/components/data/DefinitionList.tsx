import React from "react";

export function DefinitionList({
  items,
}: {
  items: readonly { term: string; description: React.ReactNode }[];
}) {
  return (
    <dl className="definition-list">
      {items.map((item) => (
        <div key={item.term}>
          <dt>{item.term}</dt>
          <dd>{item.description}</dd>
        </div>
      ))}
    </dl>
  );
}
