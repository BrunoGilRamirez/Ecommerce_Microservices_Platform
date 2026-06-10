// CallbackPage.tsx: Component to handle OAuth2 callback
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { handleCallback } from "./Security/auth";

interface CallbackPageProps {
  onAuthUpdate: () => void;
}

export function CallbackPage({ onAuthUpdate }: CallbackPageProps) {
  const navigate = useNavigate();

  useEffect(() => {
    const processCallback = async () => {
      try {
        console.log("Processing OAuth2 callback...");
        const newAccessToken = await handleCallback();

        if (newAccessToken) {
          console.log("Token obtained successfully, updating state...");
          onAuthUpdate(); // Actualizar el estado después del login
          navigate("/dashboard", { replace: true }); // Redirigir al dashboard
        } else {
          console.error("Could not obtain token");
          navigate("/", { replace: true }); // Redirigir de vuelta al inicio
        }
      } catch (error) {
        console.error("Error processing callback:", error);
        navigate("/dashboard", { replace: true }); // Redirigir al dashboard (usuario ya autenticado)
      }
    };

    processCallback();
  }, [navigate, onAuthUpdate]);

  return (
    <div className="loading-container">
      <div className="loading large"></div>
      <p className="loading-text">Processing authentication...</p>
      <p className="callback-info">
        Please wait while we complete the login process.
      </p>
    </div>
  );
}
