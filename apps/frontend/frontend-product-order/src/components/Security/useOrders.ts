// useOrders.ts: Hook personalizado para manejar el estado de las órdenes
import { useState, useEffect } from "react";
import type { Order, OrderItem, Product } from "./orderService";
import {
  getUserOrders,
  createOrder,
  updateOrder,
  deleteOrder,
  calculateOrderTotal,
} from "./orderService";
import { getCurrentUserInfo } from "./userService";

export interface OrdersState {
  orders: Order[];
  draftOrder: Order | null;
  isLoading: boolean;
  error: string | null;
}

// Clave para localStorage
const DRAFT_ORDER_STORAGE_KEY = "draftOrder";

// Funciones para persistencia
const saveDraftOrderToStorage = (draftOrder: Order | null) => {
  try {
    if (draftOrder) {
      const serialized = JSON.stringify(draftOrder);
      localStorage.setItem(DRAFT_ORDER_STORAGE_KEY, serialized);
      console.log("💾 Draft order saved to localStorage:", {
        user: draftOrder.user?.email,
        itemsCount: draftOrder.items.length,
        total: draftOrder.total,
      });
    } else {
      localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
      console.log("🗑️ Draft order removed from localStorage");
    }
  } catch (error) {
    console.error("❌ Error saving draft order to localStorage:", error);
  }
};

const loadDraftOrderFromStorage = (): Order | null => {
  try {
    const stored = localStorage.getItem(DRAFT_ORDER_STORAGE_KEY);
    if (stored) {
      const parsed = JSON.parse(stored);
      // Recalcular el total por si acaso
      if (parsed.items) {
        parsed.total = calculateOrderTotal(parsed.items);
      }
      console.log("📦 Draft order loaded from localStorage:", parsed);
      return parsed;
    }
  } catch (error) {
    console.error("Error loading draft order from localStorage:", error);
    localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
  }
  return null;
};

// Nueva función para validar la orden borrador asíncronamente
const validateDraftOrderUser = async (
  draftOrder: Order | null
): Promise<Order | null> => {
  if (!draftOrder) return null;

  try {
    const userInfo = await getCurrentUserInfo();
    if (draftOrder.user?.email === userInfo.subject) {
      console.log("✅ Draft order validated for current user");
      return draftOrder;
    } else {
      console.log("❌ Draft order belongs to different user, clearing...");
      localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
      return null;
    }
  } catch (error) {
    console.error("Error validating user for draft order:", error);
    localStorage.removeItem(DRAFT_ORDER_STORAGE_KEY);
    return null;
  }
};

export function useOrders() {
  const [ordersState, setOrdersState] = useState<OrdersState>(() => {
    // Cargar orden borrador al inicializar
    const storedDraftOrder = loadDraftOrderFromStorage();
    console.log(
      "🚀 Initializing useOrders hook with draft order:",
      storedDraftOrder
    );

    return {
      orders: [],
      draftOrder: storedDraftOrder,
      isLoading: true,
      error: null,
    };
  });

  // Efecto para persistir la orden borrador cada vez que cambie
  useEffect(() => {
    console.log("💾 Persisting draft order:", ordersState.draftOrder);
    saveDraftOrderToStorage(ordersState.draftOrder);
  }, [ordersState.draftOrder]);

  // Cargar órdenes del usuario
  const loadOrders = async () => {
    try {
      setOrdersState((prev) => ({ ...prev, isLoading: true, error: null }));
      const orders = await getUserOrders();
      setOrdersState((prev) => ({
        ...prev,
        orders,
        isLoading: false,
      }));
    } catch (error) {
      console.error("Error loading orders:", error);
      let errorMessage = "Error al cargar las órdenes";
      if (error instanceof Error) {
        if (error.message === "Failed to fetch") {
          errorMessage =
            "Error de conexión con el servidor. Verifica que el backend esté funcionando.";
        } else {
          errorMessage = error.message;
        }
      }
      setOrdersState((prev) => ({
        ...prev,
        isLoading: false,
        error: errorMessage,
      }));
    }
  };

  // Crear una nueva orden borrador
  const createDraftOrder = async () => {
    try {
      const userInfo = await getCurrentUserInfo();
      const newDraftOrder: Order = {
        user: { email: userInfo.subject },
        items: [],
      };
      setOrdersState((prev) => ({
        ...prev,
        draftOrder: newDraftOrder,
      }));
      return newDraftOrder;
    } catch (error) {
      console.error("Error creating draft order:", error);
      throw error;
    }
  };

  // Agregar producto a una orden
  const addProductToOrder = (
    orderId: number | "draft",
    product: Product,
    quantity: number = 1
  ) => {
    console.log(`➕ Adding product to ${orderId}:`, {
      productId: product.id,
      productName: product.name,
      quantity,
    });

    const newItem: OrderItem = { product, quantity };

    if (orderId === "draft") {
      // Agregar a orden borrador
      setOrdersState((prev) => {
        if (!prev.draftOrder) {
          console.warn("⚠️ No draft order exists, cannot add product");
          return prev;
        }

        const existingItemIndex = prev.draftOrder.items.findIndex(
          (item) => item.product.id === product.id
        );

        let updatedItems;
        if (existingItemIndex >= 0) {
          // Si el producto ya existe, actualizar cantidad
          updatedItems = prev.draftOrder.items.map((item, index) =>
            index === existingItemIndex
              ? { ...item, quantity: item.quantity + quantity }
              : item
          );
          console.log("📝 Updated existing product quantity");
        } else {
          // Si es nuevo producto, agregarlo
          updatedItems = [...prev.draftOrder.items, newItem];
          console.log("✨ Added new product to draft order");
        }

        const updatedDraftOrder = {
          ...prev.draftOrder,
          items: updatedItems,
          total: calculateOrderTotal(updatedItems),
        };

        console.log("📊 Draft order updated:", {
          itemsCount: updatedDraftOrder.items.length,
          total: updatedDraftOrder.total,
        });

        return {
          ...prev,
          draftOrder: updatedDraftOrder,
        };
      });
    } else {
      // Agregar a orden existente
      setOrdersState((prev) => ({
        ...prev,
        orders: prev.orders.map((order) => {
          if (order.id === orderId) {
            const existingItemIndex = order.items.findIndex(
              (item) => item.product.id === product.id
            );

            let updatedItems;
            if (existingItemIndex >= 0) {
              updatedItems = order.items.map((item, index) =>
                index === existingItemIndex
                  ? { ...item, quantity: item.quantity + quantity }
                  : item
              );
            } else {
              updatedItems = [...order.items, newItem];
            }

            return {
              ...order,
              items: updatedItems,
              total: calculateOrderTotal(updatedItems),
            };
          }
          return order;
        }),
      }));
    }
  };

  // Remover producto de una orden
  const removeProductFromOrder = (
    orderId: number | "draft",
    productId: number
  ) => {
    if (orderId === "draft") {
      setOrdersState((prev) => {
        if (!prev.draftOrder) return prev;

        const updatedItems = prev.draftOrder.items.filter(
          (item) => item.product.id !== productId
        );

        return {
          ...prev,
          draftOrder: {
            ...prev.draftOrder,
            items: updatedItems,
            total: calculateOrderTotal(updatedItems),
          },
        };
      });
    } else {
      setOrdersState((prev) => ({
        ...prev,
        orders: prev.orders.map((order) => {
          if (order.id === orderId) {
            const updatedItems = order.items.filter(
              (item) => item.product.id !== productId
            );
            return {
              ...order,
              items: updatedItems,
              total: calculateOrderTotal(updatedItems),
            };
          }
          return order;
        }),
      }));
    }
  };

  // Actualizar cantidad de un producto en una orden
  const updateProductQuantity = (
    orderId: number | "draft",
    productId: number,
    newQuantity: number
  ) => {
    if (newQuantity <= 0) {
      removeProductFromOrder(orderId, productId);
      return;
    }

    if (orderId === "draft") {
      setOrdersState((prev) => {
        if (!prev.draftOrder) return prev;

        const updatedItems = prev.draftOrder.items.map((item) =>
          item.product.id === productId
            ? { ...item, quantity: newQuantity }
            : item
        );

        return {
          ...prev,
          draftOrder: {
            ...prev.draftOrder,
            items: updatedItems,
            total: calculateOrderTotal(updatedItems),
          },
        };
      });
    } else {
      setOrdersState((prev) => ({
        ...prev,
        orders: prev.orders.map((order) => {
          if (order.id === orderId) {
            const updatedItems = order.items.map((item) =>
              item.product.id === productId
                ? { ...item, quantity: newQuantity }
                : item
            );
            return {
              ...order,
              items: updatedItems,
              total: calculateOrderTotal(updatedItems),
            };
          }
          return order;
        }),
      }));
    }
  };

  // Confirmar orden borrador (enviar al backend)
  const confirmDraftOrder = async (): Promise<Order> => {
    if (!ordersState.draftOrder || ordersState.draftOrder.items.length === 0) {
      throw new Error("La orden está vacía");
    }

    try {
      const createdOrder = await createOrder(ordersState.draftOrder);

      // Actualizar estado local
      setOrdersState((prev) => ({
        ...prev,
        orders: [createdOrder, ...prev.orders],
        draftOrder: null,
      }));

      return createdOrder;
    } catch (error) {
      console.error("Error confirming draft order:", error);
      throw error;
    }
  };

  // Actualizar orden existente en el backend
  const updateExistingOrder = async (orderId: number): Promise<Order> => {
    const orderToUpdate = ordersState.orders.find(
      (order) => order.id === orderId
    );
    if (!orderToUpdate) {
      throw new Error("Orden no encontrada");
    }

    try {
      const updatedOrder = await updateOrder(orderToUpdate);

      // Actualizar estado local
      setOrdersState((prev) => ({
        ...prev,
        orders: prev.orders.map((order) =>
          order.id === orderId ? updatedOrder : order
        ),
      }));

      return updatedOrder;
    } catch (error) {
      console.error("Error updating order:", error);
      throw error;
    }
  };

  // Eliminar orden
  const removeOrder = async (orderId: number): Promise<boolean> => {
    try {
      const success = await deleteOrder(orderId);

      if (success) {
        // Actualizar estado local
        setOrdersState((prev) => ({
          ...prev,
          orders: prev.orders.filter((order) => order.id !== orderId),
        }));
      }

      return success;
    } catch (error) {
      console.error("Error removing order:", error);
      throw error;
    }
  };

  // Cancelar orden borrador
  const cancelDraftOrder = () => {
    setOrdersState((prev) => ({ ...prev, draftOrder: null }));
  };

  // Limpiar errores
  const clearError = () => {
    setOrdersState((prev) => ({ ...prev, error: null }));
  };

  // Recargar órdenes
  const retryLoadOrders = async () => {
    clearError();
    await loadOrders();
  };

  // Validar que la orden borrador pertenece al usuario actual
  const validateDraftOrder = async () => {
    if (ordersState.draftOrder) {
      const validatedOrder = await validateDraftOrderUser(
        ordersState.draftOrder
      );
      if (!validatedOrder) {
        // La orden no es válida para el usuario actual
        setOrdersState((prev) => ({ ...prev, draftOrder: null }));
      }
    }
  };

  useEffect(() => {
    const initializeOrders = async () => {
      await validateDraftOrder();
      await loadOrders();
    };
    initializeOrders();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return {
    ...ordersState,
    // Acciones
    loadOrders,
    createDraftOrder,
    addProductToOrder,
    removeProductFromOrder,
    updateProductQuantity,
    confirmDraftOrder,
    updateExistingOrder,
    removeOrder,
    cancelDraftOrder,
    clearError,
    retryLoadOrders,
  };
}
