import { render, screen } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Main from './MainPage';
import '@testing-library/jest-dom';

// 1. Mockowanie podstron, aby test był szybki
vi.mock('./LoginPage/LoginPage', () => ({
  default: () => <div data-testid="login-page">Login Page Content</div>
}));
vi.mock('./AdminPage/AdminPage', () => ({
  default: () => <div data-testid="admin-page">Admin Page Content</div>
}));
vi.mock('./testPage/TestPage', () => ({
  default: () => <div data-testid="test-page">Home/Test Page Content</div>
}));

// 2. Mockowanie Header (nie chcemy go testować tutaj)
vi.mock('../components/header/Header', () => ({
  default: () => <header>Mocked Header</header>
}));

// 3. Mockowanie AuthContext dla ProtectedRoute
vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    user: { role: 'admin' }, // Symulujemy zalogowanego admina
  })
}));

describe('Main Component Routing', () => {
  
  test('powinien wyrenderować TestPage dla ścieżki "/"', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <Main />
      </MemoryRouter>
    );
    expect(screen.getByTestId('test-page')).toBeInTheDocument();
  });

  test('powinien wyrenderować LoginPage dla ścieżki "/login"', () => {
    render(
      <MemoryRouter initialEntries={['/login']}>
        <Main />
      </MemoryRouter>
    );
    expect(screen.getByTestId('login-page')).toBeInTheDocument();
  });

  test('powinien wyrenderować AdminPage dla zalogowanego admina na "/admin"', () => {
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Main />
      </MemoryRouter>
    );
    expect(screen.getByTestId('admin-page')).toBeInTheDocument();
  });
});