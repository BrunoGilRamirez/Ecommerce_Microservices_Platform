import { useState, useRef } from "react";
import { authenticatedFetch } from "../Security/auth";
import { PRODUCT_SERVICE } from "../../config";
import "../../styles/components/admin-forms.css";

interface ProductFormData {
  name: string;
  price: number;
  category: string;
  imageUrl: string;
  stock: number;
  description?: string;
  // Smartphone fields
  brand?: string;
  operatingSystem?: string;
  storageCapacity?: number;
  ram?: number;
  processor?: string;
  screenSize?: number;
  // Clothes fields
  size?: string;
  color?: string;
  fabricType?: string;
}

const PRODUCT_CATEGORIES = [
  { value: "smartphone", label: "Smartphone" },
  { value: "clothes", label: "Clothes" },
  { value: "electronics", label: "Electronics (Generic)" },
];

export function AdminProductForm() {
  const [formData, setFormData] = useState<ProductFormData>({
    name: "",
    price: 0,
    category: "smartphone",
    imageUrl: "",
    stock: 0,
    description: "",
  });

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);
  const formRef = useRef<HTMLFormElement>(null);

  const handleInputChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
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

  const resetForm = () => {
    setFormData({
      name: "",
      price: 0,
      category: "smartphone",
      imageUrl: "",
      stock: 0,
      description: "",
    });
    if (formRef.current) {
      formRef.current.reset();
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      let endpoint = "";
      const payload = { ...formData };

      // Determinar endpoint basado en categoría
      switch (formData.category) {
        case "smartphone":
          endpoint = `${PRODUCT_SERVICE}/products/smartphones`;
          // Asegurar que todos los campos requeridos estén presentes
          if (!formData.brand || !formData.operatingSystem) {
            throw new Error(
              "Brand and Operating System are required for smartphones"
            );
          }
          break;
        case "clothes":
          endpoint = `${PRODUCT_SERVICE}/products/clothes`;
          // Asegurar que todos los campos requeridos estén presentes
          if (!formData.brand || !formData.size || !formData.color) {
            throw new Error("Brand, Size and Color are required for clothes");
          }
          break;
        default:
          endpoint = `${PRODUCT_SERVICE}/products`;
          break;
      }

      const response = await authenticatedFetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error: ${response.status}`);
      }

      await response.json(); // Consumir respuesta
      setMessage({
        type: "success",
        text: `Product "${formData.name}" created successfully!`,
      });
      resetForm();
    } catch (error) {
      console.error("Error creating product:", error);
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Error creating product",
      });
    } finally {
      setLoading(false);
    }
  };

  const renderCategorySpecificFields = () => {
    switch (formData.category) {
      case "smartphone":
        return (
          <>
            <div className="form-group">
              <label htmlFor="brand">Brand *</label>
              <input
                type="text"
                id="brand"
                name="brand"
                value={formData.brand || ""}
                onChange={handleInputChange}
                required
                placeholder="e.g., Apple, Samsung"
              />
            </div>

            <div className="form-group">
              <label htmlFor="operatingSystem">Operating System *</label>
              <input
                type="text"
                id="operatingSystem"
                name="operatingSystem"
                value={formData.operatingSystem || ""}
                onChange={handleInputChange}
                required
                placeholder="e.g., iOS, Android"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="storageCapacity">Storage (GB)</label>
                <input
                  type="number"
                  id="storageCapacity"
                  name="storageCapacity"
                  value={formData.storageCapacity || ""}
                  onChange={handleInputChange}
                  min="0"
                  placeholder="256"
                />
              </div>

              <div className="form-group">
                <label htmlFor="ram">RAM (GB)</label>
                <input
                  type="number"
                  id="ram"
                  name="ram"
                  value={formData.ram || ""}
                  onChange={handleInputChange}
                  min="0"
                  placeholder="8"
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="processor">Processor</label>
              <input
                type="text"
                id="processor"
                name="processor"
                value={formData.processor || ""}
                onChange={handleInputChange}
                placeholder="e.g., A17 Pro, Snapdragon 8 Gen 3"
              />
            </div>

            <div className="form-group">
              <label htmlFor="screenSize">Screen Size (inches)</label>
              <input
                type="number"
                id="screenSize"
                name="screenSize"
                value={formData.screenSize || ""}
                onChange={handleInputChange}
                min="0"
                step="0.1"
                placeholder="6.1"
              />
            </div>
          </>
        );

      case "clothes":
        return (
          <>
            <div className="form-group">
              <label htmlFor="brand">Brand *</label>
              <input
                type="text"
                id="brand"
                name="brand"
                value={formData.brand || ""}
                onChange={handleInputChange}
                required
                placeholder="e.g., Nike, Adidas"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="size">Size *</label>
                <input
                  type="text"
                  id="size"
                  name="size"
                  value={formData.size || ""}
                  onChange={handleInputChange}
                  required
                  placeholder="e.g., M, L, XL"
                />
              </div>

              <div className="form-group">
                <label htmlFor="color">Color *</label>
                <input
                  type="text"
                  id="color"
                  name="color"
                  value={formData.color || ""}
                  onChange={handleInputChange}
                  required
                  placeholder="e.g., Blue, Red"
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="fabricType">Fabric Type</label>
              <input
                type="text"
                id="fabricType"
                name="fabricType"
                value={formData.fabricType || ""}
                onChange={handleInputChange}
                placeholder="e.g., Cotton, Polyester"
              />
            </div>
          </>
        );

      default:
        return null;
    }
  };

  return (
    <div className="admin-form-container">
      <h3>Create New Product</h3>

      {message && (
        <div
          className={`alert ${
            message.type === "success" ? "alert-success" : "alert-danger"
          }`}
        >
          {message.text}
        </div>
      )}

      <form ref={formRef} onSubmit={handleSubmit} className="admin-form">
        <div className="form-group">
          <label htmlFor="category">Category *</label>
          <select
            id="category"
            name="category"
            value={formData.category}
            onChange={handleInputChange}
            required
          >
            {PRODUCT_CATEGORIES.map((cat) => (
              <option key={cat.value} value={cat.value}>
                {cat.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="name">Product Name *</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            required
            placeholder="Enter product name"
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="price">Price ($) *</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleInputChange}
              required
              min="0"
              step="0.01"
              placeholder="0.00"
            />
          </div>

          <div className="form-group">
            <label htmlFor="stock">Stock *</label>
            <input
              type="number"
              id="stock"
              name="stock"
              value={formData.stock}
              onChange={handleInputChange}
              required
              min="0"
              placeholder="0"
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="imageUrl">Image URL</label>
          <input
            type="url"
            id="imageUrl"
            name="imageUrl"
            value={formData.imageUrl}
            onChange={handleInputChange}
            placeholder="https://example.com/image.jpg"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description || ""}
            onChange={handleInputChange}
            placeholder="Product description..."
            rows={3}
          />
        </div>

        {renderCategorySpecificFields()}

        <div className="form-actions">
          <button
            type="button"
            onClick={resetForm}
            className="btn btn-secondary"
            disabled={loading}
          >
            Reset
          </button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? "Creating..." : "Create Product"}
          </button>
        </div>
      </form>
    </div>
  );
}
