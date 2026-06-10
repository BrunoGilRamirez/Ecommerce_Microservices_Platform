import { Link } from "react-router-dom";
import { useUser } from "../Security/useUser";
import { useOrdersStore } from "../Security/useOrdersStore";
import { OrderCard } from "./OrderCard";
import "../../styles/components/orders.css";

export function OrdersPage() {
  const {
    hasProfile,
    isLoading: userLoading,
    currentUserInfo,
    error: userError,
    retryLoadUserData,
  } = useUser();
  const {
    orders,
    draftOrder,
    isLoading: ordersLoading,
    error: ordersError,
    createDraftOrder,
    updateProductQuantity,
    removeProductFromOrder,
    confirmDraftOrder,
    removeOrder,
    cancelDraftOrder,
    retryLoadOrders,
  } = useOrdersStore();

  // Show loading if user or orders are being loaded
  if (userLoading) {
    return (
      <div className="container">
        <h2 className="auth-title">Orders</h2>
        <div className="card">
          <div className="card-body">
            <div className="loading-container">
              <div className="loading-spinner"></div>
              <p>Checking user profile...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // If there is a user error, show error message with retry option
  if (userError) {
    return (
      <div className="container">
        <h2 className="auth-title">Orders</h2>
        <div className="card">
          <div className="card-body">
            <div className="error-container">
              <h3>Connection Error</h3>
              <p className="error-message">{userError}</p>
              <div className="mt-3">
                <button onClick={retryLoadUserData} className="btn btn-primary">
                  Try Again
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // If the user does not have a profile, show message to create profile
  if (!hasProfile) {
    return (
      <div className="container">
        <h2 className="auth-title">Orders</h2>
        <div className="card">
          <div className="card-body">
            <div className="profile-required">
              <h3>User Profile Required</h3>
              <p>
                To access your orders, you need to complete your user profile
                first.
              </p>
              <p>
                <strong>Email:</strong> {currentUserInfo?.subject}
              </p>
              <div className="mt-3">
                <Link to="/profile" className="btn btn-primary">
                  Update Profile
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const handleCreateNewOrder = async () => {
    try {
      await createDraftOrder();
    } catch (error) {
      console.error("Error creating new order:", error);
      // Error creating new order
    }
  };

  const handleUpdateQuantity = (
    orderId: number | "draft",
    productId: number,
    newQuantity: number
  ) => {
    updateProductQuantity(orderId, productId, newQuantity);
  };

  const handleRemoveProduct = (
    orderId: number | "draft",
    productId: number
  ) => {
    removeProductFromOrder(orderId, productId);
  };

  const handleConfirmDraftOrder = async () => {
    try {
      await confirmDraftOrder();
    } catch (error) {
      // Error confirming draft order
      alert(
        "Error creating the order: " +
          (error instanceof Error ? error.message : "Unknown error")
      );
    }
  };

  const handleDeleteOrder = async (orderId: number) => {
    try {
      await removeOrder(orderId);
    } catch (error) {
      // Error deleting order
      alert(
        "Error deleting the order: " +
          (error instanceof Error ? error.message : "Unknown error")
      );
    }
  };

  // If user has profile, show orders page
  return (
    <div className="orders-container">
      <div className="orders-header">
        <h2 className="orders-title">My Orders</h2>
        <button
          className="btn btn-primary"
          onClick={handleCreateNewOrder}
          disabled={!!draftOrder}
        >
          {draftOrder ? "An order is already in progress" : "New Order"}
        </button>
      </div>

      {ordersLoading && (
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading orders...</p>
        </div>
      )}

      {ordersError && (
        <div className="error-container">
          <h3>Error loading orders</h3>
          <p className="error-message">{ordersError}</p>
          <button onClick={retryLoadOrders} className="btn btn-primary">
            Try Again
          </button>
        </div>
      )}

      {!ordersLoading && !ordersError && (
        <>
          {/* Draft order */}
          {draftOrder && (
            <div className="draft-order-section">
              <h3>Order in progress</h3>
              <OrderCard
                order={draftOrder}
                isDraft={true}
                onUpdateQuantity={(productId, newQuantity) =>
                  handleUpdateQuantity("draft", productId, newQuantity)
                }
                onRemoveProduct={(productId) =>
                  handleRemoveProduct("draft", productId)
                }
                onConfirmOrder={handleConfirmDraftOrder}
                onCancelDraft={cancelDraftOrder}
              />
            </div>
          )}

          {/* Existing orders */}
          <div className="existing-orders-section">
            {orders.length === 0 && !draftOrder ? (
              <div className="empty-orders">
                <h3>You have no orders yet</h3>
                <p>
                  Create your first order by adding products from the catalog.
                </p>
                <Link to="/products" className="btn btn-primary">
                  View Product Catalog
                </Link>
              </div>
            ) : (
              <>
                {orders.length > 0 && <h3>Previous Orders</h3>}
                {orders.map((order) => (
                  <OrderCard
                    key={order.id}
                    order={order}
                    isDraft={false}
                    onUpdateQuantity={(productId, newQuantity) =>
                      handleUpdateQuantity(order.id!, productId, newQuantity)
                    }
                    onRemoveProduct={(productId) =>
                      handleRemoveProduct(order.id!, productId)
                    }
                    onDeleteOrder={() => handleDeleteOrder(order.id!)}
                  />
                ))}
              </>
            )}
          </div>
        </>
      )}
    </div>
  );
}
