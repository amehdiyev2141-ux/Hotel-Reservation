import { useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import Icon from '../components/Icons.jsx';
import SearchBar from '../components/SearchBar.jsx';
import { ErrorState, LoadingState } from '../components/States.jsx';
import { getApiError, getAvailableRooms, getRoom } from '../services/api.js';
import { photoForRoom } from '../components/RoomCard.jsx';

export default function HotelDetailsPage() {
  const { id } = useParams();
  const [params, setParams] = useSearchParams();
  const checkIn = params.get('checkIn') || '';
  const checkOut = params.get('checkOut') || '';
  const [room, setRoom] = useState(null);
  const [availability, setAvailability] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadRoom() {
    setLoading(true);
    setError('');
    try { setRoom(await getRoom(id)); }
    catch (err) { setError(getApiError(err)); }
    finally { setLoading(false); }
  }

  useEffect(() => { loadRoom(); }, [id]);
  useEffect(() => {
    let active = true;
    if (checkIn && checkOut) {
      setAvailability(null);
      getAvailableRooms(checkIn, checkOut)
        .then((available) => { if (active) setAvailability(available.some((item) => String(item.id) === String(id))); })
        .catch(() => { if (active) setAvailability(null); });
    } else {
      setAvailability(null);
    }
    return () => { active = false; };
  }, [id, checkIn, checkOut]);

  if (loading) return <div className="page-wrap page-state"><LoadingState label="Opening room details" /></div>;
  if (error || !room) return <div className="page-wrap page-state"><ErrorState message={error || 'This room could not be found.'} onRetry={loadRoom} /><Link className="text-link back-link" to="/hotels">Back to rooms <Icon name="arrow" size={16} /></Link></div>;

  const photos = [photoForRoom(room.id), 'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1000&q=85', 'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1000&q=85'];
  const bookingQuery = checkIn && checkOut ? `?checkIn=${checkIn}&checkOut=${checkOut}` : '';
  const canBook = availability !== false;

  return (
    <div className="detail-page page-wrap">
      <div className="detail-breadcrumb"><Link to="/hotels">Rooms</Link><Icon name="chevron" size={13} /><span>Room {room.number}</span></div>
      <div className="detail-title-row"><div><span className="eyebrow">A ROOM TO CALL YOURS</span><h1>{room.type || 'Guest'} room <em>no. {room.number}</em></h1><p>Room {room.number} · {room.type} · Representative room photography</p></div><div className="detail-price"><strong>${Number(room.pricePerNight).toFixed(0)}</strong><span>per night</span></div></div>
      <div className="gallery-grid"><div className="gallery-main"><img src={photos[0]} alt="Representative view of the room interior" /></div><div className="gallery-side"><img src={photos[1]} alt="Warm hotel room details" /><img src={photos[2]} alt="Comfortable hotel lounge" /></div><span className="gallery-caption">Representative editorial photography</span></div>
      <div className="detail-content-grid">
        <section className="detail-description"><span className="eyebrow">THE ROOM</span><h2>Space to settle <em>in.</em></h2><p>A considered {String(room.type || 'guest').toLowerCase()} room, ready to make your own. Room availability, type and nightly price are read directly from the reservation service.</p><div className="amenity-list"><span><Icon name="bed" /> {room.type} room</span><span><Icon name="check" /> Room {room.number}</span><span><Icon name="calendar" /> Flexible dates</span></div><div className={`availability-message ${availability === true ? 'available' : availability === false ? 'unavailable' : ''}`}>{availability === true ? <><Icon name="check" /> Available for your selected dates</> : availability === false ? <><Icon name="close" /> This room isn’t available for those dates</> : <><Icon name="calendar" /> Add dates to check availability</>}</div></section>
        <aside className="detail-book-card"><span className="eyebrow">YOUR STAY</span><p className="detail-book-price"><strong>${Number(room.pricePerNight).toFixed(0)}</strong> <span>/ night</span></p><SearchBar compact initialDates={checkIn && checkOut ? { checkIn, checkOut } : undefined} onSearch={({ checkIn: nextIn, checkOut: nextOut }) => setParams({ checkIn: nextIn, checkOut: nextOut }, { replace: true })} /><div className="book-cta-note">Guest details are collected securely at the next step.</div><Link className={`button button-navy button-full ${!canBook ? 'button-disabled' : ''}`} to={canBook ? `/booking/${room.id}${bookingQuery}` : '#'} onClick={(event) => { if (!canBook) event.preventDefault(); }}>{availability === false ? 'Choose other dates' : 'Continue to booking'} <Icon name="arrow" size={17} /></Link><p className="detail-api-note">No payment is collected on this site.</p></aside>
      </div>
    </div>
  );
}
