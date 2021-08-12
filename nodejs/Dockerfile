FROM node:14

RUN npm install -g typescript

WORKDIR /usr/src/app

COPY package.json ./
COPY package-lock.json ./
RUN npm install

COPY . .
RUN npm run build

EXPOSE 3000
ENV PORT=3000 

CMD ["npm", "run", "start"]