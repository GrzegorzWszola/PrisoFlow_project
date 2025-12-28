import { test, expect } from '@playwright/test';

test.describe('Panel Administratora - E2E', () => {
  
  test('Logowanie i weryfikacja statystyk panelu', async ({ page }) => {
    // 1. Przejdź do strony logowania
    await page.goto('http://localhost:5173/login');

    // 2. Logowanie (używamy danych, które sprawdziliśmy w bazie)
    // Używamy precyzyjnych lokatorów, aby uniknąć błędów
    await page.getByPlaceholder(/username/i).fill('admin');
    await page.getByPlaceholder(/password/i).fill('admin123'); // wpisz hasło pasujące do hasha w DB
    
    // Klikamy przycisk logowania
    await page.getByRole('button', { name: 'Zaloguj się', exact: true }).click();

    // 3. Oczekiwanie na przekierowanie po sukcesie
    // Czekamy na unikalny element strony głównej, aby upewnić się, że backend odpowiedział
    await expect(page).toHaveURL(/.*dashboard|.*admin/);

    // 4. ROZWIĄZANIE TWOJEGO BŁĘDU (Strict Mode)
    // Zamiast locator('h1'), używamy precyzyjnych nazw nagłówków
    const mainTitle = page.getByRole('heading', { name: 'Admin Panel', exact: true });
    await expect(mainTitle).toBeVisible();

    // Weryfikacja pozostałych sekcji (nagłówków h1), które Playwright wcześniej mylił
    await expect(page.getByRole('heading', { name: 'Prisons', exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Visits', exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Latest incidents', exact: true })).toBeVisible();

    // 5. Opcjonalnie: Sprawdzenie czy dane z bazy się wyświetlają
    // Jeśli w bazie masz użytkownika 'admin', sprawdź czy gdzieś widnieje jego nazwa
    await expect(page.locator('body')).toContainText('admin');
  });

  test('powinien zalogować się i dodać nowego użytkownika', async ({ page }) => {
    // 1. Logowanie
    await page.goto('http://localhost:5173/login');
    await page.getByPlaceholder(/username/i).fill('admin');
    await page.getByPlaceholder(/password/i).fill('admin123');
    await page.getByRole('button', { name: 'Zaloguj się', exact: true }).click();

    // 2. Weryfikacja wejścia do panelu
    await expect(page.getByRole('heading', { name: 'Admin Panel' })).toBeVisible();

    // 3. NAWIGACJA: Kliknięcie w zakładkę "User managment" w Sidebarze
    // W Twoim kodzie: { id: 'users', label: 'User managment', icon: '👥' }
    const userTab = page.getByRole('button', { name: '👥 User managment' });
    await userTab.click();

    // 4. OTWARCIE MODALA (komponent UsersContent)
    await page.getByRole('button', { name: 'Add User' }).click();

    // 5. WYPEŁNIENIE FORMULARZA
    const testUser = `tester_${Date.now()}`;
    await page.locator('input[name="username"]').fill(testUser);
    await page.locator('input[name="email"]').fill(`${testUser}@example.com`);
    await page.locator('input[name="password"]').fill('Password123!');
    
    // Wybór roli z Twojego <select>
    await page.locator('select[name="role"]').selectOption('user');

    // 6. SUBMIT
    await page.getByRole('button', { name: 'Submit' }).click();

    // 7. WERYFIKACJA (czy karta z użytkownikiem się pojawiła)
    const userCard = page.locator('.card-users-item').filter({ hasText: testUser });
    await expect(userCard).toBeVisible();
    await expect(userCard).toContainText('user');
  });

  test('powinien móc przełączać się między zakładkami', async ({ page }) => {
    await page.goto('http://localhost:5173/login');
    await page.getByPlaceholder(/username/i).fill('admin');
    await page.getByPlaceholder(/password/i).fill('admin123');
    await page.getByRole('button', { name: 'Zaloguj się', exact: true }).click();

    // Sprawdź czy domyślnie jest Dashboard
    await expect(page.getByRole('heading', { name: 'Admin Panel' })).toBeVisible();

    // Przełącz na Backups
    await page.getByRole('button', { name: '💾 Backups' }).click();
    await expect(page.getByRole('heading', { name: 'Backup Manager' })).toBeVisible();

    // Przełącz na Prisons
    await page.getByRole('button', { name: '🏢 Prisons managment' }).click();
    // Tutaj możesz dodać sprawdzenie nagłówka z komponentu PrisonsContent
  });
});