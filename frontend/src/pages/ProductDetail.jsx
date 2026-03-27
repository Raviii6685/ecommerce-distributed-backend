import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getProduct } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await getProduct(id);
        setProduct(res.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Product not found');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id]);

  if (loading) {
    return (
      <div className="page-loader">
        <div className="spinner-lg" />
        <p>Loading product...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-page">
        <h2>Error</h2>
        <p>{error}</p>
        <Link to="/products" className="btn btn-primary">Back to Products</Link>
      </div>
    );
  }

  const formatPrice = (price) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(price);

  return (
    <div className="product-detail-page">
      <Link to="/products" className="back-link">← Back to Products</Link>
      <div className="product-detail-card">
        <div className="product-detail-image">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.description} />
          ) : (
            <div className="product-placeholder-lg">
              <span>📸</span>
              <p>No image available</p>
            </div>
          )}
        </div>
        <div className="product-detail-info">
          <h1>{product.description}</h1>
          <p className="product-detail-price">{formatPrice(product.price)}</p>
          <div className="product-detail-meta">
            <span className="badge">ID: {product.id.substring(0, 8)}...</span>
          </div>
          {isAuthenticated() && (
            <div className="product-detail-actions">
              <button className="btn btn-primary" onClick={() => navigate(`/products/${id}/edit`)}>
                Edit Product
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
