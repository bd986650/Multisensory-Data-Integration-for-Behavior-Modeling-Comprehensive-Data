import { ButtonMenu } from '../ButtonMenu/ButtonMenu';
import styles from './Menu.module.css'

export function Menu() {
  return(
    <div className={styles.container}>
      <div className={styles.menu}>
        <ButtonMenu text={'mem'}/>
      </div>
    </div>
  );
}