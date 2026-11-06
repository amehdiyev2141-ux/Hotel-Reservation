import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Icon from './Icons.jsx';

export function defaultDates() {
  const today = new Date();
  const checkIn = new Date(today);
  checkIn.setDate(today.getDate() + 1);
  const checkOut = new Date(checkIn);
  checkOut.setDate(checkIn.getDate() + 3);
  return { checkIn: dateString(checkIn), checkOut: dateString(checkOut) };
}

export function dateString(date) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

export default function SearchBar({ compact = false, initialDates, onSearch }) {
  const defaults = initialDates || defaultDates();
  const [checkIn, setCheckIn] = useState(defaults.checkIn);
  const [checkOut, setCheckOut] = useState(defaults.checkOut);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  function submit(event) {
    event.preventDefault();
    if (!checkIn || !checkOut || checkOut <= checkIn) {
      setError('Choose a check-out date after check-in.');
      return;
    }
    setError('');
    if (onSearch) onSearch({ checkIn, checkOut });
    else navigate(`/hotels?checkIn=${checkIn}&checkOut=${checkOut}`);
  }

  return (
    <form className={`search-bar ${compact ? 'search-bar-compact' : ''}`} onSubmit={submit}>
      <label className="search-field"><span className="search-field-icon"><Icon name="calendar" /></span><span className="search-label">CHECK IN</span><input aria-label="Check-in date" type="date" min={dateString(new Date())} value={checkIn} onChange={(event) => setCheckIn(event.target.value)} required /></label>
      <span className="search-divider" />
      <label className="search-field"><span className="search-field-icon"><Icon name="calendar" /></span><span className="search-label">CHECK OUT</span><input aria-label="Check-out date" type="date" min={checkIn || dateString(new Date())} value={checkOut} onChange={(event) => setCheckOut(event.target.value)} required /></label>
      <button className="button button-navy search-submit" type="submit"><Icon name="search" size={17} /><span>Find a room</span></button>
      {error && <span className="search-error" role="alert">{error}</span>}
    </form>
  );
}
