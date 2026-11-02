FROM node:26-slim

WORKDIR /app

COPY package*.json ./

RUN npm install --production

COPY . .

EXPOSE 8085

CMD ["npm", "start"]
