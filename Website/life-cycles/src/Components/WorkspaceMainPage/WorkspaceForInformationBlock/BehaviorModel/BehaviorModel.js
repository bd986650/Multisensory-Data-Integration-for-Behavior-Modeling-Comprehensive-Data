import { useSelector } from 'react-redux';
import styles from './BehaviorModel.module.css';

export function BehaviorModel() {
  const currentTimestamp = useSelector((state) => state.time.time);
  const heartbeat = useSelector((state) => state.changebleLifeData.heartbeat);
  const coordinates = useSelector((state) => state.changebleLifeData.coordinates);
  const steps = useSelector((state) => state.changebleLifeData.steps);

  // Функция для поиска последнего значения по времени
  const findLatestBeforeTimestamp = (arr, timestamp) => {
    if (!Array.isArray(arr) || arr.length === 0) return null;

    const targetTime = new Date(timestamp).getTime();
    const filtered = arr.filter(item => new Date(item.timestamp).getTime() <= targetTime);

    if (filtered.length > 0) {
      return filtered.reduce((latest, item) =>
        new Date(item.timestamp).getTime() > new Date(latest.timestamp).getTime() ? item : latest
      );
    }
    return null;
  };

  // Находим текущие значения
  const currentHeartbeatData = findLatestBeforeTimestamp(heartbeat, currentTimestamp);
  const currentStepsData = findLatestBeforeTimestamp(steps, currentTimestamp);
  const currentCoordsData = findLatestBeforeTimestamp(coordinates, currentTimestamp);
  const prevCoordsData = coordinates?.find(c => new Date(c.timestamp) < new Date(currentTimestamp));

  const currentHeartbeat = currentHeartbeatData?.value;
  const currentSteps = currentStepsData?.value;
  const currentCoords = currentCoordsData?.coords;
  const prevCoords = prevCoordsData?.coords;

  // Определяем, движется ли пользователь
  const isMoving = currentCoords && prevCoords && 
    (currentCoords[0] !== prevCoords[0] || currentCoords[1] !== prevCoords[1]);

  // Определяем состояние
  const isSleeping = (currentHeartbeat < 55 && 
                    (!currentSteps || currentSteps === 0) && 
                    !isMoving) || (!currentHeartbeat && 
                      (!currentSteps || currentSteps === 0) && 
                      !isMoving);

  const isWalking = (currentHeartbeat >= 55 && 
                   (currentSteps > 0 || 
                   isMoving)) || (!currentHeartbeat && 
                    (currentSteps > 0 || 
                    isMoving));

  // Форматируем дату
  const formattedDate = new Date(currentTimestamp).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  // Определяем статус
  let status = 'Не определено';
  if (isSleeping) {
    status = 'Сон';
  } else if (isWalking) {
    status = 'Ходьба';
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>Модель поведения</div>
      <div className={styles.date}>{formattedDate}</div>
      <div className={styles.status}>
        {status}
      </div>
    </div>
  );
} 