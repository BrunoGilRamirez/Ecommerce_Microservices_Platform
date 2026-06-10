import { useState } from "react";
import { useUserRole } from "../Security/useUserRole";
import { ProductsList } from "./ProductsList";
import { AdminProductForm } from "./AdminProductForm";
import { AdminProductsManager } from "./AdminProductsManager";

export function ProductsPage() {
  const { isAdmin, isUser } = useUserRole();
  const [activeTab, setActiveTab] = useState<"browse" | "create" | "manage">(
    "browse"
  );

  // Vista para usuarios normales
  if (isUser) {
    return (
      <div className="container">
        <div className="page-header">
          <h2 className="auth-title">Product Catalog</h2>
          <p className="page-description">
            Browse our products and add them to your order using the{" "}
            <strong>➕ Add to Order</strong> button on each card.
          </p>
        </div>
        <ProductsList />
      </div>
    );
  }

  // Vista para administradores
  if (isAdmin) {
    return (
      <div className="container">
        <div className="page-header">
          <h2 className="auth-title">Product Management</h2>
          <p className="page-description">
            Manage products, create new items, and edit existing inventory.
          </p>
        </div>

        {/* Tabs de navegación para admin */}
        <div className="admin-tabs">
          <button
            className={`admin-tab ${activeTab === "browse" ? "active" : ""}`}
            onClick={() => setActiveTab("browse")}
          >
            Browse Products
          </button>
          <button
            className={`admin-tab ${activeTab === "create" ? "active" : ""}`}
            onClick={() => setActiveTab("create")}
          >
            Create Product
          </button>
          <button
            className={`admin-tab ${activeTab === "manage" ? "active" : ""}`}
            onClick={() => setActiveTab("manage")}
          >
            Manage Products
          </button>
        </div>

        {/* Contenido basado en tab activo */}
        <div className="admin-content">
          {activeTab === "browse" && <ProductsList adminMode={true} />}
          {activeTab === "create" && <AdminProductForm />}
          {activeTab === "manage" && <AdminProductsManager />}
        </div>
      </div>
    );
  }

  // Fallback
  return (
    <div className="container">
      <div className="page-header">
        <h2 className="auth-title">Product Catalog</h2>
        <p className="page-description">Loading...</p>
      </div>
    </div>
  );
}
