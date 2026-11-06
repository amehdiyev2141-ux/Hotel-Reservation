import { Link } from 'react-router-dom';
import Icon from './Icons.jsx';

export const roomPhotos = [
  'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1000&q=85',
  'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1000&q=85',
  'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1000&q=85',
  'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1000&q=85',
  'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=1000&q=85',
  'https://images.unsplash.com/photo-1615874694520-474822394e73?auto=format&fit=crop&w=1000&q=85',
];

export function photoForRoom(id) {
  const index = Number(id || 0) % roomPhotos.length;
  return roomPhotos[index];
}

export default function RoomCard({ room, dates }) {
  const type = room.type?.toLowerCase() || 'guest room';
  const query = dates?.checkIn && dates?.checkOut
    ? `?checkIn=${dates.checkIn}&checkOut=${dates.checkOut}`
    : '';

  return (
    <article className="room-card">
      <Link className="room-card-image" to={`/hotels/${room.id}${query}`} aria-label={`View room ${room.number}`}>
        <img src={photoForRoom(room.id)} alt="Representative hotel room interior" loading="lazy" />
        <span className="image-label">Room {room.number}</span>
        <span className="image-arrow"><Icon name="arrow" size={17} /></span>
      </Link>
      <div className="room-card-body">
        <div className="room-card-kicker"><span>{type} stay</span><span><Icon name="bed" size={15} /> Room {room.number}</span></div>
        <h3><Link to={`/hotels/${room.id}${query}`}>{room.type || 'Guest'} room, thoughtfully kept</Link></h3>
        <div className="room-card-bottom"><p><strong>${Number(room.pricePerNight).toFixed(0)}</strong><span> / night</span></p><Link className="text-link" to={`/hotels/${room.id}${query}`}>View details <Icon name="chevron" size={15} /></Link></div>
      </div>
    </article>
  );
}
