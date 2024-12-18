import { useState } from "react";
import styles from "./EntranceForm.module.css";
import logo from "../../../Images/Logo.png";
import lettersLogo from "../../../Images/LettersLogo.png";
import { LoginForm } from "../LoginForm/LoginForm";
import { RegistrationForm } from "../RegistrationForm/RegistrationForm";

export function EntranceForm() {
  const [focusButtonLogin, setFocusButtonLogin] = useState(true);
  const [focusButtonRegistration, setFocusButtonRegistration] = useState(false);

  const handleClickLogin = () => {
    setFocusButtonLogin(true);
    setFocusButtonRegistration(false);
  };

  const handleClickRegistration = () => {
    setFocusButtonLogin(false);
    setFocusButtonRegistration(true);
  };

  return (
    <div className={styles.container}>
      <div className={styles.form}>
        <div className={styles.headerContainer}>
          <div className={styles.logo}>
            <img alt="" src={logo} className={styles.logoImage} />
            <img alt="" src={lettersLogo} className={styles.lettersLogoImage} />
          </div>
        </div>
        <div className={styles.buttonContainer}>
          <button
            className={`${styles.button} ${
              focusButtonLogin ? styles.buttonHover : ""
            }`}
            onClick={handleClickLogin}
          >
            <div
              className={`${styles.textButton} ${
                focusButtonLogin ? styles.textButtonHover : ""
              }`}
            >
              Войти
            </div>
          </button>
          <button
            className={`${styles.button} ${
              focusButtonRegistration ? styles.buttonHover : ""
            }`}
            onClick={handleClickRegistration}
          >
            <div
              className={`${styles.textButton} ${
                focusButtonRegistration ? styles.textButtonHover : ""
              }`}
            >
              Регистрация
            </div>
          </button>
        </div>
        {focusButtonLogin ? <LoginForm /> : <RegistrationForm />}
      </div>
    </div>
  );
}
