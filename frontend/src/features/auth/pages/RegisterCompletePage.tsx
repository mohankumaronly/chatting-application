import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { AuthLayout } from "../components/AuthLayout";
import { Input } from "../../../components/ui/Input";
import { Button } from "../../../components/ui/Button";
import { authApi } from "../api/authApi";
import { useRegistrationStore } from "../../../stores/registrationStore";

const schema = z
  .object({
    firstName: z.string().min(1, "First name is required").max(50),
    lastName: z.string().min(1, "Last name is required").max(50),
    password: z
      .string()
      .min(8, "Password must be at least 8 characters")
      .regex(/[A-Z]/, "Must contain an uppercase letter")
      .regex(/[a-z]/, "Must contain a lowercase letter")
      .regex(/[0-9]/, "Must contain a number"),
    confirmPassword: z.string(),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

type FormValues = z.infer<typeof schema>;

const RegisterCompletePage = () => {
  const navigate = useNavigate();
  const { registrationId, challengeToken, setProfileCompleted } =
    useRegistrationStore();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      firstName: "",
      lastName: "",
      password: "",
      confirmPassword: "",
    },
  });

  const onSubmit = async (values: FormValues) => {
    if (!registrationId || !challengeToken) return;
    setServerError(null);
    try {
      await authApi.completeRegistration(registrationId, challengeToken, {
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        password: values.password,
        keepLoggedIn: true,
        clientType: "WEB",
      });
      setProfileCompleted(true);
      navigate("/register/username");
    } catch (err) {
      setServerError((err as Error).message);
    }
  };

  return (
    <AuthLayout
      step={3}
      title="Tell us about you"
      subtitle="Set your name and a strong password."
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="First name"
            placeholder="Mohan"
            error={errors.firstName?.message}
            {...register("firstName")}
          />
          <Input
            label="Last name"
            placeholder="Kumar"
            error={errors.lastName?.message}
            {...register("lastName")}
          />
        </div>

        <Input
          label="Password"
          type="password"
          placeholder="At least 8 characters"
          autoComplete="new-password"
          error={errors.password?.message}
          hint="Must include uppercase, lowercase, and a number"
          {...register("password")}
        />

        <Input
          label="Confirm Password"
          type="password"
          placeholder="Repeat your password"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          {...register("confirmPassword")}
        />

        {serverError && (
          <div className="text-sm text-danger bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {serverError}
          </div>
        )}

        <Button type="submit" size="lg" fullWidth isLoading={isSubmitting}>
          Continue
        </Button>
      </form>
    </AuthLayout>
  );
};

export default RegisterCompletePage;