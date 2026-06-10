// ordersStore.ts: Simple global store for orders
import type { Order, OrderItem, Product } from "./orderService";
import {
  getUserOrders,
  createOrder,
  updateOrder,
  deleteOrder,
  calculateOrderTotal,
} from "./orderService";
import { getCurrentUserInfo } from "./userService";

// Global state for orders
interface OrdersState {
  orders: Order[];
  draftOrder: Order | null;
  isLoading: boolean;
  error: string | null;
}

// Clave para localStorage
const DRAFT_ORDER_STORAGE_KEY = "draftOrder";

class OrdersStore {
  private state: OrdersState = {
    orders: [],
    draftOrder: null,
    isLoading: false, // Cambiar a false inicialmente
    error: null,
  };

  private listeners: Set<() => void> = new Set();
  private initialized = false;

  constructor() {
    // Do not auto-initialize
    // console.log("📦 OrdersStore created but not initialized yet");
  }

  // Manually initialize the store
  async initialize() {
    if (this.initialized) {
      // console.log("📦 OrdersStore already initialized, skipping");
      return;
    }

    // console.log("🚀 Initializing OrdersStore...");
    this.initialized = true;

    // Load draft order without async validation
    this.loadDraftOrderFromStorage();

    // Validate draft order and load orders
    await this.validateDraftOrder();
    await this.loadOrders();
  }

  // Check if initialized
  isInitialized(): boolean {
    return this.initialized;
  }

  // Store reset (useful for logout)
  reset() {
    // console.log("🔄 Resetting orders store...");
    this.initialized = false;
    this.state = {
      orders: [],
      draftOrder: null,
      isLoading: false,
      error: null,
    };
    this.notify();
  }

  // Subscribe to state changes
  subscribe(listener: () => void) {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  // Get current state
  getState(): OrdersState {
    return { ...this.state };
  }

  // Notify subscribers
  private notify() {
    this.listeners.forEach((listener) => listener());
  }

  // Update state
  private setState(newState: Partial<OrdersState>) {
    this.state = { ...this.state, ...newState };
    // console.log("🔄 Orders store state updated:", this.state);
    this.notify();
  }

  // Load draft order from localStorage (without async validation)
  private loadDraftOrderFromStorage() {
    try {
      const stored = localStorage.getItem(DRAFT_ORDER_STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);

        // Recalculate total just in case
        if (parsed.items) {
          parsed.total = calculateOrderTotal(parsed.items);
        }

        // console.log("📦 Draft order loaded from localStorage (pending validation):", parsed);
        this.setState({ draftOrder: parsed });
      }
    } catch {
      // Error loading draft order from localStorage
      localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
    }
  }

  // Validate draft order asynchronously
  async validateDraftOrder() {
    if (!this.state.draftOrder) return;

    try {
      const userInfo = await getCurrentUserInfo();
      if (this.state.draftOrder.user?.email !== userInfo.subject) {
        // console.log("❌ Draft order belongs to different user, clearing...");
        localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
        this.setState({ draftOrder: null });
      } else {
        // console.log("✅ Draft order validated for current user");
      }
    } catch {
      // Error validating user for draft order
      localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
      this.setState({ draftOrder: null });
    }
  }

  // Save draft order to localStorage
  private saveDraftOrderToStorage(draftOrder: Order | null) {
    try {
      if (draftOrder) {
        const serialized = JSON.stringify(draftOrder);
        localStorage.setItem(DRAFT_ORDER_STORAGE_KEY, serialized);
        // console.log("💾 Draft order saved to localStorage:", { user: draftOrder.user?.email, itemsCount: draftOrder.items.length, total: draftOrder.total });
      } else {
        localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
        // console.log("🗑️ Draft order removed from localStorage");
      }
    } catch {
      // Error saving draft order to localStorage
    }
  }

  // Load user orders
  async loadOrders() {
    try {
      this.setState({ isLoading: true, error: null });
      const orders = await getUserOrders();
      this.setState({ orders, isLoading: false });
    } catch (error) {
      // Error loading orders
      let errorMessage = "Error loading orders";
      if (error instanceof Error) {
        if (error.message === "Failed to fetch") {
          errorMessage =
            "Connection error with the server. Make sure the backend is running.";
        } else {
          errorMessage = error.message;
        }
      }
      this.setState({ isLoading: false, error: errorMessage });
    }
  }

  // Create a new draft order
  async createDraftOrder() {
    try {
      const userInfo = await getCurrentUserInfo();
      const newDraftOrder: Order = {
        user: { email: userInfo.subject },
        items: [],
      };
      this.setState({ draftOrder: newDraftOrder });
      this.saveDraftOrderToStorage(newDraftOrder);
      return newDraftOrder;
    } catch {
      // Error creating draft order
      throw new Error("Error creating draft order");
    }
  }

  // Add product to an order
  addProductToOrder(
    orderId: number | "draft",
    product: Product,
    quantity: number = 1
  ) {
    // console.log(`➕ Adding product to ${orderId}:`, { productId: product.id, productName: product.name, quantity });

    const newItem: OrderItem = { product, quantity };

    if (orderId === "draft") {
      if (!this.state.draftOrder) {
        // console.warn("⚠️ No draft order exists, cannot add product");
        return;
      }

      const existingItemIndex = this.state.draftOrder.items.findIndex(
        (item) => item.product.id === product.id
      );

      let updatedItems;
      if (existingItemIndex >= 0) {
        updatedItems = this.state.draftOrder.items.map((item, index) =>
          index === existingItemIndex
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
        // console.log("📝 Updated existing product quantity");
      } else {
        updatedItems = [...this.state.draftOrder.items, newItem];
        // console.log("✨ Added new product to draft order");
      }

      const updatedDraftOrder = {
        ...this.state.draftOrder,
        items: updatedItems,
        total: calculateOrderTotal(updatedItems),
      };

      this.setState({ draftOrder: updatedDraftOrder });
      this.saveDraftOrderToStorage(updatedDraftOrder);

      // console.log("📊 Draft order updated:", { itemsCount: updatedDraftOrder.items.length, total: updatedDraftOrder.total });
    } else {
      // Add to existing order
      const existingOrder = this.state.orders.find(
        (order) => order.id === orderId
      );

      if (!existingOrder) {
        // console.warn(`⚠️ Order with ID ${orderId} not found`);
        return;
      }

      // console.log(`📝 Adding product to existing order ${orderId}`);

      const existingItemIndex = existingOrder.items.findIndex(
        (item) => item.product.id === product.id
      );

      let updatedItems;
      if (existingItemIndex >= 0) {
        // Update quantity of existing product
        updatedItems = existingOrder.items.map((item, index) =>
          index === existingItemIndex
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
        // console.log("📝 Updated existing product quantity in order");
      } else {
        // Add new product to the order
        updatedItems = [...existingOrder.items, newItem];
        // console.log("✨ Added new product to existing order");
      }

      const updatedOrder = {
        ...existingOrder,
        items: updatedItems,
        total: calculateOrderTotal(updatedItems),
      };

      // Update local state immediately
      this.setState({
        orders: this.state.orders.map((order) =>
          order.id === orderId ? updatedOrder : order
        ),
      });

      // Try to sync with backend asynchronously
      this.syncOrderWithBackend(updatedOrder).catch(() => {
        // Error syncing order with backend
        // In case of error, revert local change
        this.setState({
          orders: this.state.orders.map((order) =>
            order.id === orderId ? existingOrder : order
          ),
        });
      });

      // console.log("📊 Existing order updated locally:", { orderId, itemsCount: updatedOrder.items.length, total: updatedOrder.total });
    }
  }

  // Confirm draft order
  async confirmDraftOrder(): Promise<Order> {
    if (!this.state.draftOrder || this.state.draftOrder.items.length === 0) {
      throw new Error("Order is empty");
    }

    // Detailed log of the order before sending
    // console.log("🚀 Confirming draft order:", {
    //   itemsCount: this.state.draftOrder.items.length,
    //   items: this.state.draftOrder.items.map((item) => ({
    //     productId: item.product.id,
    //     productName: item.product.name,
    //     quantity: item.quantity,
    //     hasValidProductId: item.product.id != null,
    //   })),
    //   total: this.state.draftOrder.total,
    // });

    // Check that all items have valid productId
    const invalidItems = this.state.draftOrder.items.filter(
      (item) => !item.product.id
    );
    if (invalidItems.length > 0) {
      // Found items with invalid productId
      throw new Error(`${invalidItems.length} products have invalid ID`);
    }

    const createdOrder = await createOrder(this.state.draftOrder);
    // Update local state
    this.setState({
      orders: [createdOrder, ...this.state.orders],
      draftOrder: null,
    });
    this.saveDraftOrderToStorage(null);
    // console.log("✅ Order confirmed successfully:", createdOrder.id);
    return createdOrder;
  }

  // Cancel draft order
  cancelDraftOrder() {
    this.setState({ draftOrder: null });
    this.saveDraftOrderToStorage(null);
  }

  // Remove product from an order
  removeProductFromOrder(orderId: number | "draft", productId: number) {
    if (orderId === "draft" && this.state.draftOrder) {
      const updatedItems = this.state.draftOrder.items.filter(
        (item) => item.product.id !== productId
      );

      const updatedDraftOrder = {
        ...this.state.draftOrder,
        items: updatedItems,
        total: calculateOrderTotal(updatedItems),
      };

      this.setState({ draftOrder: updatedDraftOrder });
      this.saveDraftOrderToStorage(updatedDraftOrder);
    } else if (typeof orderId === "number") {
      // Remove product from existing order
      const existingOrder = this.state.orders.find(
        (order) => order.id === orderId
      );

      if (!existingOrder) {
        // console.warn(`⚠️ Order with ID ${orderId} not found`);
        return;
      }

      // console.log(`🗑️ Removing product ${productId} from order ${orderId}`);

      const updatedItems = existingOrder.items.filter(
        (item) => item.product.id !== productId
      );

      const updatedOrder = {
        ...existingOrder,
        items: updatedItems,
        total: calculateOrderTotal(updatedItems),
      };

      // Update local state immediately
      this.setState({
        orders: this.state.orders.map((order) =>
          order.id === orderId ? updatedOrder : order
        ),
      });

      // Sync with backend asynchronously
      this.syncOrderWithBackend(updatedOrder).catch(() => {
        // Error syncing order removal with backend
        // In case of error, revert local change
        this.setState({
          orders: this.state.orders.map((order) =>
            order.id === orderId ? existingOrder : order
          ),
        });
      });
    }
  }

  // Update product quantity in an order
  updateProductQuantity(
    orderId: number | "draft",
    productId: number,
    newQuantity: number
  ) {
    if (newQuantity <= 0) {
      this.removeProductFromOrder(orderId, productId);
      return;
    }

    if (orderId === "draft" && this.state.draftOrder) {
      const updatedItems = this.state.draftOrder.items.map((item) =>
        item.product.id === productId
          ? { ...item, quantity: newQuantity }
          : item
      );

      const updatedDraftOrder = {
        ...this.state.draftOrder,
        items: updatedItems,
        total: calculateOrderTotal(updatedItems),
      };

      this.setState({ draftOrder: updatedDraftOrder });
      this.saveDraftOrderToStorage(updatedDraftOrder);
    } else if (typeof orderId === "number") {
      // Update quantity in existing order
      const existingOrder = this.state.orders.find(
        (order) => order.id === orderId
      );

      if (!existingOrder) {
        // console.warn(`⚠️ Order with ID ${orderId} not found`);
        return;
      }

      // console.log(`📝 Updating quantity for product ${productId} in order ${orderId} to ${newQuantity}`);

      const updatedItems = existingOrder.items.map((item) =>
        item.product.id === productId
          ? { ...item, quantity: newQuantity }
          : item
      );

      const updatedOrder = {
        ...existingOrder,
        items: updatedItems,
        total: calculateOrderTotal(updatedItems),
      };

      // Update local state immediately
      this.setState({
        orders: this.state.orders.map((order) =>
          order.id === orderId ? updatedOrder : order
        ),
      });

      // Sync with backend asynchronously
      this.syncOrderWithBackend(updatedOrder).catch(() => {
        // Error syncing quantity update with backend
        // In case of error, revert local change
        this.setState({
          orders: this.state.orders.map((order) =>
            order.id === orderId ? existingOrder : order
          ),
        });
      });
    }
  }

  // Update existing order in the backend
  async updateExistingOrder(orderId: number): Promise<Order> {
    const orderToUpdate = this.state.orders.find(
      (order) => order.id === orderId
    );
    if (!orderToUpdate) {
      throw new Error("Order not found");
    }
    const updatedOrder = await updateOrder(orderToUpdate);
    // Update local state
    this.setState({
      orders: this.state.orders.map((order) =>
        order.id === orderId ? updatedOrder : order
      ),
    });
    return updatedOrder;
  }

  // Delete order
  async removeOrder(orderId: number): Promise<boolean> {
    const success = await deleteOrder(orderId);
    if (success) {
      // Update local state
      this.setState({
        orders: this.state.orders.filter((order) => order.id !== orderId),
      });
    }
    return success;
  }

  // Clear errors
  clearError() {
    this.setState({ error: null });
  }

  // Sync order with backend (helper method)
  private async syncOrderWithBackend(order: Order): Promise<Order> {
    if (!order.id) {
      throw new Error("Order must have an ID to be synced");
    }
    // console.log(`🔄 Syncing order ${order.id} with backend...`);
    const syncedOrder = await updateOrder(order);
    // Update local state with backend response
    this.setState({
      orders: this.state.orders.map((existingOrder) =>
        existingOrder.id === order.id ? syncedOrder : existingOrder
      ),
    });
    // console.log(`✅ Order ${order.id} synced successfully with backend`);
    return syncedOrder;
  }
}

// Single store instance
export const ordersStore = new OrdersStore();
