import React, { useState, useEffect } from 'react';
import { useAuth } from '../../auth/AuthContext';
import './AdminPage.css';
import { Dashboard } from "./Dashboard/Dashboard.jsx"
import { UsersContent } from "./UsersContent/UsersContent.jsx"
import { PrisonsContent } from "./PrisonsContent/PrisonsContent.jsx"

const AdminPage = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const { user } = useAuth()

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: '📊' },
    { id: 'users', label: 'User managment', icon: '👥' },
    { id: 'prisons', label: 'Prisons managment', icon: '🏢' },
    { id: 'backups', label: 'Backups', icon: '💾' },
  ];

  const renderContent = () => {
    switch(activeTab) {
      case 'dashboard':
        return <Dashboard />;
      case 'users':
        return <UsersContent />;
      case 'prisons':
        return <PrisonsContent />;
      case 'backups':
        return <BackupsContent />;
      default:
        return <DashboardContent />;
    }
  };

  return (
    <div className="Admin">
      {/* Header */}
      <header className="Admin_Header">
        <div className="Admin_Header-left">
            <div>
            <button 
                className="sidebar-toggle"
                onClick={() => setSidebarOpen(!sidebarOpen)}
            >
                ☰
            </button>
            </div>

          <h1>Admin Panel</h1>
        </div>
        <div className="Admin_Header-right">
          <span className="admin-name">{user?.username}</span>
        </div>
      </header>

      <div className="Admin_Container">
        {/* Sidebar */}
        <aside className={`admin-sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
          <nav className="sidebar-nav">
            {menuItems.map(item => (
              <button
                key={item.id}
                className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
                onClick={() => setActiveTab(item.id)}
              >
                <span className="nav-icon">{item.icon}</span>
                {sidebarOpen && <span className="nav-label">{item.label}</span>}
              </button>
            ))}
          </nav>
        </aside>

        {/* Main Content */}
        <main className="admin-content">
          {renderContent()}
        </main>
      </div>
    </div>
  );
};

const BackupsContent = () => {
  const [latest, setLatest] = useState(null);
  const [backups, setBackups] = useState([]);

  const fetchBackups = async () => {
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/admin/backup/list`)
      if (!response.ok) {
          throw new Error('Błąd pobierania danych');
      }

      const data = await response.json()
      setBackups(data)
    } catch (error) {
      console.error('Error while getting backuplist:', error);
      alert('There was an error while getting backupList.');
    }
  }

  useEffect(() => {
    fetchBackups();
  }, []);

  const createBackup = async () => {
    await fetch(`${import.meta.env.VITE_API_URL}/api/admin/backup/create`, { method: "POST" }) 
    await fetchBackups()
  };

  const restoreBackup = (file) => {
    fetch(`${import.meta.env.VITE_API_URL}/api/admin/backup/restore/${file}`, { method: "POST" })
      .then(() => alert("Restored!"));
  };

  const removeBackup = async (file) => {
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/api/admin/backup/remove/${file}`, { method: "POST" })
      await fetchBackups()
    } catch (error) {
      console.error('Error while removing backup:', error);
      alert('There was an error while removing backup.');
    }
  }

  return (
    <div className="backup-page">
      <h1>Backup Manager</h1>

      <button className="create-btn" onClick={createBackup}>Create backup</button>

      <h2>Available backups:</h2>
      <ul className="backup-list">
        {backups.map(b => (
          <li key={b} className="backup-item">
            <span>{b}</span>
            <div className="backup-buttons">
              <button onClick={() => restoreBackup(b)}>Restore</button>
              <button onClick={() => removeBackup(b)}>Remove</button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};


export default AdminPage;