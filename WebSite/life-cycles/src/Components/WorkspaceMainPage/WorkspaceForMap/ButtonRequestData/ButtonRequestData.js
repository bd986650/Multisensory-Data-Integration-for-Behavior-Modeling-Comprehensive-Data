import styles from './ButtonRequestData.module.css';
import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  changeStartTimestamp,
  changeStepsInStore,
  changeCoordinatesInStore,
  changeNotificationsInStore,
  changeHeartbeatInStore,
  changeCaloriesInStore,
  changeActiveMinutesInStore
} from '../../../../Store/Slices/ChangebleLifeDataSlice';
import { refreshToken } from "../../../EntranceForm/LoginForm/authService";

export function ButtonRequestData() {
  const [error, setError] = useState(null);
  const dispatch = useDispatch();
  const tokenFromStore = useSelector((state) => state.user.tokenAuthorization);

  // При перезагрузке страницы пытаемся получить токен из localStorage
  const token = tokenFromStore || localStorage.getItem("accessToken");

  const fetchWithRefresh = async (url, currentToken) => {
    let res = await fetch(url, {
      headers: { Authorization: `Bearer ${currentToken}` },
    });

    if (res.status === 401) {
      const refreshed = await refreshToken(dispatch);
      if (refreshed) {
        res = await fetch(url, {
          headers: { Authorization: `Bearer ${refreshed}` },
        });
      }
    }

    if (!res.ok) {
      throw new Error(`Ошибка при запросе ${url}, статус ${res.status}`);
    }
    return res.json();
  };

  const requestData = async () => {
    if (!token) {
      setError("Токен не найден. Пожалуйста, авторизуйтесь снова.");
      return;
    }

    const stop = new Date().toISOString();
    const start = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString();

    try {
      // Шаги
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=steps`;
        const raw = await fetchWithRefresh(url, token);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        console.log("📊 Шаги с таймштампами:", data);
        if (data.length > 0) {
          dispatch(changeStartTimestamp(data[0].timestamp));
        }
        dispatch(changeStepsInStore(data));
      }

      // Координаты
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=coordinates`;
        const raw = await fetchWithRefresh(url, token);
        const data = raw.map(({ timestamp, value }) => {
          const [lat, lng] = value.split(":").map(Number);
          return { timestamp, coords: [lat, lng] };
        });
        console.log("📍 Координаты с таймштампами:", data);
        dispatch(changeCoordinatesInStore(data));
      }

      // Уведомления
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=notification`;
        const raw = await fetchWithRefresh(url, token);
        const data = raw.map(({ timestamp, value }) => ({ timestamp, value }));
        console.log("🔔 Уведомления с таймштампами:", data);
        dispatch(changeNotificationsInStore(data));
      }

      // Пульс
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=heartbeat`;
        const raw = await fetchWithRefresh(url, token);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: parseFloat(value),
        }));
        console.log("❤️ Пульс с таймштампами:", data);
        dispatch(changeHeartbeatInStore(data));
      }

      // Калории
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=calories`;
        const raw = await fetchWithRefresh(url, token);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        console.log("🔥 Калории с таймштампами:", data);
        dispatch(changeCaloriesInStore(data));
      }

      // Активные минуты
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=active_minutes`;
        const raw = await fetchWithRefresh(url, token);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        console.log("⏱️ Активные минуты с таймштампами:", data);
        dispatch(changeActiveMinutesInStore(data));
      }

    } catch (err) {
      console.error("❌ Ошибка при загрузке метрик:", err.message);
      setError(err.message || "Ошибка при загрузке данных");
    }
  };

  return (
    <button className={styles.button} onClick={requestData}>
      <div className={styles.text}>Загрузить данные</div>
      {error && <div className={styles.error}>{error}</div>}
    </button>
  );
}
