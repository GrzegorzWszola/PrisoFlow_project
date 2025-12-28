import React, { useState } from 'react';
import { useAuth } from "../../auth/AuthContext.jsx"
import { useNavigate } from 'react-router-dom';
import { login as apiLogin } from "../../api/apiTests.js"
import { toast } from 'react-toastify';
import "./LoginPage.css"

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await apiLogin(username, password);

      login({
        username: response.username,
        email: response.email,
        token: response.token,
        role: response.role
      });

      if (response.role == 'admin') {
        navigate('/admin')
      } else {
        navigate('/');
      }
      
    } catch (err) {
      toast.error(err.message || 'Błąd logowania');
      console.error('Login failed:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Login</h1>
        
        <div className="login-form">
          <div className="form-group">
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Username"
            />
          </div>

          <div className="form-group">
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
            />
          </div>

          <button onClick={handleSubmit}>Zaloguj się</button>
        </div>
      </div>
    </div>
  );
}