import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import WelcomePage from "./features/auth/pages/WelcomePage";
import RegisterStartPage from "./features/auth/pages/RegisterStartPage";
import RegisterOtpPage from "./features/auth/pages/RegisterOtpPage";
import RegisterCompletePage from "./features/auth/pages/RegisterCompletePage";
import RegisterUsernamePage from "./features/auth/pages/RegisterUsernamePage";
import RegisterFinishPage from "./features/auth/pages/RegisterFinishPage";
import LoginPage from "./features/auth/pages/LoginPage";
import ChatHomePage from "./features/chat/pages/ChatHomePage";

// ← Two separate imports, one per file
import { RegistrationGuard } from "./features/auth/components/RouteGuard";
import { AuthGuard } from "./features/auth/components/AuthGuard";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/" element={<WelcomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/forgot-password"
            element={<div className="p-10">Coming soon</div>}
          />

          {/* Registration flow */}
          <Route path="/register" element={<RegisterStartPage />} />
          <Route
            path="/register/otp"
            element={
              <RegistrationGuard requires="otp">
                <RegisterOtpPage />
              </RegistrationGuard>
            }
          />
          <Route
            path="/register/complete"
            element={
              <RegistrationGuard requires="complete">
                <RegisterCompletePage />
              </RegistrationGuard>
            }
          />
          <Route
            path="/register/username"
            element={
              <RegistrationGuard requires="username">
                <RegisterUsernamePage />
              </RegistrationGuard>
            }
          />
          <Route
            path="/register/finish"
            element={
              <RegistrationGuard requires="finish">
                <RegisterFinishPage />
              </RegistrationGuard>
            }
          />

          {/* Protected */}
          <Route
            path="/app"
            element={
              <AuthGuard>
                <ChatHomePage />
              </AuthGuard>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;