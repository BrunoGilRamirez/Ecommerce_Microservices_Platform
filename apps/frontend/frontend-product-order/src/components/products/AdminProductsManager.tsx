import { useState, useEffect } from "react";
import { authenticatedFetch } from "../Security/auth";
import { PRODUCT_SERVICE } from "../../config";
import type { Product } from "../Security/orderService";
import "../../styles/components/admin-forms.css";

interface ExtendedProduct extends Product {
  brand?: string;
  operatingSystem?: string;
  storageCapacity?: number;
  ram?: number;
  processor?: string;
  screenSize?: number;
  size?: string;
  color?: string;
  fabricType?: string;
  stock?: number;
}

export function AdminProductsManager() {
  const [products, setProducts] = useState<ExtendedProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingProduct, setEditingProduct] = useState<ExtendedProduct | null>(
    null
  );
  const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await authenticatedFetch(`${PRODUCT_SERVICE}/products`);

      if (!response.ok) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      setProducts(data.data);
    } catch (err) {
      console.error("Error fetching products:", err);
      setError(err instanceof Error ? err.message : "Error fetching products");
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (product: ExtendedProduct) => {
    setEditingProduct({ ...product });
  };

  const handleSaveEdit = async () => {
    if (!editingProduct) return;

    try {
      let endpoint = "";

      // Determinar endpoint basado en categoría
      switch (editingProduct.category) {
        case "smartphone":
          endpoint = `${PRODUCT_SERVICE}/products/smartphones/${editingProduct.id}`;
          break;
        case "clothes":
          endpoint = `${PRODUCT_SERVICE}/products/clothes/${editingProduct.id}`;
          break;
        default:
          endpoint = `${PRODUCT_SERVICE}/products/${editingProduct.id}`;
          break;
      }

      const response = await authenticatedFetch(endpoint, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(editingProduct),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error: ${response.status}`);
      }

      // Actualizar la lista de productos
      setProducts((prev) =>
        prev.map((p) => (p.id === editingProduct.id ? editingProduct : p))
      );
      setEditingProduct(null);
    } catch (error) {
      console.error("Error updating product:", error);
      setError(
        error instanceof Error ? error.message : "Error updating product"
      );
    }
  };

  const handleDelete = async (productId: number) => {
    const product = products.find((p) => p.id === productId);
    if (!product) return;

    try {
      let endpoint = "";

      // Determinar endpoint basado en categoría
      switch (product.category) {
        case "smartphone":
          endpoint = `${PRODUCT_SERVICE}/products/smartphones/${productId}`;
          break;
        case "clothes":
          endpoint = `${PRODUCT_SERVICE}/products/clothes/${productId}`;
          break;
        default:
          endpoint = `${PRODUCT_SERVICE}/products/${productId}`;
          break;
      }

      const response = await authenticatedFetch(endpoint, {
        method: "DELETE",
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error: ${response.status}`);
      }

      // Remover producto de la lista
      setProducts((prev) => prev.filter((p) => p.id !== productId));
      setDeleteConfirm(null);
    } catch (error) {
      console.error("Error deleting product:", error);
      setError(
        error instanceof Error ? error.message : "Error deleting product"
      );
    }
  };

  const handleEditInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    if (!editingProduct) return;

    const { name, value } = e.target;
    setEditingProduct((prev) => ({
      ...prev!,
      [name]:
        name === "price" ||
        name === "stock" ||
        name === "storageCapacity" ||
        name === "ram" ||
        name === "screenSize"
          ? value === ""
            ? 0
            : Number(value)
          : value,
    }));
  };

  if (loading) return <div className="loading-text">Loading products...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (
    <div className="admin-products-manager">
      <h3>Manage Products</h3>

      {products.length === 0 ? (
        <div className="no-products">No products found.</div>
      ) : (
        <div className="products-table-container">
          <table className="products-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Brand</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>{product.id}</td>
                  <td>{product.name}</td>
                  <td>
                    <span
                      className={`category-badge category-${product.category}`}
                    >
                      {product.category}
                    </span>
                  </td>
                  <td>${product.price.toFixed(2)}</td>
                  <td>{product.stock || "N/A"}</td>
                  <td>{product.brand || "N/A"}</td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={() => handleEdit(product)}
                      >
                        Edit
                      </button>
                      {deleteConfirm === product.id ? (
                        <div className="delete-confirm">
                          <button
                            className="btn btn-sm btn-danger"
                            onClick={() => handleDelete(product.id)}
                          >
                            Confirm
                          </button>
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => setDeleteConfirm(null)}
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => setDeleteConfirm(product.id)}
                        >
                          Delete
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal de edición */}
      {editingProduct && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h4>Edit Product: {editingProduct.name}</h4>

            <div className="edit-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Name</label>
                  <input
                    type="text"
                    name="name"
                    value={editingProduct.name}
                    onChange={handleEditInputChange}
                  />
                </div>
                <div className="form-group">
                  <label>Price ($)</label>
                  <input
                    type="number"
                    name="price"
                    value={editingProduct.price}
                    onChange={handleEditInputChange}
                    step="0.01"
                    min="0"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Stock</label>
                  <input
                    type="number"
                    name="stock"
                    value={editingProduct.stock || 0}
                    onChange={handleEditInputChange}
                    min="0"
                  />
                </div>
                {editingProduct.brand !== undefined && (
                  <div className="form-group">
                    <label>Brand</label>
                    <input
                      type="text"
                      name="brand"
                      value={editingProduct.brand || ""}
                      onChange={handleEditInputChange}
                    />
                  </div>
                )}
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  value={editingProduct.description || ""}
                  onChange={handleEditInputChange}
                  rows={3}
                />
              </div>

              <div className="form-group">
                <label>Image URL</label>
                <input
                  type="url"
                  name="imageUrl"
                  value={editingProduct.imageUrl || ""}
                  onChange={handleEditInputChange}
                />
              </div>

              {/* Campos específicos por categoría */}
              {editingProduct.category === "smartphone" && (
                <>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Operating System</label>
                      <input
                        type="text"
                        name="operatingSystem"
                        value={editingProduct.operatingSystem || ""}
                        onChange={handleEditInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Storage (GB)</label>
                      <input
                        type="number"
                        name="storageCapacity"
                        value={editingProduct.storageCapacity || 0}
                        onChange={handleEditInputChange}
                        min="0"
                      />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>RAM (GB)</label>
                      <input
                        type="number"
                        name="ram"
                        value={editingProduct.ram || 0}
                        onChange={handleEditInputChange}
                        min="0"
                      />
                    </div>
                    <div className="form-group">
                      <label>Screen Size (inches)</label>
                      <input
                        type="number"
                        name="screenSize"
                        value={editingProduct.screenSize || 0}
                        onChange={handleEditInputChange}
                        step="0.1"
                        min="0"
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label>Processor</label>
                    <input
                      type="text"
                      name="processor"
                      value={editingProduct.processor || ""}
                      onChange={handleEditInputChange}
                    />
                  </div>
                </>
              )}

              {editingProduct.category === "clothes" && (
                <>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Size</label>
                      <input
                        type="text"
                        name="size"
                        value={editingProduct.size || ""}
                        onChange={handleEditInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Color</label>
                      <input
                        type="text"
                        name="color"
                        value={editingProduct.color || ""}
                        onChange={handleEditInputChange}
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label>Fabric Type</label>
                    <input
                      type="text"
                      name="fabricType"
                      value={editingProduct.fabricType || ""}
                      onChange={handleEditInputChange}
                    />
                  </div>
                </>
              )}
            </div>

            <div className="modal-actions">
              <button
                className="btn btn-secondary"
                onClick={() => setEditingProduct(null)}
              >
                Cancel
              </button>
              <button className="btn btn-primary" onClick={handleSaveEdit}>
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
