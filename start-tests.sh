docker compose down
docker compose --profile dev down
docker compose -f docker-compose.test.yml up --build
docker compose -f docker-compose.test.yml down -v