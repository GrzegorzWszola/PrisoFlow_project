docker compose down
docker compose --profile dev down
docker compose -f docker-compose.unit-test.yml up --build --abort-on-container-exit
docker compose -f docker-compose.unit-test.yml down -v