import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, beforeEach, vi } from 'vitest';
import { UsersContent } from './UsersContent';
import '@testing-library/jest-dom';

// Globalne mocki
global.fetch = vi.fn();
vi.stubEnv('VITE_API_URL', 'http://localhost:7000');
window.confirm = vi.fn(() => true);
window.alert = vi.fn();

const mockUsers = [
    {
        id: 1,
        username: "jkowalski",
        email: "jan@test.pl",
        role: "admin",
        password: "password123"
    }
];

describe('UsersContent Component', () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('powinien pobrać i wyświetlić listę użytkowników', async () => {
        fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockUsers,
        });

        render(<UsersContent />);

        await waitFor(() => {
            expect(screen.getByText("jkowalski")).toBeInTheDocument();
            expect(screen.getByText("jan@test.pl")).toBeInTheDocument();
        });
    });

    test('powinien dodać nowego użytkownika przez formularz', async () => {
        fetch.mockResolvedValueOnce({ ok: true, json: async () => [] }); // początek
        fetch.mockResolvedValueOnce({ ok: true }); // POST
        fetch.mockResolvedValueOnce({ ok: true, json: async () => [...mockUsers] }); // odświeżenie

        render(<UsersContent />);

        const addButton = await screen.findByRole('button', { name: /add user/i });
        fireEvent.click(addButton);

        // Wypełnianie pól za pomocą querySelector (ze względu na brak id)
        fireEvent.change(document.querySelector('input[name="username"]'), { target: { value: 'nowy_user' } });
        fireEvent.change(document.querySelector('input[name="email"]'), { target: { value: 'nowy@test.pl' } });
        fireEvent.change(document.querySelector('input[name="password"]'), { target: { value: 'tajne123' } });
        fireEvent.change(document.querySelector('select[name="role"]'), { target: { value: 'user' } });

        fireEvent.click(screen.getByText(/submit/i));

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/user/addUser'),
                expect.objectContaining({ method: 'POST' })
            );
        });
    });

    test('powinien otworzyć modal z danymi do edycji', async () => {
        fetch.mockResolvedValueOnce({ ok: true, json: async () => mockUsers });
        
        render(<UsersContent />);

        // Czekamy na załadowanie użytkownika
        await screen.findByText("jkowalski");

        // Znajdujemy wszystkie przyciski wewnątrz div o klasie buttons-users
        const editButtons = document.querySelectorAll('.buttons-users button');
        
        // Pierwszy przycisk w Twoim kodzie to Edit2
        fireEvent.click(editButtons[0]);

        await waitFor(() => {
            const usernameInput = document.querySelector('input[name="username"]');
            expect(usernameInput.value).toBe("jkowalski");
        });
    });

    test('powinien usunąć użytkownika po kliknięciu Trash2', async () => {
        fetch.mockResolvedValueOnce({ ok: true, json: async () => mockUsers });
        fetch.mockResolvedValueOnce({ ok: true }); // delete
        fetch.mockResolvedValueOnce({ ok: true, json: async () => [] }); // refresh

        render(<UsersContent />);

        await waitFor(() => screen.getByText("jkowalski"));

        // Pobieramy przyciski - usuwanie jest drugie w kolejności w div.buttons-users
        const buttons = screen.getAllByRole('button');
        const deleteBtn = buttons[buttons.length - 1]; // Ostatni przycisk to zazwyczaj Trash2
        
        fireEvent.click(deleteBtn);

        expect(window.confirm).toHaveBeenCalledWith(expect.stringContaining("jkowalski"));

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/user/deleteUser/1'),
                expect.objectContaining({ method: 'DELETE' })
            );
        });
    });
});