// OrderSelectionModal.tsx: Modal para seleccionar orden al agregar producto
import { useState } from "react";
import type { Order, Product } from "../Security/orderService";

interface OrderSelectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  product: Product;
  quantity: number;
  orders: Order[];
  onCreateNewOrder: () => void;
  onSelectExistingOrder: (orderId: number) => void;
}

export function OrderSelectionModal({
  isOpen,
  onClose,
  product,
  quantity,
  orders,
  onCreateNewOrder,
  onSelectExistingOrder,
}: OrderSelectionModalProps) {
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);

  if (!isOpen) return null;

  const handleCreateNew = () => {
    onCreateNewOrder();
    onClose();
  };

  const handleSelectExisting = () => {
    if (selectedOrderId) {
      onSelectExistingOrder(selectedOrderId);
      onClose();
    }
  };

  // Filtrar órdenes existentes (solo las que tienen ID)
  const existingOrders = orders.filter((order) => order.id);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal order-selection-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h3>Where to add this product? 🛒</h3>
          <button className="btn-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          <div className="product-summary">
            <h4>📦 Selected Product:</h4>
            <div className="selected-product">
              <span className="product-name">{product.name}</span>
              <span className="product-quantity">Quantity: {quantity}</span>
              <span className="product-price">
                ${(product.price * quantity).toFixed(2)}
              </span>
            </div>
          </div>

          <div className="order-options">
            <div className="option-section">
              <button
                className="btn btn-primary btn-lg option-button new-order-btn"
                onClick={handleCreateNew}
              >
                <div className="option-content">
                  <div className="option-icon">🆕</div>
                  <div className="option-text">
                    <strong>Create new order</strong>
                    <small>Start a new order with this product</small>
                  </div>
                </div>
              </button>
            </div>

            {existingOrders.length > 0 && (
              <div className="option-section">
                <div className="section-divider">
                  <span>Or</span>
                </div>
                <h4>📋 Add to existing order:</h4>
                <div className="existing-orders">
                  {existingOrders.map((order) => (
                    <label key={order.id} className="order-option">
                      <input
                        type="radio"
                        name="selectedOrder"
                        value={order.id}
                        onChange={(e) =>
                          setSelectedOrderId(Number(e.target.value))
                        }
                      />
                      <div className="order-info">
                        <div className="order-header">
                          <span className="order-number">
                            Order #{order.id}
                          </span>
                          <span className="order-date">
                            {order.createdAt
                              ? new Date(order.createdAt).toLocaleDateString()
                              : "New"}
                          </span>
                        </div>
                        <div className="order-summary">
                          <span className="items-count">
                            {order.items.length}{" "}
                            {order.items.length === 1 ? "product" : "products"}
                          </span>
                          <span className="order-total">
                            Total: ${order.total?.toFixed(2) || "0.00"}
                          </span>
                        </div>
                      </div>
                    </label>
                  ))}

                  <button
                    className="btn btn-secondary btn-lg"
                    onClick={handleSelectExisting}
                    disabled={!selectedOrderId}
                  >
                    Add to selected order
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn btn-outline" onClick={onClose}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
