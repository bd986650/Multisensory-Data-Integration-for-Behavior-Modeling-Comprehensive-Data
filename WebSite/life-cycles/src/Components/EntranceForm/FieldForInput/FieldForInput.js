import styles from './FieldForInput.module.css';

export function FieldForInput({signField, type, login, setLogin}) {
  const handleChange = (e) => {
    setLogin(e.target.value);
  };
  
  return(
    <div className={styles.container}>
      <div className={styles.signField}>{signField}</div>
      <div className={styles.containerInput}>
        <input className={styles.input} type={type} value={login} onChange={handleChange}/>
      </div>
    </div>
  );
}