require('dotenv').config();

const express = require('express');
const path = require('path');
const pagesRouter = require('./routes/pages');

const app = express();

const PORT = process.env.PORT || 3000;
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api';

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

app.use(express.static(path.join(__dirname, 'public')));

// Expone la URL base de la API a todas las vistas
app.use((req, res, next) => {
  res.locals.apiBaseUrl = API_BASE_URL;
  next();
});

app.use('/', pagesRouter);

app.use((req, res) => {
  res.status(404).render('login', { apiBaseUrl: API_BASE_URL, notFound: true });
});

app.listen(PORT, () => {
  console.log(`Frontend disponible en http://localhost:${PORT}`);
  console.log(`Consumiendo API en ${API_BASE_URL}`);
});
