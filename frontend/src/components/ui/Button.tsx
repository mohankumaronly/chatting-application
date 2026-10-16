import { forwardRef, type ButtonHTMLAttributes } from "react";
import { Loader2 } from "lucide-react";
import { cn } from "../../lib/utils/cn";

type Variant = "primary" | "secondary" | "ghost" | "danger";
type Size = "sm" | "md" | "lg";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant;
  size?: Size;
  isLoading?: boolean;
  fullWidth?: boolean;
};

const variantMap: Record<Variant, string> = {
  primary:
    "bg-primary-600 hover:bg-primary-700 active:bg-primary-800 text-white shadow-md shadow-primary-600/25 hover:shadow-lg hover:shadow-primary-600/30",
  secondary:
    "bg-white border-2 border-border-light hover:border-primary-300 hover:bg-primary-50 text-text-primary",
  ghost: "bg-transparent hover:bg-surface-tertiary text-text-primary",
  danger: "bg-danger hover:bg-red-700 active:bg-red-800 text-white",
};

const sizeMap: Record<Size, string> = {
  sm: "px-4 py-2 text-sm rounded-lg",
  md: "px-6 py-3 text-sm font-semibold rounded-xl",
  lg: "px-8 py-3.5 text-base font-semibold rounded-xl",
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = "primary",
      size = "md",
      isLoading = false,
      fullWidth = false,
      disabled,
      className,
      children,
      ...rest
    },
    ref
  ) => {
    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn(
          "inline-flex items-center justify-center gap-2 transition-all duration-200",
          "disabled:opacity-60 disabled:cursor-not-allowed",
          "hover:-translate-y-0.5 active:translate-y-0",
          variantMap[variant],
          sizeMap[size],
          fullWidth && "w-full",
          className
        )}
        {...rest}
      >
        {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
        {children}
      </button>
    );
  }
);

Button.displayName = "Button";