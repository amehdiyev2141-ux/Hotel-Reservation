import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Icon from '../components/Icons.jsx';
import RoomCard from '../components/RoomCard.jsx';
import SearchBar from '../components/SearchBar.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/States.jsx';
import { getApiError, getRooms } from '../services/api.js';

const destinations = [
  { name: 'The quiet streets of Kyoto', region: 'JAPAN · EDITORIAL', image: 'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1000&q=85' },
  { name: 'A slower side of Lisbon', region: 'PORTUGAL · EDITORIAL', image: 'https://images.unsplash.com/photo-1555881400-74d7acaacd8b?auto=format&fit=crop&w=1000&q=85' },
  { name: 'Along the Amalfi coast', region: 'ITALY · EDITORIAL', image: 'https://images.unsplash.com/photo-1533105079780-92b9be482077?auto=format&fit=crop&w=1000&q=85' },
];

export default function HomePage() {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadRooms() {
    setLoading(true);
    setError('');
    try {
      const data = await getRooms();
      setRooms(Array.isArray(data) ? data.slice(0, 3) : []);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadRooms(); }, []);

  return (
    <>
      <section className="hero page-wrap">
        <div className="hero-copy">
          <span className="eyebrow"><span className="eyebrow-line" /> STAY A LITTLE CLOSER TO YOURSELF</span>
          <h1>A place to<br /> <em>come back to.</em></h1>
          <p>Find a room that makes the journey feel just as good as getting there.</p>
          <a className="hero-text-link" href="#featured">Discover your next stay <Icon name="arrow" size={16} /></a>
        </div>
        <div className="hero-visual">
          <img src="https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1800&q=90" alt="Sunlit, warm-toned hotel suite with an inviting sitting area" />
          <div className="hero-photo-caption"><span>01 / 03</span><span>Somewhere to slow down</span></div>
          <div className="hero-stamp"><span>STAY</span><span>WELL</span><i>✳</i></div>
        </div>
        <div className="hero-search"><SearchBar /></div>
      </section>

      <section className="section page-wrap featured-section" id="featured">
        <div className="section-heading section-heading-row">
          <div><span className="eyebrow">A FEW PLACES TO BEGIN</span><h2>Rooms worth <em>lingering in.</em></h2></div>
          <Link className="text-link desktop-only" to="/hotels">See all rooms <Icon name="arrow" size={16} /></Link>
        </div>
        {loading ? <LoadingState label="Finding available rooms" /> : error ? <ErrorState message={error} onRetry={loadRooms} /> : rooms.length ? (
          <div className="room-grid">{rooms.map((room) => <RoomCard key={room.id} room={room} />)}</div>
        ) : <EmptyState title="Your next stay starts here" message="There are no rooms to show right now. Check back soon or search dates to explore availability." action={<Link className="button button-outline" to="/hotels">Browse rooms</Link>} />}
        <Link className="button button-outline mobile-only see-all-button" to="/hotels">See all rooms <Icon name="arrow" size={16} /></Link>
      </section>

      <section className="editorial-section" id="destinations">
        <div className="page-wrap editorial-inner">
          <div className="section-heading editorial-heading"><span className="eyebrow">NOT ALL WHO WANDER</span><h2>Find your kind<br />of <em>elsewhere.</em></h2><p>Little notes from places we can’t stop thinking about.</p></div>
          <div className="destination-grid">
            {destinations.map((destination, index) => (
              <article className={`destination-card destination-${index + 1}`} key={destination.name}>
                <img src={destination.image} alt={`Editorial travel photograph: ${destination.name}`} loading="lazy" />
                <span className="destination-region">{destination.region}</span>
                <h3>{destination.name}</h3>
              </article>
            ))}
          </div>
          <p className="editorial-disclaimer">Travel inspiration only — destination search isn’t available in the current room reservation API.</p>
        </div>
      </section>

      <section className="closing-cta page-wrap">
        <div className="closing-image"><img src="https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=1500&q=85" alt="Quiet hotel terrace in warm late afternoon light" loading="lazy" /></div>
        <div className="closing-copy"><span className="eyebrow">MAKE ROOM FOR A CHANGE OF SCENE</span><h2>Let the days<br />take <em>their time.</em></h2><p>Pick your dates and see what’s waiting.</p><Link className="button button-navy" to="/hotels">Explore available rooms <Icon name="arrow" size={17} /></Link></div>
      </section>
    </>
  );
}
