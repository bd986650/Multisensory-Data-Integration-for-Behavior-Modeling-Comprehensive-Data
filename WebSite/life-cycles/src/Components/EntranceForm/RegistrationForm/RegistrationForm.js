import styles from './RegistrationForm.module.css'
import { ButtonAcceptForm } from '../ButtonAcceptForm/ButtonAcceptForm';
import { FieldForInput } from '../FieldForInput/FieldForInput';
import { FieldForInputPassword } from '../FieldForInputPassword/FieldForInputPassword';
import { useState } from 'react';
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useDispatch, useSelector } from "react-redux";
import { loginSuccessAddToken, loginSuccessAddUsername } from '../../../Store/Slices/UserSlice';


export function RegistrationForm() {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const isDisabled = login === '' || password === '';
  const token = useSelector((state) => state.user.tokenAuthorization);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    if (password === passwordConfirm) {
    const url = "http://localhost:8080/api/auth/register"; 

    const requestBody = new URLSearchParams();
    requestBody.append("username", login); 
    requestBody.append("password", password);

    try {
        const response = await axios.post(url, requestBody.toString(), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded', 
            },
        });

        dispatch(loginSuccessAddToken(response.data));
        dispatch(loginSuccessAddUsername(login)); 
        console.log(token);
        navigate("/home");
    } catch (error) {
        if (error.response) {
            setError(
                error.response.data.message ||
                "Не удалось создать пользователся и войти. Проверьте логин и пароль."
            );
        } else if (error.request) {
            setError("Сервер не отвечает. Попробуйте позже.");
        } else {
            setError("Ошибка: " + error.message);
        }
    } finally {
        setLoading(false);
    }
  }
};

  return(
    <form onSubmit={handleSubmit}>
      <div className={styles.text}>Заполните форму для регистрации.</div>
      <FieldForInput signField={'Логин'} type={'text'} login={login} setLogin={setLogin}/>
      <FieldForInputPassword signField={'Пароль'} password={password} setPassword={setPassword}/>
      <FieldForInputPassword signField={'Подтвердите пароль'} password={passwordConfirm} setPassword={setPasswordConfirm}/>
      {error && <div className={styles.error}>{error}</div>}
      <ButtonAcceptForm type={'submit'} text={loading ? "Загрузка..." : "Подтвердить регистрацию"} isDisabled={isDisabled}/>
    </form>
  );
}