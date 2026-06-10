// auth.ts: funciones para el flujo OAuth2 con PKCE
import { generateCodeVerifier, generateCodeChallenge } from "../../pkce";
import { AUTH_BASE_URL, CLIENT_CONFIG } from "../../config";

const CLIENT_ID = CLIENT_CONFIG.CLIENT_ID;
const REDIRECT_URI = CLIENT_CONFIG.REDIRECT_URI;
const SCOPE = CLIENT_CONFIG.SCOPE;

// Función para decodificar JWT sin verificar la firma (solo para leer claims)
function decodeJWT(
  token: string
): { exp?: number; [key: string]: unknown } | null {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error("Error decoding JWT:", error);
    return null;
  }
}

// Función para verificar si un token está próximo a expirar (dentro de 2 minutos)
export function isTokenExpiringSoon(token: string | null): boolean {
  if (!token) return true;

  const decoded = decodeJWT(token);
  if (!decoded || !decoded.exp) return true;

  const expirationTime = decoded.exp * 1000; // Convert to milliseconds
  const currentTime = Date.now();
  const twoMinutesInMs = 2 * 60 * 1000;

  return expirationTime - currentTime <= twoMinutesInMs;
}

// Función para verificar si un token ha expirado
export function isTokenExpired(token: string | null): boolean {
  if (!token) return true;

  const decoded = decodeJWT(token);
  if (!decoded || !decoded.exp) return true;

  const expirationTime = decoded.exp * 1000; // Convert to milliseconds
  const currentTime = Date.now();

  return currentTime >= expirationTime;
}

export async function login() {
  const codeVerifier = generateCodeVerifier();
  const codeChallenge = await generateCodeChallenge(codeVerifier);
  sessionStorage.setItem("pkce_code_verifier", codeVerifier);
  const params = new URLSearchParams({
    response_type: "code",
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    scope: SCOPE,
    code_challenge: codeChallenge,
    code_challenge_method: "S256",
  });
  window.location.href = `${AUTH_BASE_URL}/oauth2/authorize?${params.toString()}`;
}

export async function handleCallback(): Promise<string | null> {
  const params = new URLSearchParams(window.location.search); //URLSearchParams get query parameters from the URL
  const code = params.get("code");
  if (!code) return null;

  return await exchangeCodeForToken(code);
}

export function getAccessToken(): string | null {
  return sessionStorage.getItem("access_token");
}

// Función para realizar reauthentication silenciosa (fallback con redirección)
export async function silentReauth(): Promise<string | null> {
  try {
    console.log("Iniciando reauthentication silenciosa...");

    // Si el token está expirado o próximo a expirar, redirigir para obtener uno nuevo
    const accessToken = getAccessToken();
    if (
      !accessToken ||
      isTokenExpired(accessToken) ||
      isTokenExpiringSoon(accessToken)
    ) {
      console.log(
        "Token expirado o próximo a expirar, redirigiendo para reauth..."
      );
      await redirectToReauth();
      return null; // La redirección maneja el flujo
    }

    return accessToken;
  } catch (error) {
    console.error("Error en silent reauth:", error);
    return null;
  }
}

// Función para redirigir al servidor de autorización para reauth
export async function redirectToReauth() {
  const codeVerifier = generateCodeVerifier();
  const codeChallenge = await generateCodeChallenge(codeVerifier);
  sessionStorage.setItem("pkce_code_verifier", codeVerifier);

  const params = new URLSearchParams({
    response_type: "code",
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    scope: SCOPE,
    code_challenge: codeChallenge,
    code_challenge_method: "S256",
    prompt: "none", // Importante: no mostrar UI de login si hay sesión activa
  });

  const authUrl = `${AUTH_BASE_URL}/oauth2/authorize?${params.toString()}`;
  window.location.href = authUrl;
}

// Función auxiliar para intercambiar código por token
async function exchangeCodeForToken(code: string): Promise<string | null> {
  const codeVerifier = sessionStorage.getItem("pkce_code_verifier");
  if (!codeVerifier) throw new Error("No PKCE code_verifier found");

  const body = new URLSearchParams({
    grant_type: "authorization_code",
    code,
    redirect_uri: REDIRECT_URI,
    client_id: CLIENT_ID,
    code_verifier: codeVerifier,
  });

  const resp = await fetch(`${AUTH_BASE_URL}/oauth2/token`, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body,
  });

  if (!resp.ok) throw new Error("Token exchange failed");

  const data = await resp.json();
  console.log("Token exchange response:", data);

  // Guardar tokens (sin refresh token)
  sessionStorage.setItem("access_token", data.access_token);
  sessionStorage.setItem("id_token", data.id_token);

  return data.access_token;
}

// Función para hacer requests autenticados con reauth automática
export async function authenticatedFetch(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  const accessToken = getAccessToken();

  // Si no hay token o está expirado, intentar reauth
  if (!accessToken || isTokenExpired(accessToken)) {
    console.log("Access token missing or expired, redirecting for reauth...");
    await redirectToReauth();
    throw new Error("Redirecting for authentication");
  }

  // Si el token está próximo a expirar, también redirigir
  if (isTokenExpiringSoon(accessToken)) {
    console.log("Access token expiring soon, redirecting for reauth...");
    await redirectToReauth();
    throw new Error("Redirecting for token renewal");
  }

  // Agregar Authorization header
  const headers = {
    ...options.headers,
    Authorization: `Bearer ${accessToken}`,
  };

  const response = await fetch(url, {
    ...options,
    headers,
  });

  // Si la respuesta es 401 (Unauthorized), redirigir para reauth
  if (response.status === 401) {
    console.log("Received 401, redirecting for reauth...");
    await redirectToReauth();
    throw new Error("Authentication failed, redirecting for reauth");
  }

  return response;
}

export function logout() {
  sessionStorage.clear();
  window.location.href = "/";
}
