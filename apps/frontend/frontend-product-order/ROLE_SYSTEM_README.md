# Sistema de Roles Dinámicos - Frontend

## Descripción

Se ha implementado un sistema de roles dinámicos que adapta la interfaz de usuario en tiempo real basándose en el rol del usuario extraído del JWT. El sistema distingue entre dos roles principales: **USER** y **ADMIN**.

## Características Implementadas

### 🔐 Autenticación Basada en Roles

- **Extracción automática de roles del JWT**: El hook `useUserRole` decodifica el token de acceso y extrae los roles de los claims `authorities`, `roles`, o `scope`.
- **Reactividad en tiempo real**: La interfaz se actualiza automáticamente cuando cambia el token o rol del usuario.
- **Manejo de errores**: Sistema robusto de manejo de errores en la decodificación del JWT.

### 👤 Rol USER (Usuario Normal)

**Funcionalidades disponibles:**

- ✅ Navegar por el catálogo de productos
- ✅ Crear y gestionar órdenes personales
- ✅ Gestionar perfil personal
- ✅ Ver historial de pedidos

**Navegación:**

- Dashboard
- Products (modo navegación)
- Orders
- Profile

### 🛠️ Rol ADMIN (Administrador)

**Funcionalidades disponibles:**

- ✅ Gestión completa de productos (CRUD)
- ✅ Crear nuevos productos (smartphones, clothes, electronics)
- ✅ Editar productos existentes
- ✅ Eliminar productos
- ✅ Ver catálogo completo en modo administración

**Restricciones:**

- ❌ No puede crear órdenes
- ❌ No tiene perfil personal
- ❌ Las secciones "Orders" y "Profile" están ocultas

**Navegación:**

- Dashboard (con panel administrativo)
- Products (modo administración con tabs)

## Componentes Nuevos

### 1. `useUserRole` Hook

```typescript
// Ubicación: src/components/Security/useUserRole.ts
export function useUserRole(): UserRoleState {
  // Extrae roles del JWT y proporciona estado reactivo
}
```

### 2. `AdminProductForm` Componente

```typescript
// Ubicación: src/components/products/AdminProductForm.tsx
// Formulario dinámico para crear productos con campos específicos por categoría
```

### 3. `AdminProductsManager` Componente

```typescript
// Ubicación: src/components/products/AdminProductsManager.tsx
// Tabla de gestión con funciones de edición y eliminación
```

### 4. `AccessDenied` Componente

```typescript
// Ubicación: src/components/AccessDenied.tsx
// Página de acceso denegado con mensajes específicos por rol
```

## Modificaciones en Componentes Existentes

### Navigation.tsx

- **Navegación condicional**: Los enlaces de "Orders" y "Profile" solo se muestran para usuarios con rol USER
- **Badge de administrador**: Indicador visual cuando el usuario es admin
- **Estado de carga**: Manejo del estado mientras se determina el rol

### App.tsx

- **Rutas protegidas**: Implementación de rutas condicionales basadas en roles
- **Redirecciones automáticas**: Los admins son redirigidos al dashboard si intentan acceder a rutas restringidas

### ProductsPage.tsx

- **Vista dual**: Modo usuario (navegación simple) vs modo admin (pestañas de gestión)
- **Tabs administrativos**: Browse, Create, Manage

### ProductsList.tsx

- **Modo administrativo**: Vista especial para admins sin funciones de pedido
- **Información adicional**: Muestra ID y categoría de productos para admins

### Dashboard.tsx

- **Paneles específicos por rol**: Contenido diferente según USER o ADMIN
- **Información de rol**: Muestra el rol actual del usuario
- **Enlaces de acción**: Accesos directos a funcionalidades principales

## Integración con Backend

### Endpoints utilizados:

- `GET /products/smartphones` - Listar smartphones
- `POST /products/smartphones` - Crear smartphone
- `PUT /products/smartphones/{id}` - Actualizar smartphone
- `DELETE /products/smartphones/{id}` - Eliminar smartphone
- `GET /products/clothes` - Listar ropa
- `POST /products/clothes` - Crear ropa
- `PUT /products/clothes/{id}` - Actualizar ropa
- `DELETE /products/clothes/{id}` - Eliminar ropa

### Validación de roles:

Los endpoints administrativos están protegidos con `@PreAuthorize("hasRole('ADMIN')")` en el backend, proporcionando una doble capa de seguridad.

## Estilos CSS

### Archivos de estilos añadidos:

- `src/styles/components/admin-forms.css` - Estilos para formularios y gestión administrativa
- `src/styles/components/dashboard.css` - Estilos para dashboard y componentes de rol

### Características de diseño:

- **Responsive**: Adapta a diferentes tamaños de pantalla
- **Indicadores visuales**: Badges y colores para distinguir roles
- **UX intuitiva**: Mensajes claros y acciones bien definidas

## Configuración y Uso

### 1. JWT Requirements

El JWT debe contener uno de estos claims con información de roles:

```json
{
  "authorities": ["ROLE_ADMIN"],
  // o
  "roles": ["ADMIN"],
  // o
  "scope": "admin user"
}
```

### 2. Desarrollo Local

```bash
cd fp_frontend/frontend-product-order
npm run dev
```

### 3. Variables de Entorno

Asegúrate de que el frontend esté configurado para conectarse al gateway en el archivo `config.ts`.

## Flujo de Trabajo del Usuario

### Usuario Normal (USER):

1. Login → Dashboard con opciones de usuario
2. Products → Navegar y añadir a carrito
3. Orders → Gestionar pedidos
4. Profile → Actualizar información personal

### Administrador (ADMIN):

1. Login → Dashboard con panel administrativo
2. Products →
   - Tab "Browse": Ver todos los productos
   - Tab "Create": Formulario de creación
   - Tab "Manage": Tabla de gestión con edición/eliminación
3. No acceso a Orders ni Profile

## Seguridad

- **Validación frontend**: Verificación de roles antes de renderizar componentes
- **Protección de rutas**: Redirecciones automáticas para accesos no autorizados
- **Validación backend**: Endpoints protegidos con Spring Security
- **Tokens seguros**: Manejo seguro de JWT con validación de expiración

## Testing

Para probar la funcionalidad:

1. **Como Usuario**: Usa credenciales que generen un JWT con rol USER
2. **Como Admin**: Usa credenciales que generen un JWT con rol ADMIN
3. **Verificar restricciones**: Intenta acceder a rutas restringidas y confirma las redirecciones

## Próximas Mejoras

- [ ] Cache de productos para admins
- [ ] Bulk operations (eliminar múltiples productos)
- [ ] Historial de cambios administrativos
- [ ] Roles más granulares (MODERATOR, SUPER_ADMIN)
- [ ] Dashboard con métricas y estadísticas
