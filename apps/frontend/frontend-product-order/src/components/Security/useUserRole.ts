import { useState, useEffect } from "react";
import { getAccessToken } from "./auth";

export type UserRole = "USER" | "ADMIN" | null;

export interface UserRoleState {
  role: UserRole;
  isAdmin: boolean;
  isUser: boolean;
  isLoading: boolean;
}

/**
 * Hook personalizado para extraer y gestionar el rol del usuario desde el JWT
 * Reacciona a cambios en el token de acceso en tiempo real
 */
export function useUserRole(): UserRoleState {
  const [roleState, setRoleState] = useState<UserRoleState>({
    role: null,
    isAdmin: false,
    isUser: false,
    isLoading: true,
  });

  // Función para decodificar JWT y extraer roles
  const extractRoleFromToken = (token: string | null): UserRole => {
    if (!token) return null;

    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      const payload = JSON.parse(jsonPayload);

      // Buscar roles en diferentes claims del JWT
      let roles: string[] = [];

      // Verificar en 'authorities' claim
      if (payload.authorities && Array.isArray(payload.authorities)) {
        roles = payload.authorities;
      }
      // Verificar en 'roles' claim
      else if (payload.roles && Array.isArray(payload.roles)) {
        roles = payload.roles;
      }
      // Verificar en 'scope' claim
      else if (payload.scope && typeof payload.scope === "string") {
        roles = payload.scope
          .split(" ")
          .map((scope: string) => `ROLE_${scope.toUpperCase()}`);
      }

      // Determinar el rol principal (prioridad: ADMIN > USER)
      if (roles.some((role) => role === "ROLE_ADMIN" || role === "ADMIN")) {
        return "ADMIN";
      } else if (
        roles.some((role) => role === "ROLE_USER" || role === "USER")
      ) {
        return "USER";
      }

      return "USER"; // Rol por defecto
    } catch (error) {
      console.error("Error extracting role from token:", error);
      return null;
    }
  };

  // Efecto para monitorear cambios en el token y actualizar el rol
  useEffect(() => {
    const updateRole = () => {
      const token = getAccessToken();
      const role = extractRoleFromToken(token);

      setRoleState({
        role,
        isAdmin: role === "ADMIN",
        isUser: role === "USER",
        isLoading: false,
      });
    };

    // Actualizar rol inmediatamente
    updateRole();

    // Configurar intervalo para verificar cambios en el token cada segundo
    const interval = setInterval(updateRole, 1000);

    // Limpiar intervalo al desmontar
    return () => clearInterval(interval);
  }, []);

  return roleState;
}
