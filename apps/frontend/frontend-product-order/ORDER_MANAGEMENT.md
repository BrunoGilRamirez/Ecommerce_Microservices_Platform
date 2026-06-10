# Sistema de Gestión de Órdenes - Frontend

## Descripción

Sistema completo de gestión de órdenes implementado en React TypeScript que permite a los usuarios crear, gestionar y procesar órdenes de productos de manera intuitiva y eficiente.

## 🚀 Características Implementadas

### 1. **Gestión de Productos**

- ✅ Catálogo de productos con información detallada
- ✅ Selector de cantidad para cada producto
- ✅ Botón "Agregar a Orden" en cada producto

### 2. **Creación de Órdenes**

- ✅ Modal de selección al agregar productos
- ✅ Opción de crear nueva orden
- ✅ Opción de agregar a orden existente
- ✅ Sistema de órdenes borrador (en progreso)

### 3. **Gestión de Órdenes**

- ✅ Visualización de todas las órdenes del usuario
- ✅ Órdenes borrador (nuevas) vs órdenes confirmadas
- ✅ Edición de cantidades de productos
- ✅ Eliminación de productos de órdenes
- ✅ Actualización de órdenes existentes
- ✅ Eliminación de órdenes completas

### 4. **Interfaz de Usuario**

- ✅ Diseño responsivo y moderno
- ✅ Estados de carga y error
- ✅ Mensajes informativos al usuario
- ✅ Indicador de orden en progreso en Dashboard

### 5. **Integración con Backend**

- ✅ Conectado a los endpoints del microservicio de órdenes
- ✅ Autenticación JWT integrada
- ✅ Manejo de errores y validaciones

## 📋 Flujo de Usuario

### Paso 1: Explorar Productos

1. El usuario navega a **Products** desde el menú
2. Ve el catálogo de productos disponibles
3. Selecciona la cantidad deseada usando los controles `+/-` o input directo
4. Hace clic en **"Agregar a Orden"**

### Paso 2: Selección de Orden

Si es la primera vez o no hay orden borrador:

- Se abre un modal con opciones:
  - **"Crear nueva orden"**: Inicia una orden borrador
  - **"Agregar a orden existente"**: Selecciona de órdenes previas

Si ya hay una orden borrador:

- El producto se agrega automáticamente a la orden en progreso

### Paso 3: Gestión de Órdenes

1. El usuario navega a **Orders** desde el menú
2. Ve todas sus órdenes organizadas por estado:
   - **Orden en progreso** (borrador)
   - **Órdenes anteriores** (confirmadas)

### Paso 4: Opciones de Orden

En cada orden, el usuario puede:

- **Ajustar cantidades**: Usar botones `+/-` en cada producto
- **Eliminar productos**: Usar el botón 🗑️
- **Confirmar orden borrador**: Botón "Crear Orden"
- **Actualizar orden existente**: Botón "Actualizar"
- **Eliminar orden**: Botón "Eliminar" (con confirmación)

## 🛠️ Componentes Implementados

### Servicios

- **`orderService.ts`**: API calls para operaciones CRUD de órdenes
- **`useOrders.ts`**: Hook personalizado para gestión de estado

### Componentes UI

- **`OrdersPage.tsx`**: Página principal de gestión de órdenes
- **`OrderCard.tsx`**: Componente para mostrar orden individual
- **`OrderSelectionModal.tsx`**: Modal para seleccionar orden al agregar productos
- **`ProductsList.tsx`**: Lista de productos con funcionalidad de órdenes

### Archivos Actualizados

- **`Dashboard.tsx`**: Indicador de orden en progreso
- **`App.tsx`**: Rutas y navegación
- **CSS**: Estilos completos para todas las funcionalidades

## 🎯 Endpoints Utilizados

### GET `/orders/me`

- Obtiene todas las órdenes del usuario autenticado
- Respuesta: Lista de órdenes con items y totales

### POST `/orders/me`

- Crea una nueva orden
- Body ejemplo:

```json
{
  "user": { "email": "user@example.com" },
  "items": [
    { "product": { "id": 1 }, "quantity": 2 },
    { "product": { "id": 3 }, "quantity": 1 }
  ]
}
```

### PUT `/orders/me`

- Actualiza una orden existente
- Body ejemplo:

```json
{
  "id": 1,
  "user": { "email": "user@example.com" },
  "items": [
    { "product": { "id": 1 }, "quantity": 3 },
    { "product": { "id": 4 }, "quantity": 1 }
  ],
  "createdAt": "2025-06-26 12:40:36.232663"
}
```

### DELETE `/orders/me?id={orderId}`

- Elimina una orden por ID
- Respuesta: Boolean indicando éxito

## 🔐 Seguridad

- **Autenticación JWT**: Todas las operaciones requieren usuario autenticado
- **Autorización**: Solo el propietario puede gestionar sus órdenes
- **Validación**: El backend valida la existencia de usuarios y productos

## 📱 Estados de UI

### Estados de Orden

- **Nueva/Borrador**: Orden creada localmente, no enviada al backend
- **Creada**: Orden confirmada y guardada en el backend

### Estados de Componentes

- **Loading**: Durante operaciones asíncronas
- **Error**: Con mensajes específicos y opción de retry
- **Empty**: Cuando no hay órdenes o productos

### Feedback al Usuario

- **Alertas**: Confirmaciones de acciones exitosas
- **Modales**: Selección de opciones importantes
- **Botones de estado**: Indicadores de carga ("Creando...", "Actualizando...")

## 🎨 Estilos y UX

### Diseño Responsivo

- **Desktop**: Layout optimizado para pantallas grandes
- **Tablet**: Adaptación de grillas y espaciado
- **Mobile**: Stack layout y controles táctiles optimizados

### Colores y Estados

- **Borrador**: Color amarillo/warning para órdenes en progreso
- **Confirmada**: Color verde/success para órdenes guardadas
- **Error**: Color rojo para estados de error
- **Info**: Color azul para información general

### Transiciones

- **Hover effects**: En botones y tarjetas
- **Loading animations**: Spinners durante operaciones
- **Modal animations**: Fade in/out suave

## 🚦 Manejo de Errores

### Errores de Red

- Detección de fallos de conexión
- Mensajes informativos al usuario
- Botones de "Intentar de nuevo"

### Errores de Validación

- Mensajes específicos del backend
- Validación en frontend para UX inmediata
- Prevención de estados inconsistentes

### Errores de Autenticación

- Redirección automática para reautenticación
- Mantenimiento del estado de la aplicación

## 🔄 Sincronización de Estado

### Estado Local vs Backend

- **Órdenes borradores**: Solo en estado local hasta confirmación
- **Órdenes confirmadas**: Sincronizadas con backend
- **Actualizaciones**: Reflejan cambios inmediatamente en UI

### Optimistic Updates

- Cambios de cantidad se reflejan inmediatamente
- Rollback en caso de error en backend
- Loading states durante sincronización

## 📄 Tipos TypeScript

```typescript
interface Order {
  id?: number;
  user?: { email: string };
  items: OrderItem[];
  createdAt?: string;
  total?: number;
}

interface OrderItem {
  id?: number;
  product: Product;
  quantity: number;
}

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;
}
```

## 🎉 Resultado Final

El sistema de gestión de órdenes está completamente funcional y ofrece:

1. **Experiencia de usuario fluida** para agregar productos a órdenes
2. **Gestión completa** de órdenes desde creación hasta eliminación
3. **Interfaz intuitiva** con feedback visual constante
4. **Integración robusta** con el backend de microservicios
5. **Diseño responsivo** que funciona en todos los dispositivos
6. **Manejo de errores** comprensivo y user-friendly

El usuario puede ahora navegar por productos, crear órdenes, gestionarlas completamente, y tener una experiencia de compra moderna y eficiente.
