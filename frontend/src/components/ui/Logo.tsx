import { MessageCircle } from "lucide-react";
import { cn } from "../../lib/utils/cn";

type LogoProps = {
  size?: "sm" | "md" | "lg";
  showText?: boolean;
  className?: string;
};

const sizeMap = {
  sm: { box: "w-7 h-7", icon: "w-4 h-4", text: "text-base" },
  md: { box: "w-9 h-9", icon: "w-5 h-5", text: "text-lg" },
  lg: { box: "w-12 h-12", icon: "w-6 h-6", text: "text-2xl" },
};

export const Logo = ({ size = "md", showText = true, className }: LogoProps) => {
  const s = sizeMap[size];
  return (
    <div className={cn("flex items-center gap-2", className)}>
      <div
        className={cn(
          "rounded-xl bg-primary-600 flex items-center justify-center shadow-md shadow-primary-600/25",
          s.box
        )}
      >
        <MessageCircle className={cn("text-white", s.icon)} strokeWidth={2.5} />
      </div>
      {showText && (
        <span className={cn("font-bold text-text-primary", s.text)}>
          ChatApp
        </span>
      )}
    </div>
  );
};