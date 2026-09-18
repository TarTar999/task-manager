import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import { tokenStorage } from '../api/client';
import type { AuthResponse, User } from '../types';

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  handleAuthSuccess: (response: AuthResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  // état initial lu depuis localStorage : sans ça, un F5 renvoie sur /login
  const [user, setUser] = useState<User | null>(() =>
    tokenStorage.get() ? (tokenStorage.getUser() as User | null) : null,
  );

  const handleAuthSuccess = useCallback((response: AuthResponse) => {
    tokenStorage.set(response.token);
    tokenStorage.setUser(response.user);
    setUser(response.user);
  }, []);

  const logout = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: user !== null, handleAuthSuccess, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth doit être utilisé dans un AuthProvider');
  }
  return context;
}
