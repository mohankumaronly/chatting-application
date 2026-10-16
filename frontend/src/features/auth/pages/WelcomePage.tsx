import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Shield, Zap, Phone } from "lucide-react";
import { Logo } from "../../../components/ui/Logo";
import { Button } from "../../../components/ui/Button";

const WelcomePage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 via-white to-primary-50 flex flex-col">
      {/* Header */}
      <header className="w-full px-6 py-5 max-w-7xl mx-auto">
        <Logo />
      </header>

      {/* Main */}
      <main className="flex-1 flex items-center justify-center px-6 py-10">
        <div className="w-full max-w-6xl grid lg:grid-cols-2 gap-16 items-center">
          {/* Left */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="space-y-8"
          >
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-primary-100 text-primary-700 text-sm font-medium">
              <span className="w-2 h-2 rounded-full bg-primary-600 animate-pulse" />
              Secure & Private Messaging
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-text-primary leading-tight tracking-tight">
              Welcome to <span className="text-primary-600">ChatApp</span>
            </h1>

            <p className="text-lg text-text-secondary leading-relaxed max-w-lg">
              Simple, reliable and private. Message privately, make calls and
              share files with your friends, family and colleagues.
            </p>

            <div className="grid sm:grid-cols-3 gap-4 max-w-lg">
              <FeaturePill icon={<Shield className="w-5 h-5" />} label="End-to-end encrypted" />
              <FeaturePill icon={<Zap className="w-5 h-5" />} label="Fast & reliable" />
              <FeaturePill icon={<Phone className="w-5 h-5" />} label="Voice & video calls" />
            </div>

            <div className="flex flex-col sm:flex-row gap-4 pt-4">
              <Button size="lg" onClick={() => navigate("/register")}>
                Get Started
              </Button>
              <Button
                size="lg"
                variant="secondary"
                onClick={() => navigate("/login")}
              >
                I already have an account
              </Button>
            </div>
          </motion.div>

          {/* Right — Phone Mockup */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="hidden lg:flex justify-center"
          >
            <PhoneMockup />
          </motion.div>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full py-6 text-center text-sm text-text-tertiary">
        © {new Date().getFullYear()} ChatApp. Built with privacy in mind.
      </footer>
    </div>
  );
};

/* ---------- Local sub-components ---------- */

const FeaturePill = ({
  icon,
  label,
}: {
  icon: React.ReactNode;
  label: string;
}) => (
  <div className="flex flex-col gap-2 p-3 rounded-xl bg-white border border-border-light shadow-sm">
    <div className="w-9 h-9 rounded-lg bg-primary-100 text-primary-600 flex items-center justify-center">
      {icon}
    </div>
    <span className="text-sm font-medium text-text-primary leading-snug">
      {label}
    </span>
  </div>
);

const PhoneMockup = () => (
  <div className="relative">
    <div className="absolute inset-0 bg-primary-400 blur-3xl opacity-20 rounded-full" />

    <div className="relative w-[320px] h-[640px] bg-white rounded-[3rem] border-[10px] border-text-primary shadow-2xl overflow-hidden">
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-6 bg-text-primary rounded-b-2xl z-10" />

      <div className="h-full flex flex-col bg-surface-secondary">
        <div className="bg-primary-600 text-white px-4 pt-10 pb-3 flex items-center justify-between">
          <span className="font-semibold">Chats</span>
          <div className="w-8 h-8 rounded-full bg-primary-500 flex items-center justify-center text-xs font-semibold">
            M
          </div>
        </div>

        <div className="flex-1 p-3 space-y-2">
          {[
            { name: "Aarav", msg: "Hey! How are you?", time: "2:34 PM", unread: 2 },
            { name: "Priya", msg: "Sent you the files 📎", time: "1:12 PM", unread: 0 },
            { name: "Team Design", msg: "Rohit: Meeting at 4?", time: "12:05 PM", unread: 5 },
            { name: "Dad", msg: "Call me when free", time: "11:02 AM", unread: 0 },
          ].map((chat, i) => (
            <div
              key={i}
              className="bg-white rounded-xl p-3 flex items-center gap-3 shadow-sm"
            >
              <div className="w-10 h-10 rounded-full bg-primary-100 text-primary-600 flex items-center justify-center font-semibold text-sm">
                {chat.name[0]}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-baseline">
                  <span className="font-semibold text-sm text-text-primary truncate">
                    {chat.name}
                  </span>
                  <span className="text-[10px] text-text-tertiary ml-2">
                    {chat.time}
                  </span>
                </div>
                <p className="text-xs text-text-secondary truncate">
                  {chat.msg}
                </p>
              </div>
              {chat.unread > 0 && (
                <div className="w-5 h-5 rounded-full bg-primary-600 text-white text-[10px] flex items-center justify-center font-bold">
                  {chat.unread}
                </div>
              )}
            </div>
          ))}
        </div>

        <div className="px-3 pb-3">
          <div className="flex justify-end">
            <div className="bg-primary-600 text-white text-xs px-3 py-2 rounded-2xl rounded-tr-sm max-w-[70%]">
              See you soon! 👋
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
);

export default WelcomePage;