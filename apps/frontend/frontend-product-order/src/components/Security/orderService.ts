/* eslint-disable no-useless-catch */
// orderService.ts: Service to handle order operations
import { authenticatedFetch } from "./auth";
import { ORDER_SERVICE } from "../../config";

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  category?: string;
  imageUrl?: string;
}

export interface OrderItem {
  id?: number;
  product: Product;
  quantity: number;
  subtotal?: number;
}

export interface Order {
  id?: number;
  user?: {
    email: string;
  };
  items: OrderItem[];
  createdAt?: string;
  total?: number;
}

// Interfaces for server response structures
interface ServerOrderItem {
  id: number;
  productId: number;
  productName: string;
  productPrice: number;
  productCategory: string;
  productImageUrl: string;
  quantity: number;
  subtotal: number;
}

interface ServerOrder {
  id: number;
  userEmail: string;
  items: ServerOrderItem[];
  createdAt: string;
  total: number;
}

export interface AppResponse<T> {
  message: string;
  data: T;
}

// Function to transform server data to frontend format
function transformServerOrderToClientOrder(serverOrder: ServerOrder): Order {
  return {
    id: serverOrder.id,
    user: { email: serverOrder.userEmail },
    items: serverOrder.items.map(
      (serverItem): OrderItem => ({
        id: serverItem.id,
        product: {
          id: serverItem.productId,
          name: serverItem.productName || "Producto sin nombre",
          description: "", // No viene del servidor en este endpoint
          price: serverItem.productPrice || 0,
          category: serverItem.productCategory || "Uncategorized",
          // Check if image URL is valid
          imageUrl:
            serverItem.productImageUrl &&
            serverItem.productImageUrl.startsWith("http")
              ? serverItem.productImageUrl
              : undefined,
        },
        quantity: serverItem.quantity || 1,
        subtotal: serverItem.subtotal || 0,
      })
    ),
    createdAt: serverOrder.createdAt,
    total: serverOrder.total || 0,
  };
}

// Function to get orders for the authenticated user
export async function getUserOrders(): Promise<Order[]> {
  try {
    const response = await authenticatedFetch(`${ORDER_SERVICE}/orders/me`);

    if (!response.ok) {
      throw new Error(`Error ${response.status}: ${response.statusText}`);
    }

    const result: AppResponse<ServerOrder[]> = await response.json();
    // console.log("Raw Server Orders:", result.data);

    // Transformar los datos del servidor al formato del frontend
    const transformedOrders = result.data.map(
      transformServerOrderToClientOrder
    );
    // console.log("Transformed Orders:", transformedOrders);

    return transformedOrders;
  } catch (error) {
    // Error fetching user orders
    throw error;
  }
}

// Interfaces for sending data to backend (Order entity structure)
interface BackendOrderItem {
  product: {
    id: number;
  };
  quantity: number;
}

interface BackendOrder {
  id?: number;
  user?: {
    email: string;
  };
  items: BackendOrderItem[];
  createdAt?: string;
}

// Function to transform frontend order to backend Order entity format
function transformOrderToBackendOrder(order: Order): BackendOrder {
  return {
    id: order.id,
    user: order.user,
    items: order.items
      .filter((item) => item.product.id != null) // Filtrar items con productId null
      .map((item) => ({
        product: {
          id: item.product.id,
        },
        quantity: item.quantity,
      })),
    createdAt: order.createdAt,
  };
}

// Function to create a new order
export async function createOrder(order: Order): Promise<Order> {
  try {
    // Validate that the order has valid items
    const validItems = order.items.filter((item) => item.product.id != null);
    if (validItems.length === 0) {
      throw new Error("Order must contain at least one valid product");
    }

    // Transformar al formato de entidad Order del backend
    const payload = transformOrderToBackendOrder(order);
    // console.log("📤 Sending order creation payload:", payload);

    const response = await authenticatedFetch(`${ORDER_SERVICE}/orders/me`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(
        errorData.message || `Error ${response.status}: ${response.statusText}`
      );
    }

    const result: AppResponse<ServerOrder> = await response.json();
    // Transformar la respuesta del servidor al formato del frontend
    return transformServerOrderToClientOrder(result.data);
  } catch (error) {
    // Error creating order
    throw error;
  }
}

// Function to update an existing order
export async function updateOrder(order: Order): Promise<Order> {
  try {
    // Validate that the order has an ID
    if (!order.id) {
      throw new Error("Order must have an ID to be updated");
    }

    // Validate that the order has valid items
    const validItems = order.items.filter((item) => item.product.id != null);
    if (validItems.length === 0) {
      throw new Error("Order must contain at least one valid product");
    }

    // console.log("Updating order:", order);
    // Transform to backend Order entity format
    const payload = transformOrderToBackendOrder(order);
    // console.log("📤 Sending order update payload:", payload);
    // console.log("📤 Updating order with ID:", order.id);

    const response = await authenticatedFetch(`${ORDER_SERVICE}/orders/me`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(
        errorData.message || `Error ${response.status}: ${response.statusText}`
      );
    }

    const result: AppResponse<ServerOrder> = await response.json();
    // Transform server response to frontend format
    return transformServerOrderToClientOrder(result.data);
  } catch (error) {
    // Error updating order
    throw error;
  }
}

// Function to delete an order
export async function deleteOrder(orderId: number): Promise<boolean> {
  try {
    const response = await authenticatedFetch(
      `${ORDER_SERVICE}/orders/me?id=${orderId}`,
      {
        method: "DELETE",
      }
    );

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(
        errorData.message || `Error ${response.status}: ${response.statusText}`
      );
    }

    const result: AppResponse<boolean> = await response.json();
    return result.data;
  } catch (error) {
    // Error deleting order
    throw error;
  }
}

// Helper function to calculate the total of an order
export function calculateOrderTotal(items: OrderItem[]): number {
  return items.reduce(
    (total, item) => total + item.product.price * item.quantity,
    0
  );
}
