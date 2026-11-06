const STORAGE_KEY = 'serein.reservationIds';

export function readReservationIds() {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    return Array.isArray(parsed) ? [...new Set(parsed.map(String))] : [];
  } catch {
    return [];
  }
}

export function rememberReservationId(id) {
  if (id === null || id === undefined) return;
  const ids = readReservationIds();
  localStorage.setItem(STORAGE_KEY, JSON.stringify([String(id), ...ids.filter((item) => item !== String(id))]));
}
