import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getProducts, getCategories, deleteProduct, createOrder } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(null);
  const [ordering, setOrdering] = useState(null);
  
  const { isAuthenticated, isSeller, isAdmin } = useAuth();
  const navigate = useNavigate();

  const fetchData = async () => {
    setLoading(true);
    try {
      const [productsRes, categoriesRes] = await Promise.all([
        getProducts(selectedCategory, searchQuery),
        getCategories()
      ]);
      setProducts(productsRes.data);
      // categories might be loaded only once in a real app
      if (categories.length === 0) {
        setCategories(categoriesRes.data);
      }
    } catch (err) {
      console.error('Failed to load data', err);
    } finally {
      setLoading(false);
    }
  };

  // Debounce search effect or trigger on enter. We'll just trigger on button click or blur for simplicity.
  useEffect(() => {
    fetchData();
  }, [selectedCategory]);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchData();
  };

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

  const handleOrder = async (product) => {
    if (!isAuthenticated()) {
      navigate('/login');
      return;
    }
    
    setOrdering(product.id);
    try {
      // Place order for 1 item
      await createOrder({
        shippingAddress: '123 Fake Street, City', // Hardcoded for demo
        items: [{ productId: product.id, quantity: 1 }]
      });
      alert('Order placed successfully!');
      // Refresh to update stock
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Order failed');
    } finally {
      setOrdering(null);
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
        {isAdmin() || isSeller() ? (
          <button className="btn btn-primary" onClick={() => navigate('/products/new')}>
            + New Product
          </button>
        ) : null}
      </div>

      <div className="filters-section">
        <form onSubmit={handleSearch} className="search-bar">
          <input
            type="text"
            placeholder="Search products..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="form-control"
          />
          <button type="submit" className="btn btn-primary">Search</button>
        </form>
        
        <div className="category-tabs">
          <button
            className={`tab ${selectedCategory === '' ? 'active' : ''}`}
            onClick={() => setSelectedCategory('')}
          >
            All Categories
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              className={`tab ${selectedCategory === cat.id ? 'active' : ''}`}
              onClick={() => setSelectedCategory(cat.id)}
            >
              {cat.name}
            </button>
          ))}
        </div>
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
                <div className="product-meta">
                  <p className="product-price">{formatPrice(product.price)}</p>
                  <p className={`product-stock ${product.stock > 0 ? 'in-stock' : 'out-of-stock'}`}>
                    {product.stock > 0 ? `Stock: ${product.stock}` : 'Out of Stock'}
                  </p>
                </div>
                <div className="product-card-actions">
                  <Link to={`/products/${product.id}`} className="btn btn-ghost btn-sm">
                    View
                  </Link>
                  {isAuthenticated() && !isAdmin() && !isSeller() && (
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => handleOrder(product)}
                      disabled={ordering === product.id || product.stock <= 0}
                    >
                      {ordering === product.id ? 'Ordering...' : 'Order Now'}
                    </button>
                  )}
                  {(isAdmin() || isSeller()) && (
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
