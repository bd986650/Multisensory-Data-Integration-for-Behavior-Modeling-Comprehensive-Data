import styles from './ButtonInformationBlock.module.css'

export function ButtonInformationBlock({text, image, onClick}) {
  return(
    <button className={styles.button} onClick={onClick}>
      <img alt='' src={image} className={styles.image}/>
      <div className={styles.text}>{text}</div>
    </button>
  );
}
