// useOrdersContext.ts: Hook para usar el contexto de órdenes
import { useContext } from "react";
import { OrdersContext } from "./ordersContext";
import type { OrdersContextType } from "./ordersContext";

export function useOrdersContext(): OrdersContextType {
  const context = useContext(OrdersContext);
  if (context === undefined) {
    throw new Error("useOrdersContext must be used within an OrdersProvider");
  }
  return context;
}
