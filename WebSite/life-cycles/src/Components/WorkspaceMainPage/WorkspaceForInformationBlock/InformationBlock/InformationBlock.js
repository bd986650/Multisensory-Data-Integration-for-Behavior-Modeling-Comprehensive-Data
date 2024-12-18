import { DataContainerHealth } from '../DataContainerHealth/DataContainerHealth';
import { DataContainerSocial } from '../DataContainerSocial/DataContainerSocial';
import styles from './InformationBlock.module.css'
import { useSelector } from "react-redux";

export function InformationBlock() {
  const dataUser = useSelector((state) => state.user.data);

  if (!dataUser) {
    return(
      <div className={styles.container}>
        <div className={styles.headerDataContainer}>загрузка...</div>
      </div>
    );
  }
  
  return (
    <div className={styles.container}>
      <div className={styles.headerDataContainer}>Данные о здоровье</div>
      <div className={styles.dataContainer}>
        <DataContainerHealth text={'Активные минуты:' } data={dataUser.healthData.activeMinutes}/>
        <DataContainerHealth text={'Калории:' } data={dataUser.healthData.calories}/>
        <DataContainerHealth text={'Сердечный ритм:' } data={dataUser.healthData.heartRate}/>
        <DataContainerHealth text={'Шаги:' } data={dataUser.healthData.steps}/>
        <DataContainerHealth text={'Расстояние:' } data={dataUser.healthData.distance}/>
      </div>

      <div className={styles.headerDataContainer}>Социальные данные</div>
      <div className={styles.dataContainer}>
        <DataContainerSocial text={'Использованные приложения:' } data={dataUser.appUsage} textNoData={'Нет данных о использовании приложений.'}/>
        <DataContainerSocial text={'Звонки:' } data={dataUser.callLogs} textNoData={'Нет данных о звоках.'}/>
        <DataContainerSocial text={'Контакты:' } data={dataUser.contacts} textNoData={'Нет данных о контактах.'}/>
        <DataContainerSocial text={'Контакты:' } data={dataUser.notifications} textNoData={'Нет данных об уведомлениях.'}/>
        <DataContainerSocial text={'Контакты:' } data={dataUser.sms} textNoData={'Нет данных о SMS.'}/>
      </div>
    </div>
  );
}