// config.ts: Centralizes URLs and endpoint configurations
// Modify here to change service URLs throughout the app

// Development configuration with proxy
const isDevelopment = import.meta.env.DEV;

// If in development, use Vite proxy
export const GATEWAY = isDevelopment ? "/api" : "http://localhost:8080";
export const AUTH_BASE_URL = "http://localhost:8081";
export const PRODUCT_SERVICE = `${GATEWAY}/product-service`;
export const ORDER_SERVICE = `${GATEWAY}/order-service`;
export const USER_SERVICE = `${GATEWAY}/user-service`;

// Add more endpoints here as needed, following the pattern:
// export const OTRO_SERVICE = `${GATEWAY}/otro-service`;

export const CLIENT_CONFIG = {
  CLIENT_ID: "fp_frontend",
  REDIRECT_URI: "http://localhost:3000/callback",
  SCOPE: "openid profile api.read api.write",
};
