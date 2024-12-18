import styles from './DataContainerHealth.module.css'

export function DataContainerHealth({text, data}) {
  return (
    <div className={styles.container}>
        <div className={styles.header}>{text}</div>
        <div className={styles.data}>{data}</div>
    </div>
  );
}