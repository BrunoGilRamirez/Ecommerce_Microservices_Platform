# Gestión de Usuarios - Frontend

## Descripción

Este módulo implementa la gestión completa de perfiles de usuario en el frontend, integrándose con el servicio de usuarios del backend a través del gateway.

## Flujo de Usuario

### 1. Verificación de Perfil

- Al acceder a la aplicación, se verifica automáticamente si el usuario tiene un perfil creado
- Se utiliza el endpoint `/user-service/users/me` con el email del usuario autenticado
- Si el perfil no existe, el usuario debe crearlo antes de acceder a ciertas funcionalidades

### 2. Creación de Perfil

- Si el usuario no tiene perfil, se le redirige a la página de perfil
- El formulario se pre-completa con la información disponible del JWT (email y nombre)
- Se utiliza el endpoint `/user-service/users/me/create` para crear el perfil

### 3. Actualización de Perfil

- Los usuarios pueden actualizar su perfil en cualquier momento desde la navegación
- Se utiliza el endpoint `/user-service/users/me/update` para actualizar la información

### 4. Restricción de Acceso

- La página de órdenes (`/orders`) requiere que el usuario tenga un perfil completo
- Si no tiene perfil, se muestra un mensaje con un botón para crear/actualizar el perfil

## Componentes Implementados

### Servicios

#### `userService.ts`

- **`getCurrentUserInfo()`**: Obtiene información del usuario del JWT
- **`checkUserProfile(email)`**: Verifica si existe el perfil de usuario
- **`createUserProfile(userData)`**: Crea un nuevo perfil de usuario
- **`updateUserProfile(userData)`**: Actualiza un perfil existente

#### `useUser.ts`

Hook personalizado que maneja el estado del usuario:

- Carga automática de información del usuario al montar el componente
- Gestión del estado de carga y errores
- Funciones para refrescar el perfil

### Componentes UI

#### `UserProfile.tsx`

Formulario para crear/editar el perfil de usuario:

- Pre-completa campos con información disponible
- Validación de campos requeridos
- Mensajes de éxito y error
- Modo creación vs edición

#### `UserPage.tsx`

Página principal de gestión de usuario:

- Maneja estados de carga y error
- Integra el componente `UserProfile`
- Determina automáticamente si es creación o edición

### Actualizaciones de Componentes Existentes

#### `Navigation.tsx`

- Agregado botón "Perfil" en la barra de navegación
- Enlace directo a `/profile`

#### `OrdersPage.tsx`

- Verificación automática de perfil de usuario
- Mensaje de perfil requerido si no existe
- Botón para redirigir a la página de perfil

#### `App.tsx`

- Nueva ruta `/profile` que apunta a `UserPage`

## Endpoints Utilizados

### Información del Usuario (JWT)

```
GET /user-service/users/me/current
```

Retorna información del token JWT del usuario autenticado.

### Verificación de Perfil

```
GET /user-service/users/me?email={email}
```

Verifica si existe un perfil para el email especificado.

### Creación de Perfil

```
POST /user-service/users/me/create
Content-Type: application/json

{
  "firstName": "string",
  "lastName": "string",
  "email": "string"
}
```

### Actualización de Perfil

```
PUT /user-service/users/me/update
Content-Type: application/json

{
  "id": number,
  "firstName": "string",
  "lastName": "string",
  "email": "string"
}
```

## Estructura de Archivos

```
src/
├── components/
│   ├── Security/
│   │   ├── userService.ts       # Servicios de API para usuarios
│   │   └── useUser.ts           # Hook personalizado
│   ├── user/
│   │   ├── UserProfile.tsx      # Formulario de perfil
│   │   └── UserPage.tsx         # Página principal
│   ├── Navigation.tsx           # Navegación actualizada
│   └── orders/
│       └── OrdersPage.tsx       # Página de órdenes con verificación
└── styles/
    └── components/
        └── userprofile.css      # Estilos para componentes de usuario
```

## Características

### Seguridad

- Todas las peticiones utilizan `authenticatedFetch()` con JWT
- Manejo automático de tokens expirados
- Redirección automática para reautenticación

### UX/UI

- Estados de carga durante operaciones
- Mensajes de error y éxito claros
- Formularios pre-completados cuando es posible
- Diseño responsive

### Gestión de Estado

- Hook personalizado para centralizar la lógica del usuario
- Actualización automática del estado después de operaciones
- Manejo de errores integrado

## Flujo de Navegación

1. **Usuario autenticado accede a la app**
2. **Se verifica automáticamente si tiene perfil**
3. **Si no tiene perfil:**
   - Puede crear uno desde la navegación (botón "Perfil")
   - Al intentar acceder a órdenes, se le pide crear perfil
4. **Si tiene perfil:**
   - Acceso completo a todas las funcionalidades
   - Puede actualizar su perfil cuando desee

## Consideraciones Técnicas

- Los endpoints requieren rol `USER` en el backend
- El email del usuario es inmutable (viene del proveedor OAuth)
- Se mantiene sincronización entre JWT y perfil de usuario
- Manejo de errores 404 cuando el perfil no existe
