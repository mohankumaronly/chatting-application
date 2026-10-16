import { forwardRef, type InputHTMLAttributes } from "react";
import { cn } from "../../lib/utils/cn";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  error?: string;
  hint?: string;
};

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, hint, className, id, ...rest }, ref) => {
    const inputId = id || rest.name;
    return (
      <div className="space-y-1.5">
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-text-primary"
          >
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={cn(
            "w-full px-4 py-3 rounded-xl border bg-white text-text-primary placeholder:text-text-tertiary",
            "transition-colors duration-150",
            "focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-500",
            error
              ? "border-danger focus:border-danger focus:ring-danger/20"
              : "border-border-light hover:border-text-tertiary",
            className
          )}
          {...rest}
        />
        {error ? (
          <p className="text-xs text-danger">{error}</p>
        ) : hint ? (
          <p className="text-xs text-text-tertiary">{hint}</p>
        ) : null}
      </div>
    );
  }
);

Input.displayName = "Input";