import { test, expect } from '@playwright/test';

test.describe('Zarządzanie użytkownikami', () => {

  test('powinien poprawnie dodać nowego użytkownika do systemu', async ({ page }) => {
    // 1. Logowanie (możesz to przenieść do beforeEach)
    await page.goto('http://localhost:5173/login');
    await page.getByPlaceholder(/username/i).fill('admin');
    await page.getByPlaceholder(/password/i).fill('admin123'); // Użyj poprawnego hasła
    await page.getByRole('button', { name: /login|zaloguj/i }).filter({ visible: true }).click();

    // 2. Nawigacja do sekcji użytkowników
    // Szukamy linku lub przycisku "Users" w menu
    await page.getByRole('link', { name: /users|użytkownicy/i }).click();
    await expect(page).toHaveURL(/.*users/);

    // 3. Otwarcie formularza dodawania
    await page.getByRole('button', { name: /add user|dodaj użytkownika/i }).click();

    // 4. Wypełnianie danych nowego użytkownika
    const uniqueUsername = `testuser_${Date.now()}`; // Unikalna nazwa, żeby test mógł przejść wiele razy
    await page.locator('input[name="username"]').fill(uniqueUsername);
    await page.locator('input[name="email"]').fill(`${uniqueUsername}@example.com`);
    await page.locator('input[name="password"]').fill('SecretPass123!');
    
    // Wybór roli z dropdowna (jeśli masz select)
    await page.locator('select[name="role"]').selectOption('USER');

    // 5. Zapisanie i weryfikacja
    await page.getByRole('button', { name: /save|zapisz/i }).click();

    // Czekamy na powiadomienie o sukcesie (toast) lub zamknięcie modala
    // Sprawdzamy, czy nowy użytkownik pojawił się na liście (tabela)
    const tableRow = page.getByRole('row', { name: uniqueUsername });
    await expect(tableRow).toBeVisible();
    await expect(tableRow).toContainText('USER');
    
    console.log(`Użytkownik ${uniqueUsername} został poprawnie utworzony.`);
  });
});