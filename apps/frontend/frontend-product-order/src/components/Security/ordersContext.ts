// ordersContext.ts: Definición del contexto de órdenes
import { createContext } from "react";
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

export const OrdersContext = createContext<OrdersContextType | undefined>(
  undefined
);
