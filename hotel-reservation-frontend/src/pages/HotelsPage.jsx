import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import Icon from '../components/Icons.jsx';
import RoomCard from '../components/RoomCard.jsx';
import SearchBar from '../components/SearchBar.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/States.jsx';
import { getApiError, getAvailableRooms, getRooms } from '../services/api.js';

export default function HotelsPage() {
  const [params, setParams] = useSearchParams();
  const checkIn = params.get('checkIn') || '';
  const checkOut = params.get('checkOut') || '';
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');
  const [sortOrder, setSortOrder] = useState('recommended');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const data = checkIn && checkOut
        ? await getAvailableRooms(checkIn, checkOut)
        : await getRooms();
      setRooms(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [checkIn, checkOut]);

  const types = useMemo(() => [...new Set(rooms.map((room) => room.type).filter(Boolean))], [rooms]);
  const filteredRooms = useMemo(() => {
    const result = rooms.filter((room) => typeFilter === 'all' || room.type === typeFilter);
    if (sortOrder === 'low') result.sort((a, b) => a.pricePerNight - b.pricePerNight);
    if (sortOrder === 'high') result.sort((a, b) => b.pricePerNight - a.pricePerNight);
    return result;
  }, [rooms, typeFilter, sortOrder]);

  function updateDates(dates) {
    setParams(dates, { replace: true });
  }

  return (
    <div className="listing-page page-wrap">
      <div className="page-intro"><span className="eyebrow">THE ROOM EDIT</span><h1>Find a room.<br /><em>Find your rhythm.</em></h1><p>Considered spaces for wherever the day takes you.</p></div>
      <SearchBar compact initialDates={checkIn && checkOut ? { checkIn, checkOut } : undefined} />
      <div className="listing-toolbar">
        <div><span className="results-count">{loading ? 'Searching' : `${filteredRooms.length} ${filteredRooms.length === 1 ? 'room' : 'rooms'}`}</span>{checkIn && checkOut && <span className="date-context"> · {checkIn} to {checkOut}</span>}</div>
        <div className="listing-controls">
          <label className="filter-control"><span>TYPE</span><select value={typeFilter} onChange={(event) => setTypeFilter(event.target.value)}><option value="all">All room types</option>{types.map((type) => <option value={type} key={type}>{type}</option>)}</select></label>
          <label className="filter-control"><span>SORT</span><select value={sortOrder} onChange={(event) => setSortOrder(event.target.value)}><option value="recommended">Recommended</option><option value="low">Price: low to high</option><option value="high">Price: high to low</option></select></label>
        </div>
      </div>
      <p className="api-context-note"><Icon name="check" size={15} /> Live room inventory from the reservation API. Hotel names and locations aren’t part of the current backend data.</p>
      {loading ? <LoadingState label="Finding your room" /> : error ? <ErrorState message={error} onRetry={load} /> : filteredRooms.length ? <div className="room-grid listing-grid">{filteredRooms.map((room) => <RoomCard key={room.id} room={room} dates={{ checkIn, checkOut }} />)}</div> : <EmptyState title="No rooms found" message={rooms.length ? 'No rooms match that room type. Try another filter.' : 'No rooms are currently returned by the reservation service. Try different dates or check back later.'} action={<button className="button button-outline" onClick={() => { setTypeFilter('all'); updateDates({}); }}>Clear filters <Icon name="close" size={15} /></button>} />}
      <div className="listing-bottom-note">Images are representative editorial photography, not images returned by the API.</div>
    </div>
  );
}
