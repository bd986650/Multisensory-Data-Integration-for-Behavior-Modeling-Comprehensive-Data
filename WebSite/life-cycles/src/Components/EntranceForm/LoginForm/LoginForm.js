import { useState, useEffect } from "react";
import { ButtonAcceptForm } from "../ButtonAcceptForm/ButtonAcceptForm";
import { FieldForInput } from "../FieldForInput/FieldForInput";
import { FieldForInputPassword } from "../FieldForInputPassword/FieldForInputPassword";
import styles from "./LoginForm.module.css";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import axios from "axios";

import { loginSuccessAddToken, loginSuccessAddUsername } from "../../../Store/Slices/UserSlice";
import {
  changeStartTimestamp,
  changeStepsInStore,
  changeCoordinatesInStore,
  changeNotificationsInStore,
  changeHeartbeatInStore,
  changeCaloriesInStore,
  changeActiveMinutesInStore
} from "../../../Store/Slices/ChangebleLifeDataSlice";

import { refreshToken } from "./authService";

export function LoginForm() {
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const dispatch = useDispatch();
  const navigate = useNavigate();
  const token = useSelector((state) => state.user.tokenAuthorization);

  useEffect(() => {
    const saved = localStorage.getItem("accessToken");
    if (saved) dispatch(loginSuccessAddToken(saved));
  }, [dispatch]);

  useEffect(() => {
    if (token) {
      fetchAllMetrics(token);
    }
  }, [token]);

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

    try {
      // Steps
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=steps`;
        const raw = await fetchWithRefresh(url, currentToken);
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

      // Coordinates
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=coordinates`;
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => {
          const [lat, lng] = value.split(":").map(Number);
          return { timestamp, coords: [lat, lng] };
        });
        console.log("📍 Координаты с таймштампами:", data);
        dispatch(changeCoordinatesInStore(data));
      }

      // Notifications
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=notification`;
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({ timestamp, value }));
        console.log("🔔 Уведомления с таймштампами: ", data);
        dispatch(changeNotificationsInStore(data));
      }

      // Heartbeat
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=heartbeat`;
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: parseFloat(value),
        }));
        console.log("❤️ Пульс с таймштампами:", data);
        dispatch(changeHeartbeatInStore(data));
      }

      // Calories
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=calories`;
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        dispatch(changeCaloriesInStore(data));
      }

      // Active Minutes
      {
        const url = `/api/proxy?start=${start}&stop=${stop}&metricType=active_minutes`;
        const raw = await fetchWithRefresh(url, currentToken);
        const data = raw.map(({ timestamp, value }) => ({
          timestamp,
          value: Number(value),
        }));
        dispatch(changeActiveMinutesInStore(data));
      }

    } catch (err) {
      console.error("❌ Ошибка при загрузке метрик:", err.message);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const resp = await axios.post(
        "/api/login",
        new URLSearchParams({ username: login, password }),
        { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
      );

      dispatch(loginSuccessAddToken(resp.data.accessToken));
      dispatch(loginSuccessAddUsername(login));
      localStorage.setItem("accessToken", resp.data.accessToken);
      localStorage.setItem("refreshToken", resp.data.refreshToken);
      navigate("/home");

    } catch (err) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.request) {
        setError("Сервер не отвечает. Попробуйте позже.");
      } else {
        setError("Ошибка: " + err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className={styles.container} onSubmit={handleSubmit}>
      <FieldForInput signField="Логин" type="text" login={login} setLogin={setLogin} />
      <FieldForInputPassword
        signField="Пароль"
        password={password}
        setPassword={setPassword}
      />
      {error && <div className={styles.error}>{error}</div>}
      <ButtonAcceptForm
        type="submit"
        text={loading ? "Загрузка..." : "Войти"}
        isDisabled={!login || !password}
      />
    </form>
  );
}
