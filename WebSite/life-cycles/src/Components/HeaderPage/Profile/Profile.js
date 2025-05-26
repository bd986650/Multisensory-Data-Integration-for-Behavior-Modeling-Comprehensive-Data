import styles from "./Profile.module.css";
// import accountImage from "../../../Images/AccountCircle.png"; // Иконка больше не используется
import { useDispatch } from 'react-redux';
import { logout } from '../../../Store/Slices/UserSlice';
import { useNavigate } from 'react-router-dom';

export function Profile() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch(logout());
    navigate('/'); // Перенаправление на страницу логина (корневой путь)
  };

  return (
      <div className={styles.profile} onClick={handleLogout}>
        <span className={styles.logoutText}>Выйти</span>
      </div>
  );
}
