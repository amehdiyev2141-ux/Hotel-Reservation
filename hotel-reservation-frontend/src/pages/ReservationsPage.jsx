import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import Icon from '../components/Icons.jsx';
import { EmptyState, LoadingState } from '../components/States.jsx';
import { cancelReservation, getApiError, getGuest, getReservation, getRoom } from '../services/api.js';
import { readReservationIds } from '../services/reservationsStore.js';
import { photoForRoom } from '../components/RoomCard.jsx';

export default function ReservationsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busyId, setBusyId] = useState(null);
  const location = useLocation();

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    const ids = readReservationIds();
    try {
      const results = await Promise.all(ids.map(async (id) => {
        try {
          const data = await getReservation(id);
          const [room, guest] = await Promise.all([
            getRoom(data.roomId).catch(() => null),
            getGuest(data.guestId).catch(() => null),
          ]);
          return { data, room, guest };
        } catch (err) {
          return { id, error: getApiError(err) };
        }
      }));
      setItems(results);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  async function handleCancel(id) {
    setBusyId(String(id));
    setError('');
    try {
      const data = await cancelReservation(id);
      setItems((current) => current.map((item) => String(item.data?.id) === String(id) ? { ...item, data } : item));
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div className="reservations-page page-wrap">
      <div className="page-intro reservations-intro"><span className="eyebrow">YOUR LITTLE LIST</span><h1>Stays to <em>look forward to.</em></h1><p>Reservations made from this browser are saved here by their confirmation IDs.</p></div>
      <div className="privacy-note"><Icon name="check" size={17} /><p>This API has no accounts or per-guest reservation list. We only look up reservation IDs saved in this browser; this is not an account or authentication feature.</p></div>
      {error && <div className="inline-error" role="alert">{error}</div>}
      {loading ? <LoadingState label="Gathering your reservations" /> : items.length === 0 ? <EmptyState title="Nothing on the calendar yet" message="When you request a reservation here, its ID will appear in this browser." action={<Link className="button button-navy" to="/hotels">Find a room <Icon name="arrow" size={16} /></Link>} /> : (
        <div className="reservation-list">{items.map((item) => item.data ? (
          <article className="reservation-card" key={item.data.id}>
            <div className="reservation-image"><img src={photoForRoom(item.data.roomId)} alt="Representative hotel room interior" /></div>
            <div className="reservation-info">
              <div className="reservation-topline">
                <span className="eyebrow">RESERVATION #{item.data.id}</span>
                <span className={`status-pill ${item.data.status === 'CANCELLED' ? 'status-cancelled' : ''}`}>{item.data.status}</span>
              </div>
              <h2>{item.guest?.fullName || item.data.guestName || 'Guest'}</h2>
              <div className="reservation-meta">
                <span><Icon name="bed" /> {item.room?.type || 'Room type unavailable'} · Room {item.room?.number ?? item.data.roomNumber}</span>
                <span><Icon name="calendar" /> {item.data.checkIn} — {item.data.checkOut}</span>
                {item.guest?.email && <span>{item.guest.email}</span>}
                {item.guest?.phoneNumber && <span>{item.guest.phoneNumber}</span>}
              </div>
              <div className="reservation-bottom">
                <strong>${Number(item.data.totalPrice).toFixed(2)} <small>total</small></strong>
                {item.data.status === 'ACTIVE' && <button className="text-button cancel-button" disabled={busyId === String(item.data.id)} onClick={() => handleCancel(item.data.id)}>{busyId === String(item.data.id) ? 'Cancelling...' : 'Cancel reservation'}</button>}
              </div>
            </div>
          </article>
        ) : <div className="reservation-missing" key={item.id}><span>Reservation #{item.id} could not be loaded.</span><small>{item.error}</small></div>)}</div>
      )}
      <div className="reservations-refresh"><button className="text-button" onClick={load}><Icon name="refresh" size={16} /> Refresh list</button><span>Stored locally on this device.</span></div>
      {location.state?.newReservationId && <div className="success-toast" role="status"><Icon name="check" /> Reservation #{location.state.newReservationId} has been added to your list.</div>}
    </div>
  );
}
