import { useState } from 'react';
import styles from './FieldForInputPassword.module.css';
import { FaEye, FaEyeSlash } from 'react-icons/fa'; 

export function FieldForInputPassword({signField, password, setPassword}) {
  const [showPassword, setShowPassword] = useState(false);

  const handleClick = () => {
    if(showPassword) {
      setShowPassword(false)
    } else {
      setShowPassword(true)
    }
  };

  const handleChange = (e) => {
    setPassword(e.target.value)
  };

  return(
    <div className={styles.container}>
      <div className={styles.signField}>{signField}</div>
      <div className={styles.containerInput}>
        <input className={styles.input} type={showPassword ? 'text' : 'password'} value={password} onChange={handleChange}/>
        <button className={styles.buttonShowPassword} onClick={handleClick}>
          {showPassword ? <FaEye/> : <FaEyeSlash/>}
        </button>
      </div>
    </div>
  );
}