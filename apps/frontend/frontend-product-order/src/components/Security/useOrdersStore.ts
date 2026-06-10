// useOrdersStore.ts: Hook para usar el store de órdenes
import { useState, useEffect } from "react";
import { ordersStore } from "./ordersStore";
import type { Product } from "./orderService";
import { useAuth } from "./useAuth";

export function useOrdersStore() {
  const [state, setState] = useState(() => ordersStore.getState());
  const { isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    // Solo inicializar si el usuario está autenticado
    const initializeStore = async () => {
      if (isAuthenticated && !isLoading && !ordersStore.isInitialized()) {
        console.log("🔧 Initializing orders store from hook...");
        await ordersStore.initialize();
      } else if (!isAuthenticated && ordersStore.isInitialized()) {
        // Reset del store si el usuario se desloguea
        console.log("🚪 User logged out, resetting orders store...");
        ordersStore.reset();
      }
    };

    initializeStore();

    // Suscribirse a cambios en el store
    const unsubscribe = ordersStore.subscribe(() => {
      setState(ordersStore.getState());
    });

    // Limpiar suscripción al desmontar
    return () => {
      unsubscribe();
    };
  }, [isAuthenticated, isLoading]); // Dependencias importantes

  return {
    ...state,
    // Acciones del store
    loadOrders: () => ordersStore.loadOrders(),
    createDraftOrder: () => ordersStore.createDraftOrder(),
    addProductToOrder: (
      orderId: number | "draft",
      product: Product,
      quantity?: number
    ) => ordersStore.addProductToOrder(orderId, product, quantity),
    removeProductFromOrder: (orderId: number | "draft", productId: number) =>
      ordersStore.removeProductFromOrder(orderId, productId),
    updateProductQuantity: (
      orderId: number | "draft",
      productId: number,
      newQuantity: number
    ) => ordersStore.updateProductQuantity(orderId, productId, newQuantity),
    confirmDraftOrder: () => ordersStore.confirmDraftOrder(),
    updateExistingOrder: (orderId: number) =>
      ordersStore.updateExistingOrder(orderId),
    removeOrder: (orderId: number) => ordersStore.removeOrder(orderId),
    cancelDraftOrder: () => ordersStore.cancelDraftOrder(),
    clearError: () => ordersStore.clearError(),
    retryLoadOrders: () => ordersStore.loadOrders(),
    // Nuevas utilidades
    isStoreInitialized: () => ordersStore.isInitialized(),
    resetStore: () => ordersStore.reset(),
  };
}
