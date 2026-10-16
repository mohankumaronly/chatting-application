import { useAuth } from "../../auth/hooks/useAuth";
import { Button } from "../../../components/ui/Button";

const ChatHomePage = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-surface-secondary flex flex-col">
      <header className="bg-white border-b border-border-light px-6 py-4 flex items-center justify-between">
        <div>
          <p className="text-sm text-text-secondary">Signed in as</p>
          <p className="font-semibold text-text-primary">
            {user?.firstName
              ? `${user.firstName} ${user.lastName ?? ""}`.trim()
              : user?.username ?? user?.email}
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={logout}>
          Log out
        </Button>
      </header>

      <main className="flex-1 flex items-center justify-center">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-bold text-text-primary">
            🎉 You're in!
          </h1>
          <p className="text-text-secondary">
            Chat home is coming in the next phase.
          </p>
        </div>
      </main>
    </div>
  );
};

export default ChatHomePage;