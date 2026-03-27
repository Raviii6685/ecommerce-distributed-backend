import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    if (token && username) {
      setUser({ token, username });
    }
    setLoading(false);
  }, []);

  const loginUser = (token, username) => {
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
    setUser({ token, username });
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setUser(null);
  };

  const isAuthenticated = () => !!user?.token;

  // Simple JWT decoder for the payload
  const getRoles = () => {
    if (!user?.token) return [];
    try {
      const payloadBase64 = user.token.split('.')[1];
      const decodedJson = atob(payloadBase64);
      const decoded = JSON.parse(decodedJson);
      // Depending on the Spring Security setup, it might be in an array or a comma-separated string
      const authorities = decoded.roles || decoded.authorities || [];
      return Array.isArray(authorities) ? authorities : [authorities];
    } catch (e) {
      console.error("Failed to decode token", e);
      return [];
    }
  };

  const isAdmin = () => getRoles().some(r => r === 'ADMIN' || r.authority === 'ADMIN');
  const isSeller = () => getRoles().some(r => r === 'SELLER' || r.authority === 'SELLER');

  return (
    <AuthContext.Provider value={{ user, loginUser, logout, isAuthenticated, isAdmin, isSeller, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
