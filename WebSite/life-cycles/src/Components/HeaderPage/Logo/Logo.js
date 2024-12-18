import styles from './Logo.module.css'
// import logo from '../../../Images/Logo.png'
import lettersLogo from '../../../Images/LettersLogo.png'

export function Logo() {
  return(
    <div className={styles.containerLogo}>
      {/* <img alt='' src={logo} className={styles.logoImage}/> */}
      <img alt="" src={lettersLogo} className={styles.lettersLogoImage} />
    </div>
  );
}