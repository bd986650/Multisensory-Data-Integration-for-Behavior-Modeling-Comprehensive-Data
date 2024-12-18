import styles from './DataContainerSocial.module.css'

export function DataContainerSocial({text, data, textNoData}) {
  return (
    <div className={styles.container}>
        <div className={styles.header}>{text}</div>
        {data.length > 0 ? (
          data.map((app, index) => (
            <div key={index}>
              {app.appName}: {app.timeInForeground} секунд
            </div>
          ))
        ) : (
          <div className={styles.textNoData}>{textNoData}</div>
        )}
    </div>
  );
}