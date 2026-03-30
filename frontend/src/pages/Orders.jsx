import { useState, useEffect } from 'react';
import { getMyOrders, cancelOrder } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (!isAuthenticated()) return;
    
    const fetchOrders = async () => {
      try {
        const res = await getMyOrders();
        setOrders(res.data.content);
      } catch (err) {
        console.error('Failed to load orders', err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchOrders();
  }, [isAuthenticated]);

  const handleCancelClick = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    
    try {
      await cancelOrder(id);
      setOrders(orders.map(o => o.id === id ? { ...o, status: 'CANCELLED' } : o));
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel order');
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'CONFIRMED': return 'status-confirmed';
      case 'SHIPPED': return 'status-shipped';
      case 'DELIVERED': return 'status-delivered';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(price);
  };

  if (!isAuthenticated()) {
    return <div className="page-loader"><p>Please log in to view orders</p></div>;
  }

  if (loading) {
    return <div className="page-loader"><div className="spinner-lg" /><p>Loading orders...</p></div>;
  }

  return (
    <div className="orders-page fade-in">
      <div className="orders-header">
        <h1>My Orders</h1>
        <p className="subtitle">View and manage your recent orders</p>
      </div>

      {orders.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🛒</div>
          <h2>No orders yet</h2>
          <p>Head to the products page to place your first order.</p>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <div key={order.id} className="order-card p-4 my-3 border rounded shadow-sm flex flex-col gap-2">
              <div className="order-header flex justify-between items-center border-b pb-2">
                <div>
                  <span className="font-bold text-lg">Order #{order.id.substring(0, 8)}</span>
                  <span className="text-sm text-gray-500 ml-2">
                    {new Date(order.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <span className={`status-badge ${getStatusClass(order.status)}`}>
                  {order.status}
                </span>
              </div>
              
              <div className="order-items mt-2">
                {order.items.map(item => (
                  <div key={item.id} className="flex justify-between items-center py-1">
                    <span>
                      {item.product ? item.product.description : 'Deleted Product'} (x{item.quantity})
                    </span>
                    <span className="text-gray-600">{formatPrice(item.priceAtOrder)}</span>
                  </div>
                ))}
              </div>
              
              <div className="order-footer flex justify-between items-center mt-3 pt-2 border-t">
                <span className="font-bold text-lg">Total: {formatPrice(order.totalAmount)}</span>
                
                {order.status === 'PENDING' && (
                  <button 
                    className="btn btn-outline btn-sm text-red-500 border-red-500 hover:bg-red-50"
                    onClick={() => handleCancelClick(order.id)}
                  >
                    Cancel Order
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
