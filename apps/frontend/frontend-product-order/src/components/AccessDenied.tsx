import { useUserRole } from "./Security/useUserRole";

interface AccessDeniedProps {
  requiredRole: "USER" | "ADMIN";
  currentPath: string;
}

export function AccessDenied({ requiredRole, currentPath }: AccessDeniedProps) {
  const { role } = useUserRole();

  const getMessage = () => {
    if (requiredRole === "USER" && role === "ADMIN") {
      if (currentPath.includes("orders")) {
        return {
          title: "🚫 Orders Access Restricted",
          message:
            "Administrators cannot access order management functionality.",
          reason:
            "Admin accounts are designed for product and system management only.",
        };
      }
      if (currentPath.includes("profile")) {
        return {
          title: "🚫 Profile Access Restricted",
          message: "Administrators do not have personal profiles.",
          reason:
            "Admin accounts are system-level accounts without personal information.",
        };
      }
    }

    return {
      title: "🚫 Access Denied",
      message: `This section requires ${requiredRole} role access.`,
      reason: `Your current role (${role}) does not have permission to access this area.`,
    };
  };

  const { title, message, reason } = getMessage();

  return (
    <div className="container">
      <div className="access-denied-container">
        <div className="access-denied-content">
          <h2>{title}</h2>
          <p className="access-denied-message">{message}</p>
          <p className="access-denied-reason">{reason}</p>

          <div className="access-denied-actions">
            <button
              className="btn btn-primary"
              onClick={() => window.history.back()}
            >
              ← Go Back
            </button>
            <a href="/dashboard" className="btn btn-secondary">
              🏠 Dashboard
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
