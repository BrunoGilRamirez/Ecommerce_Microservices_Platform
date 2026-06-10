/* eslint-disable @typescript-eslint/no-unused-vars */
import { useState } from "react";
import { authenticatedFetch, redirectToReauth } from "./Security/auth";
import { useUserRole } from "./Security/useUserRole";
import { Link } from "react-router-dom";

interface DashboardProps {
  accessToken: string | null;
}

export function Dashboard({ accessToken }: DashboardProps) {
  const [apiResponse, setApiResponse] = useState<string>("");
  const { role, isAdmin, isUser, isLoading } = useUserRole();

  const handleReauth = async () => {
    try {
      setApiResponse("Redirecting for re-authentication...");
      await redirectToReauth();
      setApiResponse("Redirecting to obtain new token...");
    } catch (error) {
      setApiResponse(
        `Error: ${error instanceof Error ? error.message : "Unknown error"}`
      );
    }
  };

  const testAuthenticatedRequest = async () => {
    try {
      setApiResponse("Testing connection...");
      const response = await authenticatedFetch(
        "http://localhost:8080/gateway/test",
        {
          method: "GET",
        }
      );

      if (response.ok) {
        const data = await response.text();
        setApiResponse(`API Response: ${data}`);
      } else {
        setApiResponse(`API Error: ${response.status} ${response.statusText}`);
      }
    } catch (error) {
      setApiResponse(`Request Error: ${error}`);
    }
  };

  return (
    <div className="container">
      <h2 className="auth-title">
        Dashboard {isAdmin && <span className="admin-badge">ADMIN</span>}
      </h2>

      {/* Información específica por rol */}
      {isAdmin && (
        <div className="card admin-welcome">
          <div className="card-header">
            <h3>🛠️ Administrator Panel</h3>
          </div>
          <div className="card-body">
            <p>
              Welcome, Administrator! You have access to advanced management
              features:
            </p>
            <div className="feature-grid">
              <div className="feature-item">
                <h4>📦 Product Management</h4>
                <p>Create, edit, and delete products across all categories</p>
                <Link to="/products" className="btn btn-primary btn-sm">
                  Manage Products
                </Link>
              </div>
              <div className="feature-item">
                <h4>📊 System Overview</h4>
                <p>Monitor system performance and connectivity</p>
              </div>
            </div>
            <div className="admin-restrictions">
              <h4>⚠️ Access Restrictions</h4>
              <p>As an administrator, you cannot:</p>
              <ul>
                <li>Create or manage personal orders</li>
                <li>Access user profile settings</li>
              </ul>
            </div>
          </div>
        </div>
      )}

      {isUser && (
        <div className="card user-welcome">
          <div className="card-header">
            <h3>🛒 Welcome to Product Store</h3>
          </div>
          <div className="card-body">
            <p>Explore our products and manage your orders:</p>
            <div className="feature-grid">
              <div className="feature-item">
                <h4>🛍️ Browse Products</h4>
                <p>Discover our catalog and add items to your orders</p>
                <Link to="/products" className="btn btn-primary btn-sm">
                  Browse Products
                </Link>
              </div>
              <div className="feature-item">
                <h4>📋 My Orders</h4>
                <p>View and manage your order history</p>
                <Link to="/orders" className="btn btn-secondary btn-sm">
                  View Orders
                </Link>
              </div>
              <div className="feature-item">
                <h4>👤 My Profile</h4>
                <p>Update your personal information</p>
                <Link to="/profile" className="btn btn-outline btn-sm">
                  Manage Profile
                </Link>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Sección de Authentication Status - Solo visible para Administradores */}
      {isAdmin && (
        <div className="card">
          <div className="card-header">
            <h3>Authentication Status</h3>
          </div>
          <div className="card-body">
            <div className="auth-info-grid">
              <div className="token-section">
                <h4>Access Token:</h4>
                <code className="token-display">
                  {accessToken
                    ? accessToken.substring(0, 50) + "..."
                    : "Not available"}
                </code>
              </div>

              <div className="role-section">
                <h4>Current Role:</h4>
                <span className={`role-badge role-${role?.toLowerCase()}`}>
                  {role || "Loading..."}
                </span>
              </div>
            </div>

            <div className="button-group">
              <button className="btn btn-secondary" onClick={handleReauth}>
                🔄 Renew Token
              </button>
              <button
                className="btn btn-primary"
                onClick={testAuthenticatedRequest}
              >
                🧪 Test API Connection
              </button>
            </div>

            {apiResponse && (
              <div className="api-response">
                <h4>Response:</h4>
                <pre>{apiResponse}</pre>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
