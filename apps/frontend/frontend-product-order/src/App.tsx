import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { login } from "./components/Security/auth";
import { useAuth } from "./components/Security/useAuth";
import { useUserRole } from "./components/Security/useUserRole";
import { Navigation } from "./components/Navigation";
import { Dashboard } from "./components/Dashboard";
import { ProductsPage } from "./components/products/ProductsPage";
import { OrdersPage } from "./components/orders/OrdersPage";
import { UserPage } from "./components/user/UserPage";
import { CallbackPage } from "./components/CallbackPage";
import "./styles/index.css";

function App() {
  const { accessToken, isAuthenticated, isLoading, logout, updateAuthState } =
    useAuth();
  const { isAdmin, isUser, isLoading: roleLoading } = useUserRole();

  if (isLoading || (isAuthenticated && roleLoading))
    return (
      <div className="loading-container">
        <div className="loading"></div>
        <p className="loading-text">Loading...</p>
      </div>
    );

  if (!isAuthenticated) {
    return (
      <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <div className="app-container">
          <Routes>
            <Route
              path="/callback"
              element={<CallbackPage onAuthUpdate={updateAuthState} />}
            />
            <Route
              path="*"
              element={
                <main className="main-content">
                  <div className="auth-section">
                    <h2 className="auth-title">Login</h2>
                    <button className="btn btn-primary btn-lg" onClick={login}>
                      Login with OAuth2
                    </button>
                  </div>
                </main>
              }
            />
          </Routes>
        </div>
      </Router>
    );
  }

  return (
    <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <div className="app-container">
        <Navigation onLogout={logout} />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route
              path="/callback"
              element={<CallbackPage onAuthUpdate={updateAuthState} />}
            />
            <Route
              path="/dashboard"
              element={<Dashboard accessToken={accessToken} />}
            />
            <Route path="/products" element={<ProductsPage />} />

            {/* Rutas exclusivas para usuarios (no admin) */}
            {isUser && (
              <>
                <Route path="/orders" element={<OrdersPage />} />
                <Route path="/profile" element={<UserPage />} />
              </>
            )}

            {/* Redirigir rutas no permitidas para admins */}
            {isAdmin && (
              <>
                <Route
                  path="/orders"
                  element={<Navigate to="/dashboard" replace />}
                />
                <Route
                  path="/profile"
                  element={<Navigate to="/dashboard" replace />}
                />
              </>
            )}

            {/* Ruta catch-all */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
