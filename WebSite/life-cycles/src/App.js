import "./App.css";
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { MainPage } from "./Pages/MainPage/MainPage";
import { EntrancePage } from './Pages/EntrancePage/EntrancePage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path='/' element={<EntrancePage />}/>
        <Route path='/home' element={<MainPage />}/>
      </Routes>
    </Router>
  );
}

export default App;
