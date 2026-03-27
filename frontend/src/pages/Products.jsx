import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getProducts, deleteProduct } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(null);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const fetchProducts = async () => {
    try {
      const res = await getProducts();
      setProducts(res.data);
    } catch (err) {
      console.error('Failed to load products', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    setDeleting(id);
    try {
      await deleteProduct(id);
      setProducts(products.filter((p) => p.id !== id));
    } catch (err) {
      alert(err.response?.data?.message || 'Delete failed');
    } finally {
      setDeleting(null);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(price);
  };

  if (loading) {
    return (
      <div className="page-loader">
        <div className="spinner-lg" />
        <p>Loading products...</p>
      </div>
    );
  }

  return (
    <div className="products-page">
      <div className="products-header">
        <div>
          <h1>Products</h1>
          <p className="subtitle">{products.length} items in catalogue</p>
        </div>
        {isAuthenticated() && (
          <button className="btn btn-primary" onClick={() => navigate('/products/new')}>
            + New Product
          </button>
        )}
      </div>

      {products.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📦</div>
          <h2>No products yet</h2>
          <p>Create your first product to get started</p>
          {isAuthenticated() && (
            <button className="btn btn-primary" onClick={() => navigate('/products/new')}>
              Create Product
            </button>
          )}
        </div>
      ) : (
        <div className="products-grid">
          {products.map((product) => (
            <div key={product.id} className="product-card">
              <div className="product-card-image">
                {product.imageUrl ? (
                  <img src={product.imageUrl} alt={product.description} />
                ) : (
                  <div className="product-placeholder">
                    <span>📸</span>
                  </div>
                )}
              </div>
              <div className="product-card-body">
                <h3>{product.description}</h3>
                <p className="product-price">{formatPrice(product.price)}</p>
                <div className="product-card-actions">
                  <Link to={`/products/${product.id}`} className="btn btn-ghost btn-sm">
                    View
                  </Link>
                  {isAuthenticated() && (
                    <>
                      <Link to={`/products/${product.id}/edit`} className="btn btn-outline btn-sm">
                        Edit
                      </Link>
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => handleDelete(product.id)}
                        disabled={deleting === product.id}
                      >
                        {deleting === product.id ? '...' : 'Delete'}
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
