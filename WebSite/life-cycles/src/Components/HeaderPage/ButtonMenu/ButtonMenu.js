import styles from './ButtonMenu.module.css'

export function ButtonMenu({text}) {
  return(
    <button className={styles.button}>
      <div className={styles.text}>{text}</div>
    </button>
  );
}