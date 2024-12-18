import styles from "./ButtonMenu.module.css";

export function ButtonMenu({ text }) {
  return (
    <div className={styles.container}>
      <button className={styles.button}>
        <div className={styles.text}>{text}</div>
      </button>
    </div>
  );
}
