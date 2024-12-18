import styles from './ButtonAcceptForm.module.css'

export function ButtonAcceptForm({text, isDisabled, type}) {
  return(
    <button type={type} className={styles.button} disabled={isDisabled}>
      <div className={styles.text}>{text}</div>
    </button>
  );
}