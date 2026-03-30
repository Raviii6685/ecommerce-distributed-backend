import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getProduct, createProduct, updateProduct, getCategories } from '../services/api';

export default function ProductForm() {
  const { id } = useParams();
  const isEdit = !!id;
  const [form, setForm] = useState({ description: '', price: '', imageUrl: '', categoryId: '', stock: '' });
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const initForm = async () => {
      try {
        const catsRes = await getCategories();
 setCategories(catsRes.data.content || catsRes.data || []);        
        if (isEdit) {
          const res = await getProduct(id);
          setForm({
            description: res.data.description || '',
            price: res.data.price?.toString() || '',
            imageUrl: res.data.imageUrl || '',
            categoryId: res.data.categoryId || '',
            stock: res.data.stock?.toString() || '',
          });
        }
      } catch (err) {
        setError('Failed to load form data');
      } finally {
        setFetching(false);
      }
    };
    initForm();
  }, [id, isEdit]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});
    setLoading(true);

    const payload = {
      description: form.description,
      price: parseFloat(form.price),
      imageUrl: form.imageUrl || null,
      categoryId: form.categoryId || null,
      stock: parseInt(form.stock) || 0,
    };

    try {
      if (isEdit) {
        await updateProduct(id, payload);
        navigate(`/products/${id}`);
      } else {
        const res = await createProduct(payload);
        navigate(`/products/${res.data.id}`);
      }
    } catch (err) {
      const data = err.response?.data;
      if (data?.errors) {
        setFieldErrors(data.errors);
      } else {
        setError(data?.message || 'Save failed');
      }
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return (
      <div className="page-loader">
        <div className="spinner-lg" />
        <p>Loading product...</p>
      </div>
    );
  }

  return (
    <div className="form-page">
      <Link to="/products" className="back-link">← Back to Products</Link>
      <div className="form-card">
        <div className="form-header">
          <h1>{isEdit ? 'Edit Product' : 'New Product'}</h1>
          <p>{isEdit ? 'Update product details below' : 'Fill in the details to create a new product'}</p>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="product-description">Description</label>
            <input
              id="product-description"
              type="text"
              placeholder="Product name or description (min 5 chars)"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              required
              minLength={5}
              maxLength={255}
            />
            {fieldErrors.description && <span className="field-error">{fieldErrors.description}</span>}
          </div>
          <div className="form-group">
            <label htmlFor="product-price">Price (USD)</label>
            <input
              id="product-price"
              type="number"
              step="0.01"
              min="0"
              max="5000"
              placeholder="0.00"
              value={form.price}
              onChange={(e) => setForm({ ...form, price: e.target.value })}
              required
            />
            {fieldErrors.price && <span className="field-error">{fieldErrors.price}</span>}
          </div>
          <div className="form-group">
            <label htmlFor="product-category">Category</label>
            <select
              id="product-category"
              value={form.categoryId}
              onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
              required
            >
              <option value="" disabled>Select a Category</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
            {fieldErrors.categoryId && <span className="field-error">{fieldErrors.categoryId}</span>}
          </div>
          <div className="form-group">
            <label htmlFor="product-stock">Stock</label>
            <input
              id="product-stock"
              type="number"
              min="0"
              placeholder="0"
              value={form.stock}
              onChange={(e) => setForm({ ...form, stock: e.target.value })}
              required
            />
            {fieldErrors.stock && <span className="field-error">{fieldErrors.stock}</span>}
          </div>
          <div className="form-group">
            <label htmlFor="product-imageUrl">Image URL (optional)</label>
            <input
              id="product-imageUrl"
              type="url"
              placeholder="https://example.com/image.png"
              value={form.imageUrl}
              onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
            />
            {fieldErrors.imageUrl && <span className="field-error">{fieldErrors.imageUrl}</span>}
          </div>
          {form.imageUrl && (
            <div className="image-preview">
              <img src={form.imageUrl} alt="Preview" onError={(e) => (e.target.style.display = 'none')} />
            </div>
          )}
          <div className="form-actions">
            <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <span className="spinner" /> : isEdit ? 'Update Product' : 'Create Product'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
