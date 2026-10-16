import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Link, useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { Input } from "../../../components/ui/Input";
import { Button } from "../../../components/ui/Button";
import { authApi } from "../api/authApi";
import { useRegistrationStore } from "../../../stores/registrationStore";
import { useState } from "react";

const schema = z.object({
  email: z.string().email("Enter a valid email"),
  dateOfBirth: z
    .string()
    .min(1, "Date of birth is required")
    .refine(
      (val) => {
        const dob = new Date(val);
        const now = new Date();
        const age =
          (now.getTime() - dob.getTime()) / (1000 * 60 * 60 * 24 * 365.25);
        return age >= 13;
      },
      { message: "You must be at least 13 years old" }
    ),
});

type FormValues = z.infer<typeof schema>;

const RegisterStartPage = () => {
  const navigate = useNavigate();
  const setStartData = useRegistrationStore((s) => s.setStartData);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: "", dateOfBirth: "" },
  });

  const onSubmit = async (values: FormValues) => {
    setServerError(null);
    try {
      const res = await authApi.startRegistration(values);
      setStartData({
        registrationId: res.registrationId,
        challengeToken: res.challengeToken,
        email: values.email,
        dateOfBirth: values.dateOfBirth,
      });
      navigate("/register/otp");
    } catch (err) {
      setServerError((err as Error).message);
    }
  };

  return (
    <AuthLayout
      step={1}
      title="Create your account"
      subtitle="We'll send a verification code to your email."
      footer={
        <>
          Already have an account?{" "}
          <Link to="/login" className="text-primary-600 font-medium hover:underline">
            Log in
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <Input
          label="Email"
          type="email"
          placeholder="you@example.com"
          autoComplete="email"
          error={errors.email?.message}
          {...register("email")}
        />

        <Input
          label="Date of Birth"
          type="date"
          error={errors.dateOfBirth?.message}
          hint="You must be at least 13 years old"
          {...register("dateOfBirth")}
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

export default RegisterStartPage;