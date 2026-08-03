const TOKEN_KEY = 'token';
const USERNAME_KEY = 'username';

export const getToken = () => localStorage.getItem(TOKEN_KEY);

export const getUsername = () => localStorage.getItem(USERNAME_KEY);

export const saveAuth = (token, username) => {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USERNAME_KEY, username);
};

export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USERNAME_KEY);
};
