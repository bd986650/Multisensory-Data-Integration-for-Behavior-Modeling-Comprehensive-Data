import styles from './ButtonRequestData.module.css';
import axios from "axios";
import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { addData } from '../../../../Store/Slices/UserSlice'


export function ButtonRequestData() {
  const [error, setError] = useState(null);
  const dispatch = useDispatch();
  const token = useSelector((state) => state.user.tokenAuthorization);
  const dateStr = "2024-12-17 19:29:15";
  const date = new Date(dateStr + " UTC");
  const timestamp = Math.floor(date.getTime() / 1000); 
  // const timestamp = Math.floor(Date.now() / 1000);

  const requestData = async () => {
    const url = `http://localhost:8080/api/users/get-data?timestamp=${timestamp}`; 

    try {
      const response = await axios.post(url, null, {
        headers: {
            'Authorization': `Bearer ${token}`, 
        },
      });
      if (response.status === 200) {
        dispatch(addData(response.data))
      } else {
        setError(`Ошибка: ${response.status} ${response.statusText}`)
      }
    } catch (error) {
      if (error.response) {
        setError(
            error.response.data.message ||
            "ошибка при получение данных"
        );
      } else if (error.request) {
        setError("Сервер не отвечает. Попробуйте позже.");
      } else {
        setError("Ошибка: " + error.message);
      }
    }
  }

  return(
    <button className={styles.button} onClick={requestData}>
      <div className={styles.text}>загрузить данные</div>
      {error && <div className={styles.error}>{error}</div>}
    </button>
  );
}