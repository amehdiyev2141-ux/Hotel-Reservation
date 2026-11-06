import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://hotel-reservation-84x6.onrender.com',
  // Render may need time to wake the service before responding.
  timeout: 90000,
});

export const getRooms = async () => (await api.get('/api/rooms')).data;

export const getRoom = async (id) => (await api.get(`/api/rooms/${id}`)).data;

export const getGuest = async (id) => (await api.get(`/api/guests/${id}`)).data;

export const getAvailableRooms = async (checkIn, checkOut) =>
  (await api.get('/api/rooms/available', { params: { checkIn, checkOut } })).data;

export const createGuest = async (guest) => (await api.post('/api/guests', guest)).data;

export const createReservation = async (reservation) =>
  (await api.post('/api/reservations', reservation)).data;

export const getReservation = async (id) => (await api.get(`/api/reservations/${id}`)).data;

export const cancelReservation = async (id) =>
  (await api.patch(`/api/reservations/${id}/cancel`)).data;

export function getApiError(error) {
  if (error.response?.data?.message) return error.response.data.message;
  if (error.code === 'ECONNABORTED') return 'The request took too long. Please try again.';
  if (!error.response) return 'The reservation service could not be reached. It may be waking up; please try again shortly.';
  return 'Something went wrong. Please try again.';
}

export default api;
