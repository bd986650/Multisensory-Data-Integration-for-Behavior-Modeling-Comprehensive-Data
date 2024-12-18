import { ButtonMenu } from '../ButtonMenu/ButtonMenu';
import { Logo } from '../Logo/Logo';
import { Profile } from '../Profile/Profile';
import styles from './Header.module.css'

export function Header() {
  return(
    <div className={styles.header}>
      <div className={styles.leftPartHeader}>
        <Logo/>
        <ButtonMenu text={'меню'}/>
        <ButtonMenu text={'меню'}/>
      </div>
      <div className={styles.rightPartHeader}>
        <Profile/>
      </div>
    </div>
  );
}