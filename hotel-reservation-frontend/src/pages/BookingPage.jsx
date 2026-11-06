import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import Icon from '../components/Icons.jsx';
import { LoadingState } from '../components/States.jsx';
import { createGuest, createReservation, getApiError, getRoom } from '../services/api.js';
import { defaultDates, dateString } from '../components/SearchBar.jsx';
import { photoForRoom } from '../components/RoomCard.jsx';
import { rememberReservationId } from '../services/reservationsStore.js';

export default function BookingPage() {
  const { roomId } = useParams();
  const [params] = useSearchParams();
  const defaults = defaultDates();
  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [savedGuest, setSavedGuest] = useState(null);
  const [checkIn, setCheckIn] = useState(params.get('checkIn') || defaults.checkIn);
  const [checkOut, setCheckOut] = useState(params.get('checkOut') || defaults.checkOut);
  const [guestCount, setGuestCount] = useState(2);
  const [form, setForm] = useState({ fullName: '', email: '', phoneNumber: '' });
  const navigate = useNavigate();

  useEffect(() => {
    getRoom(roomId).then(setRoom).catch((err) => setError(getApiError(err))).finally(() => setLoading(false));
  }, [roomId]);

  const nights = useMemo(() => {
    if (!checkIn || !checkOut) return 0;
    const [inYear, inMonth, inDay] = checkIn.split('-').map(Number);
    const [outYear, outMonth, outDay] = checkOut.split('-').map(Number);
    const days = (Date.UTC(outYear, outMonth - 1, outDay) - Date.UTC(inYear, inMonth - 1, inDay)) / 86400000;
    return Math.max(0, days);
  }, [checkIn, checkOut]);
  const total = room ? nights * Number(room.pricePerNight) : 0;
  const guestSignature = `${form.fullName.trim().toLowerCase()}|${form.email.trim().toLowerCase()}|${form.phoneNumber.trim()}`;

  function updateForm(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    if (checkIn < dateString(new Date())) {
      setError('Check-in cannot be in the past. Choose today or a future date.');
      return;
    }
    if (checkOut <= checkIn) {
      setError('Check-out must be after check-in.');
      return;
    }
    setSubmitting(true);
    let guestCreatedDuringSubmit = false;
    try {
      let guestId = savedGuest?.signature === guestSignature ? savedGuest.id : null;
      if (!guestId) {
        const guest = await createGuest({
          fullName: form.fullName.trim(),
          email: form.email.trim(),
          phoneNumber: form.phoneNumber.trim(),
        });
        if (guest?.id === undefined || guest?.id === null) throw new Error('The guest service returned no guest ID.');
        guestId = guest.id;
        setSavedGuest({ id: guestId, signature: guestSignature });
        guestCreatedDuringSubmit = true;
      }

      const reservation = await createReservation({ roomId: Number(roomId), guestId, checkIn, checkOut });
      if (reservation?.id === undefined || reservation?.id === null) throw new Error('The reservation service returned no reservation ID.');
      rememberReservationId(reservation.id);
      navigate('/reservations', { state: { newReservationId: reservation.id } });
    } catch (err) {
      const message = err.response ? getApiError(err) : err.message || getApiError(err);
      setError(guestCreatedDuringSubmit ? `Your guest record was created, but the reservation could not be completed: ${message} Retry without changing your guest details to reuse that record.` : message);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className="page-wrap page-state"><LoadingState label="Preparing your booking" /></div>;
  if (!room) return <div className="page-wrap page-state"><div className="inline-error" role="alert">{error || 'This room is no longer available.'}</div><Link className="text-link" to="/hotels">Back to rooms <Icon name="arrow" size={16} /></Link></div>;

  return (
    <div className="booking-page page-wrap">
      <div className="detail-breadcrumb"><Link to="/hotels">Rooms</Link><Icon name="chevron" size={13} /><Link to={`/hotels/${room.id}`}>Room {room.number}</Link><Icon name="chevron" size={13} /><span>Booking</span></div>
      <div className="page-intro booking-intro"><span className="eyebrow">THE DETAILS, THEN THE DAYDREAM</span><h1>Make it <em>yours.</em></h1><p>Share a few details to request your reservation.</p></div>
      <div className="booking-layout">
        <form className="booking-form" onSubmit={submit}>
          <section className="form-section"><div className="form-section-title"><span className="step-number">01</span><div><h2>Your dates</h2><p>Choose when you’d like to arrive.</p></div></div><div className="form-fields two-cols"><label className="form-field"><span>CHECK IN</span><input type="date" min={dateString(new Date())} value={checkIn} onChange={(event) => setCheckIn(event.target.value)} required /></label><label className="form-field"><span>CHECK OUT</span><input type="date" min={checkIn || dateString(new Date())} value={checkOut} onChange={(event) => setCheckOut(event.target.value)} required /></label></div></section>
          <section className="form-section"><div className="form-section-title"><span className="step-number">02</span><div><h2>Your details</h2><p>Used to create the guest record for this reservation.</p></div></div><div className="form-fields"><label className="form-field"><span>FULL NAME</span><input name="fullName" autoComplete="name" placeholder="Your name" value={form.fullName} onChange={updateForm} required /></label><div className="two-cols"><label className="form-field"><span>EMAIL ADDRESS</span><input name="email" type="email" autoComplete="email" placeholder="you@example.com" value={form.email} onChange={updateForm} required /></label><label className="form-field"><span>PHONE NUMBER</span><input name="phoneNumber" type="tel" autoComplete="tel" placeholder="+1 555 010 2020" value={form.phoneNumber} onChange={updateForm} required /></label></div></div></section>
          <section className="form-section guest-count-section"><div className="form-section-title"><span className="step-number">03</span><div><h2>Who’s coming?</h2><p>Keep track of your group size.</p></div></div><div className="guest-counter"><span><Icon name="users" /> Guests <small>planning only</small></span><div><button type="button" onClick={() => setGuestCount((count) => Math.max(1, count - 1))} aria-label="Remove one guest"><Icon name="minus" size={16} /></button><strong>{guestCount}</strong><button type="button" onClick={() => setGuestCount((count) => Math.min(8, count + 1))} aria-label="Add one guest"><Icon name="plus" size={16} /></button></div></div><p className="field-note">The current reservation API doesn’t accept a guest count, so this number is not sent with your booking.</p></section>
          {error && <div className="inline-error" role="alert">{error}</div>}
          <button className="button button-navy button-full booking-submit" type="submit" disabled={submitting || nights < 1}>{submitting ? <><span className="button-spinner" /> Sending reservation…</> : <>Request reservation <Icon name="arrow" size={17} /></>}</button>
          <p className="no-payment-note">No payment is collected. Your reservation is submitted to the hotel reservation API.</p>
        </form>
        <aside className="booking-summary"><span className="eyebrow">YOUR STAY, AT A GLANCE</span><div className="summary-room-image"><img src={photoForRoom(room.id)} alt="Representative room interior" /></div><div className="summary-room-title"><span>{room.type} · ROOM {room.number}</span><h2>A room to settle in.</h2></div><div className="summary-dates"><div><span>CHECK IN</span><strong>{checkIn || 'Choose a date'}</strong></div><Icon name="arrow" size={16} /><div><span>CHECK OUT</span><strong>{checkOut || 'Choose a date'}</strong></div></div><div className="summary-price-line"><span>${Number(room.pricePerNight).toFixed(2)} × {nights} {nights === 1 ? 'night' : 'nights'}</span><strong>${total.toFixed(2)}</strong></div><div className="summary-total"><span>Estimated total</span><strong>${total.toFixed(2)}</strong></div><p className="summary-footnote">Calculated from the API nightly price. Taxes and other fees aren’t provided by the current API.</p></aside>
      </div>
    </div>
  );
}
