export function getSessionToken(): string {
  const SESSION_KEY = 'beatthebot_session';
  let token = localStorage.getItem(SESSION_KEY);
  if (!token) {
    token = crypto.randomUUID();
    localStorage.setItem(SESSION_KEY, token);
  }
  return token;
}
