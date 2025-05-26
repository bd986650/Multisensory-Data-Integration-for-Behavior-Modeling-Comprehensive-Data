export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ message: 'No authorization header' });
  }

  try {
    const response = await fetch('http://51.250.108.190:8080/api/auth/refresh', {
      method: 'POST',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      },
    });

    if (!response.ok) {
      throw new Error(`Error from backend: ${response.statusText}`);
    }

    const data = await response.text();
    res.status(200).send(data);
  } catch (error) {
    console.error('Ошибка прокси refresh:', error);
    res.status(500).json({ message: 'Ошибка обновления токена через прокси' });
  }
} 