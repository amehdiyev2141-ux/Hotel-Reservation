import Icon from './Icons.jsx';

export function LoadingState({ label = 'Finding your stay' }) {
  return <div className="state-card" role="status"><span className="spinner" /><span>{label}</span></div>;
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="state-card state-error" role="alert">
      <div><strong>We couldn’t load this just yet.</strong><p>{message}</p></div>
      {onRetry && <button className="button button-small button-outline" onClick={onRetry}><Icon name="refresh" size={15} /> Try again</button>}
    </div>
  );
}

export function EmptyState({ title, message, action }) {
  return <div className="empty-state"><span className="empty-mark"><Icon name="bed" size={22} /></span><h3>{title}</h3><p>{message}</p>{action}</div>;
}
