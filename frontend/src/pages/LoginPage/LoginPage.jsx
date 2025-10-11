import React, { useState } from 'react';
import "./LoginPage.css"

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Login:', { username, password });
    alert('Logowanie!');
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