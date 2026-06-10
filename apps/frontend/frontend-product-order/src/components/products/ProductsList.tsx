import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { authenticatedFetch } from "../Security/auth";
import { PRODUCT_SERVICE } from "../../config";
import { useOrdersStore } from "../Security/useOrdersStore";
import { useUserRole } from "../Security/useUserRole";
import { OrderSelectionModal } from "../orders/OrderSelectionModal";
import type { Product } from "../Security/orderService";
import "../../styles/components/products.css";

interface ProductsListProps {
  adminMode?: boolean;
}

export function ProductsList({ adminMode = false }: ProductsListProps) {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [selectedQuantity, setSelectedQuantity] = useState(1);
  const [quantities, setQuantities] = useState<{ [key: number]: number }>({});
  const [showOrderModal, setShowOrderModal] = useState(false);

  const { orders, draftOrder, createDraftOrder, addProductToOrder } =
    useOrdersStore();
  const { isUser } = useUserRole();

  useEffect(() => {
    async function fetchProducts() {
      setLoading(true);
      setError(null);
      try {
        const productsUrl = `${PRODUCT_SERVICE}/products`;
        console.log("🚀 Requesting products from:", productsUrl);

        const response = await authenticatedFetch(productsUrl);

        console.log("📡 Server response:", {
          status: response.status,
          statusText: response.statusText,
          headers: Object.fromEntries(response.headers.entries()),
        });

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        console.log("Products:", data.data);
        setProducts(data.data);

        // Initialize quantities with 1 for each product
        const initialQuantities = data.data.reduce(
          (acc: { [key: number]: number }, product: Product) => {
            acc[product.id] = 1;
            return acc;
          },
          {}
        );
        setQuantities(initialQuantities);
      } catch (err: unknown) {
        console.error("Error:", err);
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("Error fetching products");
        }
      } finally {
        setLoading(false);
      }
    }
    fetchProducts();
  }, []);

  const handleQuantityChange = (productId: number, delta: number) => {
    setQuantities((prev) => ({
      ...prev,
      [productId]: Math.max(1, (prev[productId] || 1) + delta),
    }));
  };
  const handleAddToOrder = (product: Product) => {
    const quantity = quantities[product.id] || 1;
    setSelectedProduct(product);
    setSelectedQuantity(quantity);

    // If there is a draft order, add directly
    if (draftOrder) {
      addProductToOrder("draft", product, quantity);
      // Show a more descriptive message
      const existingItem = draftOrder.items.find(
        (item) => item.product.id === product.id
      );
      if (existingItem) {
        alert(
          `✅ ${product.name} updated in your order (${
            existingItem.quantity + quantity
          } units)`
        );
      } else {
        alert(
          `✅ ${product.name} added to your in-progress order (${quantity} ${
            quantity === 1 ? "unit" : "units"
          })`
        );
      }
      return;
    }

    // If there is no draft order, show selection modal
    setShowOrderModal(true);
  };

  const handleCreateNewOrder = async () => {
    if (!selectedProduct) return;

    try {
      await createDraftOrder();
      addProductToOrder("draft", selectedProduct, selectedQuantity);
      alert(`${selectedProduct.name} added to new order`);
    } catch (error) {
      console.error("Error creating new order:", error);
      alert("Error creating new order");
    }
  };

  const handleSelectExistingOrder = (orderId: number) => {
    if (!selectedProduct) return;

    addProductToOrder(orderId, selectedProduct, selectedQuantity);
    alert(`${selectedProduct.name} added to order #${orderId}`);
  };

  if (loading) return <div className="loading-text">Loading Products...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;
  console.log("Loaded products:", products);
  if (!products.length) return <div>No products available.</div>;

  return (
    <>
      {/* Draft order indicator - solo para usuarios, no para admins */}
      {isUser && draftOrder && (
        <div className="draft-order-indicator">
          <div className="draft-order-content">
            <div className="draft-order-info">
              <span className="draft-order-icon">🛒</span>
              <div className="draft-order-text">
                <strong>Order in progress:</strong> {draftOrder.items.length}{" "}
                {draftOrder.items.length === 1 ? "product" : "products"} -
                Total: ${draftOrder.total?.toFixed(2) || "0.00"}
              </div>
            </div>
            <div className="draft-order-actions">
              <Link to="/orders" className="btn btn-primary btn-sm">
                View Order
              </Link>
            </div>
          </div>
        </div>
      )}

      {adminMode && (
        <div className="admin-products-header">
          <h4>Product Catalog (Admin View)</h4>
          <p>
            Browse all products. Use the tabs above to create or manage
            products.
          </p>
        </div>
      )}

      <div className="products-container">
        {products.map((product) => (
          <div className="product-card" key={product.id}>
            {product.imageUrl ? (
              <img
                className="product-image"
                src={product.imageUrl}
                alt={product.name}
              />
            ) : (
              <div className="product-image-placeholder">No image</div>
            )}
            <div className="product-info">
              <div className="product-title">{product.name}</div>
              <div className="product-description">{product.description}</div>
              <div className="product-price">${product.price.toFixed(2)}</div>

              {adminMode && (
                <div className="admin-product-details">
                  <span className="product-id">ID: {product.id}</span>
                  <span className="product-category">{product.category}</span>
                </div>
              )}

              {/* Solo mostrar acciones de orden para usuarios normales */}
              {isUser && !adminMode && (
                <div className="product-actions">
                  <div className="quantity-selector">
                    <button
                      className="quantity-btn"
                      onClick={() => handleQuantityChange(product.id, -1)}
                    >
                      -
                    </button>
                    <input
                      type="number"
                      className="quantity-input"
                      value={quantities[product.id] || 1}
                      onChange={(e) =>
                        setQuantities((prev) => ({
                          ...prev,
                          [product.id]: Math.max(
                            1,
                            parseInt(e.target.value) || 1
                          ),
                        }))
                      }
                      min="1"
                      title={`Quantity for ${product.name}`}
                    />
                    <button
                      className="quantity-btn"
                      onClick={() => handleQuantityChange(product.id, 1)}
                    >
                      +
                    </button>
                  </div>

                  <button
                    className="btn btn-primary add-to-order-btn"
                    onClick={() => handleAddToOrder(product)}
                    title={
                      draftOrder
                        ? `Add ${product.name} to your in-progress order`
                        : `Add ${product.name} to a new order`
                    }
                  >
                    {draftOrder ? (
                      <>🛒 Add to My Order</>
                    ) : (
                      <>➕ Add to Order</>
                    )}
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Modal de selección de orden - solo para usuarios */}
      {isUser && (
        <OrderSelectionModal
          isOpen={showOrderModal}
          onClose={() => setShowOrderModal(false)}
          product={selectedProduct!}
          quantity={selectedQuantity}
          orders={orders}
          onCreateNewOrder={handleCreateNewOrder}
          onSelectExistingOrder={handleSelectExistingOrder}
        />
      )}
    </>
  );
}
