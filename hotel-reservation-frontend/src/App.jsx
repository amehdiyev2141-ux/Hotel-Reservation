import { Navigate, Route, Routes } from 'react-router-dom';
import SiteLayout from './components/SiteLayout.jsx';
import BookingPage from './pages/BookingPage.jsx';
import HotelDetailsPage from './pages/HotelDetailsPage.jsx';
import HotelsPage from './pages/HotelsPage.jsx';
import HomePage from './pages/HomePage.jsx';
import ReservationsPage from './pages/ReservationsPage.jsx';

function NotFoundPage() {
  return <div className="page-wrap not-found"><span className="eyebrow">A LITTLE OFF THE MAP</span><h1>This page took<br /><em>a different turn.</em></h1><a className="button button-navy" href="/">Back to the beginning</a></div>;
}

export default function App() {
  return (
    <Routes>
      <Route element={<SiteLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/hotels" element={<HotelsPage />} />
        <Route path="/hotels/:id" element={<HotelDetailsPage />} />
        <Route path="/booking/:roomId" element={<BookingPage />} />
        <Route path="/reservations" element={<ReservationsPage />} />
        <Route path="/home" element={<Navigate to="/" replace />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
