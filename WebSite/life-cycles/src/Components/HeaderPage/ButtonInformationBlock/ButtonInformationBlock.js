import styles from './ButtonInformationBlock.module.css'

export function ButtonInformationBlock({text, image}) {
  return(
    <button className={styles.button}>
      <img alt='' src={image} className={styles.image}/>
      <div className={styles.text}>{text}</div>
    </button>
  );
}