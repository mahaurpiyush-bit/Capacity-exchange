import { useEffect, useState } from 'react';
import { Routes, Route, Link, useParams, useNavigate } from 'react-router-dom';
import api, { authHeaders } from './api';

function getStoredUser() {
  const raw = localStorage.getItem('user');
  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
}

function HomePage() {
  const user = getStoredUser();

  return (
    <div>
      <header className="hero">
        <nav className="topbar">
          <div className="brand">Surplus Capacity Exchange</div>
          <div className="nav-links">
            <Link to="/">Home</Link>
            <Link to="/resources">Browse</Link>
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/bookings">My bookings</Link>
            {user ? <button className="text-button" onClick={() => {
              localStorage.removeItem('token');
              localStorage.removeItem('user');
              window.location.href = '/';
            }}>Logout</button> : <Link to="/login">Login</Link>}
          </div>
        </nav>
        <div className="hero-content">
          <div>
            <p className="eyebrow">B2B capacity monetization</p>
            <h1>Turn idle operational capacity into booked revenue.</h1>
            <p>
              Match underutilized equipment, kitchens, and workspaces with businesses that need short-term capacity without overbooking.
            </p>
            <div className="cta-row">
              <Link className="primary-button" to="/resources">Explore resources</Link>
              <Link className="secondary-button" to="/login">List capacity</Link>
            </div>
          </div>
          <div className="stat-card">
            <h3>Platform snapshot</h3>
            <div className="stats-grid">
              <div><strong>18.4%</strong><span>avg utilization uplift</span></div>
              <div><strong>1,240</strong><span>booked hours</span></div>
              <div><strong>94%</strong><span>on-time fulfillment</span></div>
              <div><strong>3.4x</strong><span>capacity efficiency</span></div>
            </div>
          </div>
        </div>
      </header>

      <main className="page-shell">
        <section className="section-block">
          <h2>Why this works</h2>
          <div className="feature-grid">
            <div className="feature-card">
              <h3>Capacity-first logic</h3>
              <p>Real availability checks prevent overbooking and enforce time-based utilization.</p>
            </div>
            <div className="feature-card">
              <h3>Provider monetization</h3>
              <p>Turn idle windows into predictable revenue from unused operational assets.</p>
            </div>
            <div className="feature-card">
              <h3>Seeker efficiency</h3>
              <p>Discover resources by location, capacity, schedule, and budget in one workflow.</p>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

function ResourceListPage() {
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/resources')
      .then((res) => setResources(res.data))
      .catch(() => {
        setResources([]);
        setError('The API is unavailable. Start the backend on http://localhost:8080 and refresh this page.');
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="page-shell">
      <div className="section-header">
        <div>
          <p className="eyebrow">Resource discovery</p>
          <h2>Available capacity</h2>
        </div>
      </div>
      {loading ? <p>Loading resources...</p> : error ? <div className="empty-state">{error}</div> : (
        <div className="resource-grid">
          {resources.map((resource) => (
            <article className="resource-card" key={resource.id}>
              <div className="resource-badge">{resource.category}</div>
              <h3>{resource.name}</h3>
              <p>{resource.description}</p>
              <ul className="resource-meta">
                <li>{resource.location}</li>
                <li>{resource.totalCapacity} units</li>
                <li>₹{resource.pricePerHour}/hour</li>
              </ul>
              <Link to={`/resources/${resource.id}`}>View details</Link>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}

function ResourceDetailPage() {
  const { id } = useParams();
  const [resource, setResource] = useState(null);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ startTime: '2026-11-12T09:00', endTime: '2026-11-12T12:00', requestedCapacity: 2 });
  const [notice, setNotice] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    api.get(`/resources/${id}`)
      .then((res) => setResource(res.data))
      .catch(() => setResource(null))
      .finally(() => setLoading(false));
  }, [id]);

  const handleBook = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    try {
      const payload = {
        resourceId: Number(id),
        startTime: form.startTime,
        endTime: form.endTime,
        requestedCapacity: Number(form.requestedCapacity)
      };
      await api.post('/bookings', payload, { headers: authHeaders() });
      setNotice('Booking request created successfully.');
    } catch (error) {
      setNotice(error.response?.data?.error || 'Booking failed. Try logging in as seeker@test.com.');
    }
  };

  if (loading) return <main className="page-shell"><p>Loading resource...</p></main>;
  if (!resource) return <main className="page-shell"><p>Resource not found.</p></main>;

  return (
    <main className="page-shell detail-layout">
      <div className="detail-panel">
        <p className="eyebrow">{resource.category}</p>
        <h2>{resource.name}</h2>
        <p>{resource.description}</p>
        <div className="detail-metrics">
          <span>Location: {resource.location}</span>
          <span>Capacity: {resource.totalCapacity} units</span>
          <span>Rate: ₹{resource.pricePerHour}/hour</span>
        </div>
        <div className="booking-box">
          <h3>Request booking</h3>
          <div className="booking-form">
            <label>
              Start time
              <input type="datetime-local" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
            </label>
            <label>
              End time
              <input type="datetime-local" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
            </label>
            <label>
              Capacity
              <input type="number" min="1" value={form.requestedCapacity} onChange={(e) => setForm({ ...form, requestedCapacity: e.target.value })} />
            </label>
            <button className="primary-button" onClick={handleBook}>Request booking</button>
          </div>
          {notice && <p className="status-message">{notice}</p>}
        </div>
      </div>
    </main>
  );
}

function LoginPage() {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({
    fullName: '',
    email: 'seeker@test.com',
    password: 'secret123',
    companyName: '',
    phone: ''
  });
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (event) => {
    event.preventDefault();
    setMessage('');
    try {
      const endpoint = mode === 'login' ? '/auth/login' : '/auth/register';
      const payload = mode === 'login'
        ? { email: form.email, password: form.password }
        : form;
      const response = await api.post(endpoint, payload);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
      setMessage(mode === 'login' ? 'Login successful.' : 'Account created successfully.');
      navigate('/resources');
    } catch (error) {
      const response = error.response?.data;
      const validation = response && typeof response === 'object'
        ? Object.values(response).join(' ')
        : '';
      setMessage(validation || 'Unable to connect to the backend or authenticate this account.');
    }
  };

  return (
    <main className="page-shell auth-layout">
      <div className="auth-card">
        <div className="auth-tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Login</button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Create account</button>
        </div>
        <h2>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h2>
        <form onSubmit={handleLogin}>
          {mode === 'register' && <label>
            Full name
            <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
          </label>}
          <label>
            Email
            <input required type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </label>
          <label>
            Password
            <input required minLength="6" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </label>
          {mode === 'register' && <><label>
            Company name
            <input value={form.companyName} onChange={(e) => setForm({ ...form, companyName: e.target.value })} />
          </label>
          <label>
            Phone
            <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          </label></>}
          <button type="submit" className="primary-button">{mode === 'login' ? 'Login' : 'Create account'}</button>
        </form>
        {message && <p className="status-message">{message}</p>}
      </div>
    </main>
  );
}

function BookingsPage() {
  const [bookings, setBookings] = useState([]);
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;
    api.get('/bookings', { headers: authHeaders() })
      .then((res) => setBookings(res.data))
      .catch(() => setBookings([]));
  }, []);

  return (
    <main className="page-shell">
      <h2>My bookings</h2>
      <div className="booking-list">
        {bookings.length === 0 ? <p>No bookings yet.</p> : bookings.map((booking) => (
          <div key={booking.id} className="booking-item">
            <strong>{booking.resource?.name || 'Resource'}</strong>
            <span>{booking.status}</span>
            <span>{new Date(booking.startTime).toLocaleString()} - {new Date(booking.endTime).toLocaleString()}</span>
            <span>Capacity: {booking.requestedCapacity}</span>
          </div>
        ))}
      </div>
    </main>
  );
}

function DashboardPage() {
  const [user, setUser] = useState(() => getStoredUser());
  const [summary, setSummary] = useState({});
  const [bookings, setBookings] = useState([]);
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [resourceForm, setResourceForm] = useState({
    name: '',
    description: '',
    category: 'Equipment',
    pricePerHour: '',
    totalCapacity: 1,
    location: ''
  });

  useEffect(() => {
    const currentUser = getStoredUser();
    if (!currentUser) {
      setError('Please log in to view the dashboard.');
      setLoading(false);
      return;
    }

    setUser(currentUser);
    const roles = currentUser.roles || [];
    const isProvider = roles.includes('PROVIDER');
    const isAdmin = roles.includes('ADMIN');

    if (!isProvider && !isAdmin) {
      setError('This dashboard is available to providers and admins.');
      setLoading(false);
      return;
    }

    const requests = [];

    if (isProvider) {
      requests.push(api.get(`/providers/${currentUser.id}/summary`, { headers: authHeaders() })
        .then((res) => setSummary((prev) => ({ ...prev, ...res.data }))));
      requests.push(api.get('/resources/mine', { headers: authHeaders() })
        .then((res) => setResources(res.data)));
    }

    if (isAdmin) {
      requests.push(
        api.get('/admin/summary', { headers: authHeaders() })
          .then((res) => setSummary((prev) => ({ ...prev, ...res.data })))
      );
    }

    requests.push(
      api.get('/bookings', { headers: authHeaders() })
        .then((res) => setBookings(res.data))
        .catch(() => setBookings([]))
    );

    Promise.allSettled(requests).finally(() => setLoading(false));
  }, []);

  const updateBooking = async (bookingId, action) => {
    setActionMessage('');
    try {
      await api.post(`/bookings/${bookingId}/${action}`, {}, { headers: authHeaders() });
      setBookings((current) => current.map((booking) => (
        booking.id === bookingId
          ? { ...booking, status: action === 'approve' ? 'PENDING_PAYMENT' : 'REJECTED' }
          : booking
      )));
      setActionMessage(`Booking ${action === 'approve' ? 'approved' : 'rejected'} successfully.`);
    } catch (requestError) {
      setActionMessage(requestError.response?.data?.error || `Unable to ${action} this booking.`);
    }
  };

  const createResource = async (event) => {
    event.preventDefault();
    setActionMessage('');
    try {
      const response = await api.post('/resources', {
        ...resourceForm,
        pricePerHour: Number(resourceForm.pricePerHour),
        totalCapacity: Number(resourceForm.totalCapacity)
      }, { headers: authHeaders() });
      setResources((current) => [...current, response.data]);
      setResourceForm({
        name: '',
        description: '',
        category: 'Equipment',
        pricePerHour: '',
        totalCapacity: 1,
        location: ''
      });
      setActionMessage('Machine listed successfully.');
    } catch (requestError) {
      setActionMessage(requestError.response?.data?.error || 'Unable to list this machine.');
    }
  };

  const summaryCards = [
    { label: 'Total resources', value: summary.totalResources ?? 0 },
    { label: 'Active resources', value: summary.activeResources ?? 0 },
    { label: 'Upcoming bookings', value: summary.upcomingBookings ?? 0 },
    { label: 'Completed bookings', value: summary.completedBookings ?? 0 },
    { label: 'Confirmed bookings', value: summary.confirmedBookings ?? 0 },
    { label: 'Revenue', value: `₹${Number(summary.revenue ?? 0).toFixed(2)}` }
  ];

  return (
    <main className="page-shell">
      <div className="section-header">
        <div>
          <p className="eyebrow">Operations dashboard</p>
          <h2>{user?.fullName || 'Dashboard'}</h2>
        </div>
      </div>

      {loading ? (
        <p>Loading dashboard...</p>
      ) : error ? (
        <div className="empty-state">{error}</div>
      ) : (
        <>
          <div className="dashboard-grid">
            {summaryCards.map((card) => (
              <div className="dashboard-card" key={card.label}>
                <span>{card.label}</span>
                <strong>{card.value}</strong>
              </div>
            ))}
          </div>

          <div className="dashboard-section">
            <h3>Recent booking activity</h3>
            {bookings.length === 0 ? (
              <p className="empty-state">No related bookings yet.</p>
            ) : (
              <div className="booking-list">
                {bookings.map((booking) => (
                  <div key={booking.id} className="booking-item">
                    <strong>{booking.resource?.name || 'Resource'}</strong>
                    <span>Status: {booking.status}</span>
                    <span>{new Date(booking.startTime).toLocaleString()} - {new Date(booking.endTime).toLocaleString()}</span>
                    <span>Capacity: {booking.requestedCapacity} · Total: ₹{Number(booking.totalPrice ?? 0).toFixed(2)}</span>
                    {booking.provider?.id === user?.id && booking.status === 'REQUESTED' && (
                      <div className="booking-actions">
                        <button className="primary-button" onClick={() => updateBooking(booking.id, 'approve')}>Approve request</button>
                        <button className="secondary-button action-secondary" onClick={() => updateBooking(booking.id, 'reject')}>Reject</button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {user?.roles?.includes('PROVIDER') && (
            <div className="dashboard-section">
              <h3>List a machine or workspace</h3>
              <p className="muted-copy">Add unused capacity so seekers can request it for a specific time window.</p>
              <form className="resource-form" onSubmit={createResource}>
                <label>Machine or resource name<input required value={resourceForm.name} onChange={(event) => setResourceForm({ ...resourceForm, name: event.target.value })} /></label>
                <label>Category<input required value={resourceForm.category} onChange={(event) => setResourceForm({ ...resourceForm, category: event.target.value })} /></label>
                <label>Location<input required value={resourceForm.location} onChange={(event) => setResourceForm({ ...resourceForm, location: event.target.value })} /></label>
                <label>Price per hour (₹)<input required min="0.01" step="0.01" type="number" value={resourceForm.pricePerHour} onChange={(event) => setResourceForm({ ...resourceForm, pricePerHour: event.target.value })} /></label>
                <label>Available capacity<input required min="1" type="number" value={resourceForm.totalCapacity} onChange={(event) => setResourceForm({ ...resourceForm, totalCapacity: event.target.value })} /></label>
                <label className="wide-field">Description<textarea required rows="3" value={resourceForm.description} onChange={(event) => setResourceForm({ ...resourceForm, description: event.target.value })} /></label>
                <button type="submit" className="primary-button">Publish listing</button>
              </form>
              <h3>Your listings</h3>
              {resources.length === 0 ? <p className="empty-state">You have not listed any resources yet.</p> : (
                <div className="booking-list">
                  {resources.map((resource) => (
                    <div className="booking-item" key={resource.id}>
                      <strong>{resource.name}</strong>
                      <span>{resource.category} · {resource.location} · ₹{resource.pricePerHour}/hour</span>
                      <span>{resource.totalCapacity} units · {resource.active ? 'Active' : 'Inactive'}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
          {actionMessage && <p className="status-message">{actionMessage}</p>}
        </>
      )}
    </main>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/resources" element={<ResourceListPage />} />
      <Route path="/resources/:id" element={<ResourceDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/bookings" element={<BookingsPage />} />
      <Route path="/dashboard" element={<DashboardPage />} />
    </Routes>
  );
}
