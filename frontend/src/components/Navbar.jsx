import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isAuthenticated, isSeller } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/products" className="navbar-brand">
          <span className="brand-icon">⚡</span>
          <span className="brand-text">ProductHub</span>
        </Link>
        <div className="navbar-links">
          <Link to="/products" className="nav-link">Products</Link>
          {isAuthenticated() && (
            <Link to="/orders" className="nav-link">Orders</Link>
          )}
          {isSeller() && (
            <Link to="/products/new" className="nav-link">Seller Dashboard</Link>
          )}
          {isAuthenticated() ? (
            <div className="navbar-user">
              <span className="user-badge">{user.username}</span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
                Logout
              </button>
            </div>
          ) : (
            <div className="navbar-auth">
              <Link to="/login" className="btn btn-ghost btn-sm">Sign In</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Sign Up</Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
