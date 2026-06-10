import { useState, useEffect, useCallback } from "react";
import {
  getAccessToken,
  redirectToReauth,
  isTokenExpiringSoon,
  logout as authLogout,
} from "./auth";

export interface AuthState {
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export function useAuth() {
  const [authState, setAuthState] = useState<AuthState>({
    accessToken: null,
    isAuthenticated: false,
    isLoading: true,
  });

  // Función para actualizar el estado de autenticación
  const updateAuthState = useCallback(() => {
    const accessToken = getAccessToken();

    setAuthState({
      accessToken,
      isAuthenticated: !!accessToken,
      isLoading: false,
    });
  }, []);

  // Función para realizar redirect reauth
  const redirectReauthFunction = useCallback(async (): Promise<void> => {
    try {
      await redirectToReauth();
    } catch (error) {
      console.error("Error in redirect reauth:", error);
    }
  }, []);

  // Función para logout
  const logout = useCallback(() => {
    authLogout();
    setAuthState({
      accessToken: null,
      isAuthenticated: false,
      isLoading: false,
    });
  }, []);

  // Auto-reauth cuando el token está próximo a expirar
  useEffect(() => {
    if (authState.accessToken) {
      // Verificar cada minuto si el token está próximo a expirar
      const interval = setInterval(async () => {
        if (isTokenExpiringSoon(authState.accessToken)) {
          console.log("Access token expiring soon, redirecting for reauth...");
          await redirectReauthFunction();
        }
      }, 60 * 1000); // Verificar cada minuto

      return () => clearInterval(interval);
    }
  }, [authState.accessToken, redirectReauthFunction]);

  // Inicializar el estado de autenticación
  useEffect(() => {
    updateAuthState();
  }, [updateAuthState]);

  return {
    ...authState,
    redirectReauthFunction,
    logout,
    updateAuthState,
  };
}
