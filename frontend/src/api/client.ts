import type { ApiError } from '../types';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const TOKEN_KEY = 'tm_token';
const USER_KEY = 'tm_user';

// localStorage et pas un cookie httpOnly : l'API est stateless et sur un autre
// domaine, un cookie aurait demandé du CORS credentials + CSRF pour pas grand
// chose sur un projet de cette taille.
export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
  getUser: () => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },
  setUser: (user: unknown) => localStorage.setItem(USER_KEY, JSON.stringify(user)),
};

// Tous les appels passent par ici : token ajouté et erreurs API transformées en
// Error avec le message du back.
export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  const token = tokenStorage.get();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, { ...options, headers });
  } catch {
    // fetch ne rejette que sur erreur réseau, pas sur un 4xx/5xx
    throw new Error('Impossible de contacter le serveur. Vérifiez votre connexion.');
  }

  if (!response.ok) {
    let message = `Erreur ${response.status}`;
    try {
      const body = (await response.json()) as ApiError;
      if (body.message) message = body.message;
    } catch {
      // corps vide ou non JSON, on garde le message générique
    }
    throw new Error(message);
  }

  // DELETE renvoie 204, response.json() planterait
  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
