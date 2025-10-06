# Etap builda
FROM node:20 AS build
WORKDIR /app

# Kopiujemy tylko package.json, żeby zainstalować zależności (cache Docker)
COPY package*.json ./
RUN npm install

ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Kopiujemy resztę kodu i budujemy produkcję
COPY . .
RUN npm run build

# Etap produkcyjny – serwowanie statycznych plików przez nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]