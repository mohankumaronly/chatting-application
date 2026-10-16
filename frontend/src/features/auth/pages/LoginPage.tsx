import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Eye, EyeOff } from "lucide-react";
import { AuthLayout } from "../components/AuthLayout";
import { Input } from "../../../components/ui/Input";
import { Button } from "../../../components/ui/Button";
import { useAuth } from "../hooks/useAuth";

const schema = z.object({
  identifier: z.string().min(1, "Enter your email or username"),
  password: z.string().min(1, "Enter your password"),
  keepLoggedIn: z.boolean().optional(),
});

type FormValues = z.infer<typeof schema>;

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation() as { state?: { from?: { pathname: string } } };
  const { login } = useAuth();

  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { identifier: "", password: "", keepLoggedIn: true },
  });

  const onSubmit = async (values: FormValues) => {
    setServerError(null);
    try {
      await login(values.identifier.trim(), values.password, values.keepLoggedIn ?? true);
      const from = location.state?.from?.pathname || "/app";
      navigate(from, { replace: true });
    } catch (err) {
      setServerError((err as Error).message);
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Log in to continue chatting."
      footer={
        <>
          Don't have an account?{" "}
          <Link
            to="/register"
            className="text-primary-600 font-medium hover:underline"
          >
            Sign up
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <Input
          label="Email or Username"
          type="text"
          placeholder="you@example.com or mohandev"
          autoComplete="username"
          error={errors.identifier?.message}
          {...register("identifier")}
        />

        <div className="relative">
          <Input
            label="Password"
            type={showPassword ? "text" : "password"}
            placeholder="Your password"
            autoComplete="current-password"
            error={errors.password?.message}
            {...register("password")}
          />
          <button
            type="button"
            onClick={() => setShowPassword((v) => !v)}
            className="absolute right-3 top-9.5 text-text-tertiary hover:text-text-secondary"
            aria-label={showPassword ? "Hide password" : "Show password"}
          >
            {showPassword ? (
              <EyeOff className="w-4 h-4" />
            ) : (
              <Eye className="w-4 h-4" />
            )}
          </button>
        </div>

        <div className="flex items-center justify-between">
          <label className="inline-flex items-center gap-2 text-sm text-text-secondary select-none">
            <input
              type="checkbox"
              className="w-4 h-4 rounded border-border-light text-primary-600 focus:ring-primary-500/30"
              {...register("keepLoggedIn")}
            />
            Keep me logged in
          </label>

          <Link
            to="/forgot-password"
            className="text-sm text-primary-600 font-medium hover:underline"
          >
            Forgot password?
          </Link>
        </div>

        {serverError && (
          <div className="text-sm text-danger bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {serverError}
          </div>
        )}

        <Button type="submit" size="lg" fullWidth isLoading={isSubmitting}>
          Log in
        </Button>
      </form>
    </AuthLayout>
  );
};

export default LoginPage;