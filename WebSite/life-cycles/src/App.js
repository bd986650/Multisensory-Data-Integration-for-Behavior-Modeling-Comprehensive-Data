import "./App.css";
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom'; // добавил Navigate
import { MainPage } from "./Pages/MainPage/MainPage";
import { EntrancePage } from './Pages/EntrancePage/EntrancePage';
import { AuthCheck } from './Components/AuthCheck/AuthCheck';
import SberPage from './Pages/SberPage/SberPage';

function App() {
  return (
    <Router>
      <AuthCheck />
      <Routes>
        {/* <Route path='/' element={<Navigate replace to="/home" />} />  // добавил эту строку */}
        <Route path='/' element={<EntrancePage />}/>
        <Route path='/home' element={<MainPage />}/>
        <Route path='/sber' element={<SberPage />}/>
        {/* <Route path='/' element={<MainPage />}/> */}
      </Routes>
    </Router>
  );
}
export default App;
