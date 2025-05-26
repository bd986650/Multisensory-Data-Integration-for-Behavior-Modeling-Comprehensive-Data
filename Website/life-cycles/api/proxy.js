export default async function handler(req, res) {
  if (req.method !== 'GET') {
    return res.status(405).json({ message: 'Method Not Allowed' });
  }

  const { start, stop, metricType } = req.query;

  if (!start || !stop || !metricType) {
    return res.status(400).json({ message: 'Missing required query parameters' });
  }

  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ message: 'No authorization header' });
  }

  // Формируем целевой URL с параметрами
  const targetUrl = `http://51.250.108.190:8080/api/users/get?start=${encodeURIComponent(start)}&stop=${encodeURIComponent(stop)}&metricType=${encodeURIComponent(metricType)}`;

  try {
    const response = await fetch(targetUrl, {
      method: 'GET',
      headers: {
        Authorization: authHeader,
      },
    });

    if (!response.ok) {
      if (response.status === 401) {
        return res.status(401).json({ message: 'Unauthorized' });
      }
      return res.status(response.status).json({ message: 'Error from backend' });
    }

    const data = await response.json();
    res.status(200).json(data);
  } catch (error) {
    console.error('Ошибка на прокси:', error);
    res.status(500).json({ message: 'Internal server error' });
  }
}
