// OrderTestComponent.tsx: Componente de prueba para validar el flujo de órdenes
import { useState } from "react";
import { useOrdersStore } from "../Security/useOrdersStore";
import type { Product } from "../Security/orderService";

const TEST_PRODUCT: Product = {
  id: 1,
  name: "Producto de Prueba",
  description: "Este es un producto de prueba para validar el flujo de órdenes",
  price: 29.99,
  category: "Pruebas",
  imageUrl: "https://via.placeholder.com/200x150/007bff/ffffff?text=Prueba",
};

export function OrderTestComponent() {
  const [logs, setLogs] = useState<string[]>([]);
  const {
    draftOrder,
    createDraftOrder,
    addProductToOrder,
    confirmDraftOrder,
    orders,
    isLoading,
    error,
  } = useOrdersStore();

  const addLog = (message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prev) => [...prev, `[${timestamp}] ${message}`]);
    console.log(`[OrderTest] ${message}`);
  };

  const handleCreateDraftOrder = async () => {
    try {
      addLog("🚀 Creando nueva orden borrador...");
      await createDraftOrder();
      addLog("✅ Orden borrador creada exitosamente");
    } catch (error) {
      addLog(`❌ Error creando orden borrador: ${error}`);
    }
  };

  const handleAddProduct = () => {
    try {
      addLog(
        `➕ Agregando producto: ${TEST_PRODUCT.name} (ID: ${TEST_PRODUCT.id})`
      );
      addProductToOrder("draft", TEST_PRODUCT, 2);
      addLog("✅ Producto agregado a la orden borrador");
    } catch (error) {
      addLog(`❌ Error agregando producto: ${error}`);
    }
  };

  const handleConfirmOrder = async () => {
    try {
      addLog("📤 Confirmando orden borrador (enviando al backend)...");
      const confirmedOrder = await confirmDraftOrder();
      addLog(`✅ Orden confirmada exitosamente con ID: ${confirmedOrder.id}`);
    } catch (error) {
      addLog(`❌ Error confirmando orden: ${error}`);
    }
  };

  const clearLogs = () => {
    setLogs([]);
  };

  return (
    <div
      style={{
        padding: "20px",
        border: "2px solid #007bff",
        borderRadius: "8px",
        margin: "20px",
        backgroundColor: "#f8f9fa",
      }}
    >
      <h2>🧪 Componente de Prueba de Órdenes</h2>

      <div style={{ marginBottom: "20px" }}>
        <h3>Estado Actual:</h3>
        <div>
          <strong>Loading:</strong> {isLoading ? "Sí" : "No"}
        </div>
        <div>
          <strong>Error:</strong> {error || "Ninguno"}
        </div>
        <div>
          <strong>Órdenes totales:</strong> {orders.length}
        </div>
        <div>
          <strong>Orden borrador:</strong>{" "}
          {draftOrder
            ? `${
                draftOrder.items.length
              } items - Total: $${draftOrder.total?.toFixed(2)}`
            : "Ninguna"}
        </div>
      </div>

      <div style={{ marginBottom: "20px" }}>
        <h3>Acciones de Prueba:</h3>
        <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
          <button
            onClick={handleCreateDraftOrder}
            disabled={!!draftOrder}
            style={{
              padding: "10px 15px",
              backgroundColor: draftOrder ? "#6c757d" : "#28a745",
              color: "white",
              border: "none",
              borderRadius: "4px",
            }}
          >
            1. Crear Orden Borrador
          </button>

          <button
            onClick={handleAddProduct}
            disabled={!draftOrder}
            style={{
              padding: "10px 15px",
              backgroundColor: !draftOrder ? "#6c757d" : "#007bff",
              color: "white",
              border: "none",
              borderRadius: "4px",
            }}
          >
            2. Agregar Producto
          </button>

          <button
            onClick={handleConfirmOrder}
            disabled={!draftOrder || draftOrder.items.length === 0}
            style={{
              padding: "10px 15px",
              backgroundColor:
                !draftOrder || draftOrder.items.length === 0
                  ? "#6c757d"
                  : "#dc3545",
              color: "white",
              border: "none",
              borderRadius: "4px",
            }}
          >
            3. Confirmar Orden
          </button>
        </div>
      </div>

      <div style={{ marginBottom: "20px" }}>
        <h3>Producto de Prueba:</h3>
        <div
          style={{
            padding: "10px",
            border: "1px solid #dee2e6",
            borderRadius: "4px",
            backgroundColor: "white",
          }}
        >
          <div>
            <strong>ID:</strong> {TEST_PRODUCT.id}
          </div>
          <div>
            <strong>Nombre:</strong> {TEST_PRODUCT.name}
          </div>
          <div>
            <strong>Precio:</strong> ${TEST_PRODUCT.price}
          </div>
        </div>
      </div>

      <div>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <h3>Logs de Prueba:</h3>
          <button
            onClick={clearLogs}
            style={{
              padding: "5px 10px",
              backgroundColor: "#6c757d",
              color: "white",
              border: "none",
              borderRadius: "4px",
            }}
          >
            Limpiar Logs
          </button>
        </div>
        <div
          style={{
            height: "200px",
            overflowY: "auto",
            border: "1px solid #dee2e6",
            padding: "10px",
            backgroundColor: "#000",
            color: "#00ff00",
            fontFamily: "monospace",
            fontSize: "12px",
          }}
        >
          {logs.length === 0 ? (
            <div>
              No hay logs aún. Ejecuta algunas acciones para ver los resultados.
            </div>
          ) : (
            logs.map((log, index) => <div key={index}>{log}</div>)
          )}
        </div>
      </div>
    </div>
  );
}
