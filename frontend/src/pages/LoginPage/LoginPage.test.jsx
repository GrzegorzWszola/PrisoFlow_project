import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, beforeEach, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from './LoginPage';

// 1. Mockowanie nawigacji (React Router)
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { 
    ...actual, 
    useNavigate: () => mockNavigate 
  };
});

// 2. Mockowanie kontekstu autoryzacji
const mockLoginContext = vi.fn();
vi.mock('../../auth/AuthContext.jsx', () => ({
  useAuth: () => ({ login: mockLoginContext })
}));

// 3. Mockowanie API
vi.mock('../../api/apiTests.js', () => ({
  login: vi.fn()
}));

// 4. Mockowanie Toastów (naprawiony błąd ESM)
vi.mock('react-toastify', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

// Importy zamockowanych funkcji do użycia w testach
import { login as apiLoginMock } from "../../api/apiTests.js";
import { toast } from 'react-toastify';

describe('LoginPage Component', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('powinien zaktualizować stan username i password przy wpisywaniu', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );
    
    const userInput = screen.getByPlaceholderText(/username/i);
    const passInput = screen.getByPlaceholderText(/password/i);

    fireEvent.change(userInput, { target: { value: 'admin' } });
    fireEvent.change(passInput, { target: { value: 'tajnehaslo' } });

    expect(userInput.value).toBe('admin');
    expect(passInput.value).toBe('tajnehaslo');
  });

  test('powinien wywołać login i nawigować do /admin dla roli admina', async () => {
    // Symulujemy udaną odpowiedź z API
    apiLoginMock.mockResolvedValueOnce({
      username: 'admin',
      email: 'admin@test.com',
      token: 'fake-token',
      role: 'admin'
    });

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText(/username/i), { target: { value: 'admin' } });
    fireEvent.change(screen.getByPlaceholderText(/password/i), { target: { value: 'admin123' } });
    fireEvent.click(screen.getByRole('button', { name: /zaloguj się/i }));

    await waitFor(() => {
      // Sprawdzamy wywołanie funkcji z kontekstu
      expect(mockLoginContext).toHaveBeenCalledWith(expect.objectContaining({ 
        role: 'admin',
        username: 'admin'
      }));
      // Sprawdzamy przekierowanie
      expect(mockNavigate).toHaveBeenCalledWith('/admin');
    });
  });

  test('powinien wyświetlić błąd (toast) przy nieudanym logowaniu', async () => {
    // Symulujemy błąd API
    const errorMessage = 'Błędne dane logowania';
    apiLoginMock.mockRejectedValueOnce(new Error(errorMessage));

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText(/username/i), { target: { value: 'zly_user' } });
    fireEvent.change(screen.getByPlaceholderText(/password/i), { target: { value: 'zle_haslo' } });
    fireEvent.click(screen.getByRole('button', { name: /zaloguj się/i }));

    await waitFor(() => {
      // Sprawdzamy czy toast.error został wywołany z komunikatem błędu
      expect(toast.error).toHaveBeenCalledWith(errorMessage);
    });
  });
});