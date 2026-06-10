// OrdersContext.tsx: Contexto de React para manejar el estado global de órdenes
import { createContext } from "react";
import type { ReactNode } from "react";
import { useOrders } from "./useOrders";
import type { OrdersState } from "./useOrders";
import type { Order, Product } from "./orderService";

export interface OrdersContextType extends OrdersState {
  // Acciones
  loadOrders: () => Promise<void>;
  createDraftOrder: () => Promise<Order>;
  addProductToOrder: (
    orderId: number | "draft",
    product: Product,
    quantity?: number
  ) => void;
  removeProductFromOrder: (
    orderId: number | "draft",
    productId: number
  ) => void;
  updateProductQuantity: (
    orderId: number | "draft",
    productId: number,
    newQuantity: number
  ) => void;
  confirmDraftOrder: () => Promise<Order>;
  updateExistingOrder: (orderId: number) => Promise<Order>;
  removeOrder: (orderId: number) => Promise<boolean>;
  cancelDraftOrder: () => void;
  clearError: () => void;
  retryLoadOrders: () => Promise<void>;
}

// Crear el contexto
export const OrdersContext = createContext<OrdersContextType | undefined>(
  undefined
);

// Proveedor del contexto
interface OrdersProviderProps {
  children: ReactNode;
}

export function OrdersProvider({ children }: OrdersProviderProps) {
  const ordersHook = useOrders();

  return (
    <OrdersContext.Provider value={ordersHook}>
      {children}
    </OrdersContext.Provider>
  );
}
