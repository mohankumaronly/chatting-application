import { motion } from "framer-motion";
import { Logo } from "../../../components/ui/Logo";

type Props = {
  step?: number; // 1..5 (optional — welcome has none)
  totalSteps?: number;
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
};

export const AuthLayout = ({
  step,
  totalSteps = 5,
  title,
  subtitle,
  children,
  footer,
}: Props) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 via-white to-primary-50 flex flex-col">
      <header className="w-full px-6 py-5 max-w-7xl mx-auto flex items-center justify-between">
        <Logo />
        {step && (
          <span className="text-sm font-medium text-text-secondary">
            Step {step} of {totalSteps}
          </span>
        )}
      </header>

      <main className="flex-1 flex items-center justify-center px-6 py-8">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
          className="w-full max-w-md"
        >
          {/* Progress bar */}
          {step && (
            <div className="mb-8">
              <div className="h-1.5 w-full bg-surface-tertiary rounded-full overflow-hidden">
                <motion.div
                  className="h-full bg-primary-600 rounded-full"
                  initial={{ width: 0 }}
                  animate={{ width: `${(step / totalSteps) * 100}%` }}
                  transition={{ duration: 0.4 }}
                />
              </div>
            </div>
          )}

          <div className="mb-8">
            <h1 className="text-3xl font-bold text-text-primary tracking-tight">
              {title}
            </h1>
            {subtitle && (
              <p className="mt-2 text-text-secondary">{subtitle}</p>
            )}
          </div>

          <div className="bg-white rounded-2xl border border-border-light shadow-sm p-6 sm:p-8">
            {children}
          </div>

          {footer && <div className="mt-6 text-center text-sm">{footer}</div>}
        </motion.div>
      </main>
    </div>
  );
};