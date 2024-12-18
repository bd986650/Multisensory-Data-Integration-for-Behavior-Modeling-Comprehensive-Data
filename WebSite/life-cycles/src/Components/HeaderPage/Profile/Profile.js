import styles from "./Profile.module.css";
import accountImage from "../../../Images/AccountCircle.png";
import OutsideClickHandler from "react-outside-click-handler";
import { useState } from "react";
import { InformationBlockProfile } from "../InformationBlockProfile/InformationBlockProfile";

export function Profile() {
  const [focusProfile, setFocusProfile] = useState(false);

  const handleClickProfile = () => {
    setFocusProfile(true);
  };

  const outsideClick = () => {
    setFocusProfile(false);
  }
  return (
    <OutsideClickHandler onOutsideClick={outsideClick}>
      <div className={styles.profile} onClick={handleClickProfile}>
        <img alt="" src={accountImage} className={styles.accountImage} />
      </div>
      {focusProfile && <InformationBlockProfile/>}
    </OutsideClickHandler>
  );
}
