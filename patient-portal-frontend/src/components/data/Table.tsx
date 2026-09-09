import React from "react";

export function Table({
  caption,
  headers,
  children,
}: {
  caption: string;
  headers: readonly string[];
  children: React.ReactNode;
}) {
  return (
    <div
      className="table-region"
      role="region"
      aria-label={caption}
      tabIndex={0}
    >
      <table>
        <caption>{caption}</caption>
        <thead>
          <tr>
            {headers.map((header) => (
              <th scope="col" key={header}>
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  );
}
