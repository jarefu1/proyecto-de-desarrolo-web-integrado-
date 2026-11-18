declare global {
  var MHM_API_BASE: string | undefined;
}

// Puede definirse antes de cargar la app para desplegar el mismo bundle en varios entornos.
export const API_BASE = globalThis.MHM_API_BASE ?? 'http://localhost:8080/api/v1';
