import { Link, useLocation } from "react-router-dom";
import { useUserRole } from "./Security/useUserRole";
import "../styles/components/navigation.css";

interface NavigationProps {
  onLogout: () => void;
}

export function Navigation({ onLogout }: NavigationProps) {
  const location = useLocation();
  const { isAdmin, isUser, isLoading } = useUserRole();

  // No renderizar navegación mientras se carga el rol
  if (isLoading) {
    return (
      <nav className="navigation">
        <div className="nav-brand">
          <h1>Product Store</h1>
        </div>
        <div className="nav-links">
          <span>Loading...</span>
        </div>
      </nav>
    );
  }

  return (
    <nav className="navigation">
      <div className="nav-brand">
        <h1>
          Product Store {isAdmin && <span className="admin-badge">ADMIN</span>}
        </h1>
      </div>
      <div className="nav-links">
        <Link
          to="/dashboard"
          className={`nav-link ${
            location.pathname === "/dashboard" ? "active" : ""
          }`}
        >
          Dashboard
        </Link>
        <Link
          to="/products"
          className={`nav-link ${
            location.pathname === "/products" ? "active" : ""
          }`}
        >
          Products
        </Link>

        {/* Orders solo para usuarios normales, no para admins */}
        {isUser && (
          <Link
            to="/orders"
            className={`nav-link ${
              location.pathname === "/orders" ? "active" : ""
            }`}
          >
            Orders
          </Link>
        )}
      </div>
      <div className="nav-actions">
        {/* Profile solo para usuarios normales, no para admins */}
        {isUser && (
          <Link
            to="/profile"
            className={`btn btn-outline ${
              location.pathname === "/profile" ? "active" : ""
            }`}
          >
            Profile
          </Link>
        )}
        <button className="btn btn-danger btn-sm" onClick={onLogout}>
          Log out
        </button>
      </div>
    </nav>
  );
}
