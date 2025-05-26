import { useState, useEffect } from "react";
import Slider from "react-slider";
import { useDispatch, useSelector } from "react-redux";
import { addTimeInStore } from "../../../Store/Slices/TimeSlice";
import styles from "./TimeLine.module.css";

export function Timeline({ onTimeChange }) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const dispatch = useDispatch();

  // Получаем данные из Redux
  const stepsData = useSelector(state => state.changebleLifeData.steps) || [];
  const coordinatesData = useSelector(state => state.changebleLifeData.coordinates) || [];
  const notificationsData = useSelector(state => state.changebleLifeData.notifications) || [];
  const pulseData = useSelector(state => state.changebleLifeData.pulse) || [];
  const startTimestamp = useSelector(state => state.changebleLifeData.startTimestamp);

  // Объединяем все данные в один массив
  const allEvents = [
    ...stepsData.map(item => ({ ...item, type: 'step' })),
    ...coordinatesData.map(item => ({ ...item, type: 'coordinate' })),
    ...notificationsData.map(item => ({ ...item, type: 'notification' })),
    ...pulseData.map(item => ({ ...item, type: 'pulse' }))
  ];

  // Сортируем по времени
  allEvents.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

  useEffect(() => {
    if (allEvents.length > 0 && !startTimestamp) {
      console.warn("Нет стартового таймштампа");
    }
  }, [allEvents, startTimestamp]);

  const formatTimeMinutesWithSeconds = (seconds) => {
    const m = Math.floor(seconds / 60);
    const sec = seconds % 60;
    return `${m} мин ${sec < 10 ? "0" + sec : sec} сек`;
  };

  const formatReadableDate = (isoString) => {
    const date = new Date(isoString);
    const day = date.getDate();
    const monthNames = [
      "января", "февраля", "марта", "апреля", "мая", "июня",
      "июля", "августа", "сентября", "октября", "ноября", "декабря"
    ];
    const month = monthNames[date.getMonth()];
    const hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${day} ${month}, ${hours}:${minutes}`;
  };

  const handleSliderChange = (index) => {
    setCurrentIndex(index);
    if (!allEvents[index]) return;
    const ts = allEvents[index].timestamp;
    dispatch(addTimeInStore(ts));
    onTimeChange?.(ts);
  };

  const currentTimestamp = allEvents[currentIndex]?.timestamp;
  const secondsPassed = currentTimestamp && startTimestamp
    ? Math.floor((new Date(currentTimestamp).getTime() - new Date(startTimestamp).getTime()) / 1000)
    : 0;

  return (
    <div className={styles.container}>
      <h2>Таймлайн</h2>
      {/* <div className={styles.timeline}>
        {allEvents.map((item, idx) => (
          <div key={idx} className={styles.year}>
            {idx % Math.ceil(allEvents.length / 10) === 0 && formatTimeMinutesWithSeconds(
              Math.floor((new Date(item.timestamp).getTime() - new Date(allEvents[0].timestamp).getTime()) / 1000)
            )}
          </div>
        ))}
      </div> */}
      <Slider
        className={styles.slider}
        min={0}
        max={allEvents.length - 1}
        step={1}
        value={currentIndex}
        onChange={handleSliderChange}
        trackClassName={styles.track}
        thumbClassName={styles.thumb}
        renderThumb={({ key, ...props }) => (
          <div key={key} {...props} className={styles.thumb} />
        )}
      />
      {/* <p className={styles.selectedTime}>
        Прошло: {formatTimeMinutesWithSeconds(secondsPassed)}
      </p> */}
      {currentTimestamp && (
        <p className={styles.realTime}>
          Реальное время: {formatReadableDate(currentTimestamp)}
        </p>
      )}
    </div>
  );
}
