import React from 'react';

export type AlertTone = 'info' | 'success' | 'warning' | 'danger';

export function Alert({ title, children, tone = 'info' }: { title: string; children: React.ReactNode; tone?: AlertTone }) {
  const mark = { info: 'i', success: '✓', warning: '!', danger: '×' }[tone];
  return <div className={`alert alert--${tone}`} role={tone === 'danger' ? 'alert' : 'status'}>
    <span className="alert-mark" aria-hidden="true">{mark}</span><div><strong>{title}</strong><div>{children}</div></div>
  </div>;
}
