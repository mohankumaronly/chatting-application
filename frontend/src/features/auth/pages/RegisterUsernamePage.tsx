import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Check, X } from "lucide-react";
import { AuthLayout } from "../components/AuthLayout";
import { Input } from "../../../components/ui/Input";
import { Button } from "../../../components/ui/Button";
import { authApi } from "../api/authApi";
import { useRegistrationStore } from "../../../stores/registrationStore";

const schema = z.object({
  username: z
    .string()
    .min(3, "At least 3 characters")
    .max(24, "At most 24 characters")
    .regex(/^[a-zA-Z][a-zA-Z0-9]*$/, {
      message: "Letters and numbers only. Must start with a letter.",
    }),
});

type FormValues = z.infer<typeof schema>;

type Availability = "idle" | "checking" | "available" | "taken";

const RegisterUsernamePage = () => {
  const navigate = useNavigate();
  const { registrationId, challengeToken, setUsernameSet } =
    useRegistrationStore();
  const [serverError, setServerError] = useState<string | null>(null);
  const [availability, setAvailability] = useState<Availability>("idle");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { username: "" },
    mode: "onChange",
  });

  const username = watch("username");

  // Debounced availability check
  useEffect(() => {
    if (!username || username.length < 3 || errors.username) {
      setAvailability("idle");
      return;
    }
    setAvailability("checking");
    const t = setTimeout(async () => {
      try {
        const res = await authApi.checkUsernameAvailable(username);
        setAvailability(res.available ? "available" : "taken");
      } catch {
        setAvailability("idle");
      }
    }, 450);
    return () => clearTimeout(t);
  }, [username, errors.username]);

  const onSubmit = async (values: FormValues) => {
    if (!registrationId || !challengeToken) return;
    setServerError(null);
    try {
      await authApi.setUsername(registrationId, challengeToken, {
        username: values.username.trim().toLowerCase(),
      });
      setUsernameSet(true);
      navigate("/register/finish");
    } catch (err) {
      setServerError((err as Error).message);
    }
  };

  return (
    <AuthLayout
      step={4}
      title="Pick a username"
      subtitle="This is how others will find and mention you."
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div className="relative">
          <Input
            label="Username"
            placeholder="mohandev"
            autoComplete="off"
            autoCapitalize="off"
            spellCheck={false}
            error={errors.username?.message}
            {...register("username")}
          />
          {/* Availability badge */}
          {!errors.username && username.length >= 3 && (
            <div className="absolute right-3 top-9.5 flex items-center gap-1 text-xs">
              {availability === "checking" && (
                <span className="text-text-tertiary">checking…</span>
              )}
              {availability === "available" && (
                <span className="inline-flex items-center gap-1 text-success">
                  <Check className="w-3.5 h-3.5" /> available
                </span>
              )}
              {availability === "taken" && (
                <span className="inline-flex items-center gap-1 text-danger">
                  <X className="w-3.5 h-3.5" /> taken
                </span>
              )}
            </div>
          )}
        </div>

        {/* Live rules checklist */}
        <UsernameRules username={username} />

        {serverError && (
          <div className="text-sm text-danger bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {serverError}
          </div>
        )}

        <Button
          type="submit"
          size="lg"
          fullWidth
          isLoading={isSubmitting}
          disabled={availability === "taken"}
        >
          Continue
        </Button>
      </form>
    </AuthLayout>
  );
};

/* ---------- Live rules checklist ---------- */

type RuleProps = { ok: boolean; label: string };

const Rule = ({ ok, label }: RuleProps) => (
  <li
    className={`flex items-center gap-2 text-xs ${
      ok ? "text-success" : "text-text-tertiary"
    }`}
  >
    <span
      className={`w-4 h-4 rounded-full flex items-center justify-center border ${
        ok ? "bg-success/10 border-success" : "border-border-light"
      }`}
    >
      {ok && <Check className="w-3 h-3" />}
    </span>
    {label}
  </li>
);

const UsernameRules = ({ username }: { username: string }) => {
  const rules = [
    { ok: username.length >= 3 && username.length <= 24, label: "3–24 characters" },
    { ok: /^[a-zA-Z]/.test(username), label: "Starts with a letter" },
    { ok: /^[a-zA-Z0-9]*$/.test(username), label: "Letters and numbers only" },
  ];

  return (
    <ul className="space-y-1.5 bg-surface-secondary border border-border-light rounded-xl p-3">
      {rules.map((r) => (
        <Rule key={r.label} ok={r.ok && username.length > 0} label={r.label} />
      ))}
    </ul>
  );
};

export default RegisterUsernamePage;