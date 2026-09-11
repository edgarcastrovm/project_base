const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
  res.redirect('/login');
});

router.get('/login', (req, res) => {
  res.render('login', { apiBaseUrl: res.locals.apiBaseUrl });
});

router.get('/home', (req, res) => {
  res.render('home', { apiBaseUrl: res.locals.apiBaseUrl, page: 'home' });
});

router.get('/usuarios', (req, res) => {
  res.render('users', { apiBaseUrl: res.locals.apiBaseUrl, page: 'usuarios' });
});

module.exports = router;
