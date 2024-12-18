import styles from "./InformationBlockProfile.module.css";
import imageAvatar from '../../../Images/AccountCircle.png'
import { ButtonInformationBlock } from "../ButtonInformationBlock/ButtonInformationBlock";
import money from '../../../Images/Money.svg'; 
import help from '../../../Images/Help.svg'; 
import exit from '../../../Images/Exit.svg';
import { useSelector } from "react-redux";

export function InformationBlockProfile() {
  const nickName = useSelector((state) => state.user.user);

  return (
    <div className={`${styles.informationBlock} ${styles.informationBlockPosition}`}>
      <div className={styles.avatar}>
        <img alt='' src={imageAvatar} className={styles.imageAvatar}/>
      </div>
      <div className={styles.nickName}>{nickName}</div>
      <ButtonInformationBlock text={'поддержать'} image={money}/>
      <ButtonInformationBlock text={'помощь'} image={help}/>
      <ButtonInformationBlock text={'выход'} image={exit}/>
    </div>
  );
}