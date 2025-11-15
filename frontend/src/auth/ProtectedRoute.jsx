import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext'

const ProtectedRoute = ({ element, requiredRole = null }) => {
  const { user } = useAuth(); // Destruct 'user' z kontekstu!

  // Sprawdzenie czy użytkownik jest zalogowany
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Sprawdzenie czy ma wymaganą rolę (jeśli rola jest wymagana)
  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/" replace />;
  }

  // Jeśli wszystko OK, renderuj chronioną stronę
  return element;
};

export default ProtectedRoute;