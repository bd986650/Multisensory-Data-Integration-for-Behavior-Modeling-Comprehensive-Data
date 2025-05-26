// api/login.js

export default async function handler(req, res) {
    if (req.method !== 'POST') {
      return res.status(405).json({ message: 'Method Not Allowed' });
    }
  
    let bodyData = '';
  
    req.on('data', chunk => {
      bodyData += chunk;
    });
  
    req.on('end', async () => {
      try {
        const response = await fetch('http://51.250.108.190:8080/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: bodyData,
        });
  
        const data = await response.json();
        res.status(response.status).json(data);
  
      } catch (error) {
        console.error('Ошибка прокси авторизации:', error);
        res.status(500).json({ message: 'Ошибка авторизации через прокси' });
      }
    });
  }
  