import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { loginSuccessAddToken } from '../../Store/Slices/UserSlice';
import {
  changeStartTimestamp,
  changeStepsInStore,
  changeCoordinatesInStore,
  changeNotificationsInStore,
  changeHeartbeatInStore,
  changeCaloriesInStore,
  changeActiveMinutesInStore
} from '../../Store/Slices/ChangebleLifeDataSlice';
import { refreshToken } from '../EntranceForm/LoginForm/authService';

export function AuthCheck() {
  const dispatch = useDispatch();
  const token = useSelector((state) => state.user.tokenAuthorization);

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

  const fetchAllMetrics = async (currentToken) => {
    const stop = new Date().toISOString();
    const start = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString();

    console.log("🔄 Начинаю загрузку метрик...");

    try {
      // Steps
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=steps`;
        console.log("📊 Запрашиваю шаги...");
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        if (data.length > 0) {
          dispatch(changeStartTimestamp(data[0].timestamp));
        }
        dispatch(changeStepsInStore(data));
        console.log("✅ Шаги загружены:", data.length, "записей");
      }

      // Coordinates
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=coordinates`;
        console.log("📍 Запрашиваю координаты...");
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => {
          const [lat, lng] = value.split(":").map(Number);
          return { timestamp, coords: [lat, lng] };
        });
        dispatch(changeCoordinatesInStore(data));
        console.log("✅ Координаты загружены:", data.length, "записей");
      }

      // Notifications
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=notification`;
        console.log("🔔 Запрашиваю уведомления...");
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({ timestamp, value }));
        dispatch(changeNotificationsInStore(data));
        console.log("✅ Уведомления загружены:", data.length, "записей");
      }

      // Heartbeat
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=heartbeat`;
        console.log("❤️ Запрашиваю пульс...");
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: parseFloat(value),
        }));
        dispatch(changeHeartbeatInStore(data));
        console.log("✅ Пульс загружен:", data.length, "записей");
      }

      // Calories
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=calories`;
        console.log("🔥 Запрашиваю калории...");
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        dispatch(changeCaloriesInStore(data));
        console.log("✅ Калории загружены:", data.length, "записей");
      }

      // Active Minutes
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=active_minutes`;
        console.log("⏱️ Запрашиваю активные минуты...");
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        dispatch(changeActiveMinutesInStore(data));
        console.log("✅ Активные минуты загружены:", data.length, "записей");
      }

      console.log("✨ Все метрики успешно загружены!");

    } catch (err) {
      console.error("❌ Ошибка при загрузке метрик:", err.message);
    }
  };

  useEffect(() => {
    const savedToken = localStorage.getItem("accessToken");
    console.log("🔍 Проверяю сохраненный токен...");
    if (savedToken) {
      console.log("✅ Найден сохраненный токен");
      dispatch(loginSuccessAddToken(savedToken));
    } else {
      console.log("❌ Токен не найден");
    }
  }, [dispatch]);

  useEffect(() => {
    console.log("🔄 Проверяю наличие токена в store...");
    if (token) {
      console.log("✅ Токен найден в store, начинаю загрузку метрик");
      fetchAllMetrics(token);
    } else {
      console.log("❌ Токен отсутствует в store");
    }
  }, [token]);

  return null;
} 