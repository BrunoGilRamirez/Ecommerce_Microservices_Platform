# Implementación de Refresh Tokens

Este proyecto incluye una implementación completa del flujo de refresh tokens para OAuth2 con PKCE.

## Características implementadas

### 1. Funciones principales en `auth.ts`

- **`refreshAccessToken()`**: Intercambia el refresh token por un nuevo access token
- **`authenticatedFetch()`**: Wrapper para hacer requests HTTP con refresh automático de tokens
- **`getRefreshToken()`**: Obtiene el refresh token del sessionStorage

### 2. Hook personalizado `useAuth.ts`

- Manejo centralizado del estado de autenticación
- Auto-refresh automático cada 5 minutos
- Estado reactivo para access token, refresh token e isAuthenticated

### 3. Flujo automático de refresh

Cuando un request retorna 401 (Unauthorized), el sistema:

1. Automáticamente intenta refrescar el access token usando el refresh token
2. Reintenta el request original con el nuevo token
3. Si el refresh falla, redirige al usuario al login

## Uso

### Usando el hook useAuth

```typescript
import { useAuth } from "./useAuth";

function MyComponent() {
  const {
    accessToken,
    refreshToken,
    isAuthenticated,
    isLoading,
    refreshTokenFunction,
    logout,
  } = useAuth();

  // El hook maneja automáticamente:
  // - Verificación inicial de tokens
  // - Auto-refresh cada 5 minutos
  // - Estado reactivo de autenticación
}
```

### Haciendo requests autenticados

```typescript
import { authenticatedFetch } from "./auth";

// Request que maneja automáticamente el refresh de tokens
const response = await authenticatedFetch("/api/protected-endpoint", {
  method: "GET",
});
```

### Refrescando tokens manualmente

```typescript
import { refreshAccessToken } from "./auth";

const newToken = await refreshAccessToken();
if (newToken) {
  console.log("Token refreshed successfully");
} else {
  console.log("Refresh failed, user needs to login again");
}
```

## Configuración

### Variables de entorno

```typescript
const AUTH_BASE_URL = "http://localhost:8081";
const CLIENT_ID = "fp_frontend";
const REDIRECT_URI = "http://localhost:3000/callback";
```

### Intervalos de auto-refresh

El auto-refresh está configurado para ejecutarse cada 5 minutos. Puedes modificar este valor en `useAuth.ts`:

```typescript
}, 5 * 60 * 1000); // 5 minutos en milisegundos
```

## Flujo completo de ejemplo (usando curl)

```bash
# 1. Obtener código de autorización
# GET http://localhost:8081/oauth2/authorize?response_type=code&client_id=fp_frontend&redirect_uri=http://localhost:3000/callback&scope=openid%20profile%20api.read

# 2. Intercambiar código por tokens
TOKENS=$(curl -s -X POST http://localhost:8081/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=<AUTHORIZATION_CODE>&redirect_uri=http://localhost:3000/callback&client_id=fp_frontend")

# 3. Extraer refresh token
REFRESH_TOKEN=$(echo $TOKENS | jq -r '.refresh_token')

# 4. Usar refresh token para obtener nuevo access token
NEW_TOKENS=$(curl -s -X POST http://localhost:8081/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&refresh_token=$REFRESH_TOKEN&client_id=fp_frontend")
```

## Manejo de errores

- Si el refresh token es inválido o ha expirado, el usuario es redirigido automáticamente al login
- Los errores de red son capturados y logeados
- El estado de error se propaga a la UI para mostrar mensajes apropiados

## Almacenamiento

Los tokens se almacenan en `sessionStorage`:

- `access_token`: Token de acceso actual
- `refresh_token`: Token para refrescar el access token
- `id_token`: Token de identidad (OpenID Connect)

> **Nota de seguridad**: En una aplicación de producción, considera usar almacenamiento más seguro como httpOnly cookies para los tokens.
