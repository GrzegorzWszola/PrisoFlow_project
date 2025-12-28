import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, beforeEach, vi } from 'vitest';
import AdminPage from './AdminPage';
import '@testing-library/jest-dom';

// 1. Mockowanie modułu AuthContext - ZAMIAST PROVIDERA
// To rozwiązuje błąd "Cannot read properties of undefined (reading 'Provider')"
vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({
    user: { username: 'admin_test' }
  })
}));

// 2. Mockowanie komponentów podrzędnych
vi.mock('./Dashboard/Dashboard.jsx', () => ({
  Dashboard: () => <div data-testid="dashboard-content">Dashboard Component</div>
}));
vi.mock('./UsersContent/UsersContent.jsx', () => ({
  UsersContent: () => <div data-testid="users-content">Users Component</div>
}));
vi.mock('./PrisonsContent/PrisonsContent.jsx', () => ({
  PrisonsContent: () => <div data-testid="prisons-content">Prisons Component</div>
}));

// 3. Globalne mocki
global.fetch = vi.fn();
vi.stubEnv('VITE_API_URL', 'http://localhost:7000');
window.alert = vi.fn();

describe('AdminPage Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Domyslna odpowiedź dla backupów, żeby testy nie rzucały błędami fetch
    fetch.mockResolvedValue({ ok: true, json: async () => [] });
  });

  test('powinien wyświetlić nazwę zalogowanego admina w nagłówku', () => {
    render(<AdminPage />);
    // Szukamy 'admin_test' bo tak zdefiniowaliśmy w mocku na górze
    expect(screen.getByText('admin_test')).toBeInTheDocument();
  });

  test('powinien przełączać treści po kliknięciu w menu boczne', async () => {
    render(<AdminPage />);

    // Domyślnie Dashboard
    expect(screen.getByTestId('dashboard-content')).toBeInTheDocument();

    // Klikamy User Management
    fireEvent.click(screen.getByText(/user managment/i));
    expect(screen.getByTestId('users-content')).toBeInTheDocument();

    // Klikamy Prisons Management
    fireEvent.click(screen.getByText(/prisons managment/i));
    expect(screen.getByTestId('prisons-content')).toBeInTheDocument();
  });

  test('powinien chować/pokazywać etykiety menu po kliknięciu hamburgera', () => {
    render(<AdminPage />);
    
    expect(screen.getByText('Dashboard')).toBeInTheDocument();

    const toggleBtn = screen.getByText('☰');
    fireEvent.click(toggleBtn);

    // queryByText zwraca null zamiast błędu, co pozwala sprawdzić nieobecność
    expect(screen.queryByText('Dashboard')).not.toBeInTheDocument();
  });
});

describe('BackupsContent (wewnątrz AdminPage)', () => {
  const mockBackups = ['backup_2023.sql', 'backup_2024.sql'];

  test('powinien pobrać i wyświetlić listę dostępnych backupów', async () => {
    fetch.mockResolvedValueOnce({ ok: true, json: async () => mockBackups });
    
    render(<AdminPage />);
    fireEvent.click(screen.getByText(/backups/i));

    await waitFor(() => {
      expect(screen.getByText('backup_2023.sql')).toBeInTheDocument();
      expect(screen.getByText('backup_2024.sql')).toBeInTheDocument();
    });
  });

  test('powinien wywołać tworzenie nowego backupu', async () => {
    fetch.mockResolvedValue({ ok: true, json: async () => [] });
    
    render(<AdminPage />);
    fireEvent.click(screen.getByText(/backups/i));

    const createBtn = await screen.findByText(/create backup/i);
    fireEvent.click(createBtn);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/admin/backup/create'),
        expect.objectContaining({ method: 'POST' })
      );
    });
  });

  test('powinien wywołać usunięcie backupu', async () => {
    fetch.mockResolvedValueOnce({ ok: true, json: async () => ['file1.sql'] });
    fetch.mockResolvedValueOnce({ ok: true });
    fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<AdminPage />);
    fireEvent.click(screen.getByText(/backups/i));

    const removeBtn = await screen.findByText('Remove');
    fireEvent.click(removeBtn);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/admin/backup/remove/file1.sql'),
        expect.any(Object)
      );
    });
  });
});