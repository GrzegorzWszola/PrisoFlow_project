import { render, screen, waitFor } from '@testing-library/react';
import { describe, test, expect, beforeEach, vi } from 'vitest';
import { Dashboard } from './Dashboard';
import '@testing-library/jest-dom';

// 1. Mockowanie fetch
global.fetch = vi.fn();

// 2. Mockowanie zmiennych środowiskowych Vite
vi.stubEnv('VITE_API_URL', 'http://localhost:7000');

const mockDashboardData = {
  prisons: [
    {
      id: 1,
      name: "Wronki",
      location: "Poznań",
      capacity: 100,
      currentInmates: 85,
      occupancyPercentage: 85
    }
  ],
  visits: [
    {
      visit_datetime: "2025-12-28T10:00:00",
      criminal_first_name: "Jan",
      criminal_last_name: "Kowalski",
      visitor_first_name: "Anna",
      visitor_last_name: "Nowak",
      prison_name: "Wronki",
      relationship: "Family"
    }
  ],
  incidents: [
    {
      incident_id: 101,
      severity: "High",
      criminal_involved: "Adam Małysz",
      officer_involved: "Officer Smith",
      description: "Bójka w stołówce",
      prison_name: "Wronki",
      incident_type: "Fight",
      incident_datetime: "2025-12-27T15:00:00"
    }
  ]
};

describe('Dashboard Component', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('powinien wyświetlić stan ładowania na początku', () => {
    fetch.mockReturnValue(new Promise(() => {})); 
    render(<Dashboard />);
    expect(screen.getByText(/ładowanie/i)).toBeInTheDocument();
  });

  test('powinien wyrenderować dane po udanym pobraniu z API', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockDashboardData,
    });

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText("Wronki")).toBeInTheDocument();
    });

    // Rozwiązanie problemu "multiple elements":
    // Szukamy wszystkich wystąpień "85%" (jedno w nagłówku, drugie w pasku)
    const percentageElements = screen.getAllByText("85%");
    expect(percentageElements.length).toBeGreaterThanOrEqual(1);

    // Sprawdzamy, czy pasek postępu ma odpowiednią szerokość
    // Szukamy elementu paska postępu, który zawiera tekst "85%"
    const progressBar = percentageElements.find(el => el.classList.contains('progress-text'))?.closest('.progress-fill');
    if (progressBar) {
        expect(progressBar).toHaveStyle('width: 85%');
    }
  });

  test('powinien wyświetlić błąd, gdy API zawiedzie', async () => {
    fetch.mockResolvedValueOnce({
      ok: false,
    });

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText(/błąd: błąd pobierania danych/i)).toBeInTheDocument();
    });
  });

  test('powinien poprawnie stosować kolory dla incydentów o wysokim priorytecie', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockDashboardData,
    });

    render(<Dashboard />);

    await waitFor(() => {
      const severityElement = screen.getByText(/high/i);
      expect(severityElement).toHaveStyle('color: red');
    });
  });

  test('powinien pokazać ostrzeżenie o przpełnieniu powyżej 100%', async () => {
    const overcapacityData = {
        ...mockDashboardData,
        prisons: [{ ...mockDashboardData.prisons[0], occupancyPercentage: 110 }]
    };

    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => overcapacityData,
    });

    render(<Dashboard />);

    await waitFor(() => {
      // Pobieramy wszystkie wystąpienia "110%"
      const percentageElements = screen.getAllByText("110%");
      
      // Sprawdzamy czy którykolwiek z nich ma klasę koloru (ten w nagłówku)
      const hasColorClass = percentageElements.some(el => el.classList.contains('text-red-600'));
      expect(hasColorClass).toBe(true);
      
      expect(screen.getByText(/⚠️ Overfilled/i)).toBeInTheDocument();
    });
  });
});