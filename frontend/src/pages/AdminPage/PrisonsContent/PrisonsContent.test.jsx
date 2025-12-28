import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, beforeEach, vi } from 'vitest';
import { PrisonsContent } from './PrisonsContent';
import '@testing-library/jest-dom';

global.fetch = vi.fn();
vi.stubEnv('VITE_API_URL', 'http://localhost:7000');
window.confirm = vi.fn(() => true);
window.alert = vi.fn();

const mockPrisons = [
    {
        id: 1,
        name: "Central Prison",
        location: "Warszawa",
        capacity: 500,
        securityLevel: "high",
        numOfCells: 100,
        isActive: true
    }
];

describe('PrisonsContent Component', () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('powinien pobrać i wyświetlić listę więzień', async () => {
        fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockPrisons,
        });

        render(<PrisonsContent />);

        // Czekamy aż zniknie "Ładowanie" i pojawi się nazwa więzienia
        await waitFor(() => {
            expect(screen.getByText("Central Prison")).toBeInTheDocument();
        });
    });

    test('powinien otworzyć modal i dodać nowe więzienie', async () => {
        fetch.mockResolvedValueOnce({ ok: true, json: async () => [] }); // początkowa lista
        fetch.mockResolvedValueOnce({ ok: true }); // odpowiedź na POST
        fetch.mockResolvedValueOnce({ ok: true, json: async () => mockPrisons }); // odświeżona lista

        render(<PrisonsContent />);

        // Szukamy przycisku po roli zamiast samego tekstu - to jest pewniejsze
        const addButton = await screen.findByRole('button', { name: /add prison/i });
        fireEvent.click(addButton);

        // Używamy getAllByRole lub szukamy po placeholderze/etykiecie
        // Ponieważ nie masz id w inputach, szukamy po nazwie atrybutu:
        const nameInput = document.querySelector('input[name="name"]');
        const locationInput = document.querySelector('input[name="location"]');
        const capacityInput = document.querySelector('input[name="capacity"]');
        const securitySelect = document.querySelector('select[name="securityLevel"]');
        const cellsInput = document.querySelector('input[name="numOfCells"]');
        const activeSelect = document.querySelector('select[name="isActive"]');

        fireEvent.change(nameInput, { target: { value: 'New Jail' } });
        fireEvent.change(locationInput, { target: { value: 'Kraków' } });
        fireEvent.change(capacityInput, { target: { value: '200' } });
        fireEvent.change(securitySelect, { target: { value: 'normal' } });
        fireEvent.change(cellsInput, { target: { value: '50' } });
        fireEvent.change(activeSelect, { target: { value: 'true' } });

        const submitButton = screen.getByText(/submit/i);
        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/prison/addPrison'),
                expect.any(Object)
            );
        });
    });

    test('powinien usunąć więzienie po potwierdzeniu', async () => {
        fetch.mockResolvedValueOnce({ ok: true, json: async () => mockPrisons });
        fetch.mockResolvedValueOnce({ ok: true }); 
        fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });

        render(<PrisonsContent />);

        // Czekamy na załadowanie danych
        const prisonName = await screen.findByText("Central Prison");
        expect(prisonName).toBeInTheDocument();

        // Szukamy przycisku usuwania wewnątrz karty więzienia
        // W Twoim kodzie przycisk usuwania zawiera ikonę Trash2
        const allButtons = screen.getAllByRole('button');
        // Ostatni przycisk w Twoim mapowaniu to Trash2 (usuwanie)
        const deleteButton = allButtons[allButtons.length - 1];
        
        fireEvent.click(deleteButton);

        expect(window.confirm).toHaveBeenCalled();

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/prison/deletePrison/1'),
                expect.objectContaining({ method: 'DELETE' })
            );
        });
    });

    test('powinien wyświetlić błąd (alert), gdy pola są puste', async () => {
        fetch.mockResolvedValueOnce({ ok: true, json: async () => [] });

        render(<PrisonsContent />);
        
        const addButton = await screen.findByRole('button', { name: /add prison/i });
        fireEvent.click(addButton);
        
        const submitButton = screen.getByText(/submit/i);
        fireEvent.click(submitButton);

        expect(window.alert).toHaveBeenCalledWith("Fill all required fields!");
    });
});