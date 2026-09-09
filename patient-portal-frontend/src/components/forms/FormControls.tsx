import React, { forwardRef } from 'react';

interface FieldProps {
  id: string;
  label: string;
  required?: boolean;
  error?: string;
  help?: string;
  children: React.ReactElement<{
    id?: string;
    'aria-invalid'?: boolean;
    'aria-describedby'?: string;
    'aria-required'?: boolean;
  }>;
}

export function Field({ id, label, required, error, help, children }: FieldProps) {
  const descriptionIds = [help ? `${id}-help` : '', error ? `${id}-error` : ''].filter(Boolean).join(' ') || undefined;
  return <div className="field">
    <label htmlFor={id}>{label}{required && <span className="required" aria-hidden="true"> *</span>}</label>
    {React.cloneElement(children, { id, 'aria-invalid': Boolean(error), 'aria-describedby': descriptionIds, 'aria-required': required })}
    {help && <p className="field-help" id={`${id}-help`}>{help}</p>}
    {error && <p className="field-error" id={`${id}-error`}>{error}</p>}
  </div>;
}

export const TextField = forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>((props, ref) => <input {...props} ref={ref} className={`text-input ${props.className ?? ''}`} />);
TextField.displayName = 'TextField';

export const DateField = forwardRef<HTMLInputElement, Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'>>((props, ref) => <TextField {...props} ref={ref} type="date" />);
DateField.displayName = 'DateField';

export const Select = forwardRef<HTMLSelectElement, React.SelectHTMLAttributes<HTMLSelectElement>>((props, ref) => <select {...props} ref={ref} className={`select-input ${props.className ?? ''}`} />);
Select.displayName = 'Select';

export const TextArea = forwardRef<HTMLTextAreaElement, React.TextareaHTMLAttributes<HTMLTextAreaElement>>((props, ref) => <textarea {...props} ref={ref} className={`textarea-input ${props.className ?? ''}`} />);
TextArea.displayName = 'TextArea';

export const Checkbox = forwardRef<HTMLInputElement, Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'>>(({ children, ...props }, ref) => <label className="checkbox-label"><input {...props} ref={ref} type="checkbox" /><span>{children}</span></label>);
Checkbox.displayName = 'Checkbox';

export function ValidationSummary({ errors }: { errors: readonly { fieldId: string; message: string }[] }) {
  if (!errors.length) return null;
  return <div className="validation-summary" role="alert" tabIndex={-1}><strong>Review {errors.length} {errors.length === 1 ? 'field' : 'fields'}</strong><ul>{errors.map(error => <li key={error.fieldId}><a href={`#${error.fieldId}`}>{error.message}</a></li>)}</ul></div>;
}
