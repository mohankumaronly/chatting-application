import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthLayout } from "../components/AuthLayout";
import { Button } from "../../../components/ui/Button";
import { authApi } from "../api/authApi";
import { useRegistrationStore } from "../../../stores/registrationStore";

const OTP_LENGTH = 6;

const RegisterOtpPage = () => {
  const navigate = useNavigate();
  const {
    registrationId,
    challengeToken,
    email,
    setOtpVerified,
    setChallengeToken,
  } = useRegistrationStore();

  const [digits, setDigits] = useState<string[]>(Array(OTP_LENGTH).fill(""));
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);
  const inputsRef = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    inputsRef.current[0]?.focus();
  }, []);

  useEffect(() => {
    if (resendCooldown <= 0) return;
    const t = setInterval(() => setResendCooldown((c) => c - 1), 1000);
    return () => clearInterval(t);
  }, [resendCooldown]);

  const handleChange = (index: number, value: string) => {
    const cleaned = value.replace(/\D/g, "").slice(-1);
    const next = [...digits];
    next[index] = cleaned;
    setDigits(next);
    if (cleaned && index < OTP_LENGTH - 1) {
      inputsRef.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (
    index: number,
    e: React.KeyboardEvent<HTMLInputElement>
  ) => {
    if (e.key === "Backspace" && !digits[index] && index > 0) {
      inputsRef.current[index - 1]?.focus();
    }
    if (e.key === "ArrowLeft" && index > 0) {
      inputsRef.current[index - 1]?.focus();
    }
    if (e.key === "ArrowRight" && index < OTP_LENGTH - 1) {
      inputsRef.current[index + 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, OTP_LENGTH);
    if (!pasted) return;
    const next = Array(OTP_LENGTH).fill("");
    for (let i = 0; i < pasted.length; i++) next[i] = pasted[i];
    setDigits(next);
    inputsRef.current[Math.min(pasted.length, OTP_LENGTH - 1)]?.focus();
  };

  const handleVerify = async () => {
    const otp = digits.join("");
    if (otp.length !== OTP_LENGTH) {
      setError("Please enter all 6 digits");
      return;
    }
    if (!registrationId || !challengeToken) {
      setError("Session expired. Please start over.");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await authApi.verifyOtp(registrationId, challengeToken, { otp });
      setOtpVerified(true);
      navigate("/register/complete");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!registrationId || !challengeToken) return;
    setResending(true);
    setError(null);
    try {
      const res = await authApi.resendOtp(registrationId, challengeToken);
      if (res?.challengeToken) setChallengeToken(res.challengeToken);
      setResendCooldown(30);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setResending(false);
    }
  };

  // auto-verify when all digits entered
  useEffect(() => {
    if (digits.every((d) => d !== "") && !loading) {
      handleVerify();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [digits]);

  return (
    <AuthLayout
      step={2}
      title="Verify your email"
      subtitle={`We sent a 6-digit code to ${email ?? "your email"}`}
    >
      <div className="space-y-6">
        <div
          className="flex gap-2 justify-between"
          onPaste={handlePaste}
        >
          {digits.map((d, i) => (
            <input
              key={i}
              ref={(el) => {
                inputsRef.current[i] = el;
              }}
              inputMode="numeric"
              maxLength={1}
              value={d}
              onChange={(e) => handleChange(i, e.target.value)}
              onKeyDown={(e) => handleKeyDown(i, e)}
              className="w-12 h-14 text-center text-xl font-semibold rounded-xl border border-border-light focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-500 transition-colors"
            />
          ))}
        </div>

        {error && (
          <div className="text-sm text-danger bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {error}
          </div>
        )}

        <Button
          size="lg"
          fullWidth
          isLoading={loading}
          onClick={handleVerify}
        >
          Verify
        </Button>

        <div className="text-center text-sm text-text-secondary">
          Didn't receive the code?{" "}
          <button
            type="button"
            disabled={resending || resendCooldown > 0}
            onClick={handleResend}
            className="text-primary-600 font-medium hover:underline disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {resendCooldown > 0
              ? `Resend in ${resendCooldown}s`
              : resending
                ? "Sending…"
                : "Resend"}
          </button>
        </div>
      </div>
    </AuthLayout>
  );
};

export default RegisterOtpPage;