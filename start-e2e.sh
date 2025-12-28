docker compose down
docker compose --profile dev down
docker compose -f docker-compose.test.yml up --build -d
cd frontend
npx playwright test --ui
cd ..
docker compose -f docker-compose.test.yml down -v