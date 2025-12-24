export const getEnv = () => {
  const env = import.meta.env
  return {
    appTitle: env.VITE_APP_TITLE,
    appPort: env.VITE_APP_PORT,
    apiUrl: env.VITE_APP_BASE_URL
  }
}