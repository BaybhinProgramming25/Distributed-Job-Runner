import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import PublicLayout from './components/PublicLayout/PublicLayout.jsx';
import Home from './components/Home/Home.jsx';
import Login from './components/Auth/Login.jsx';
import Signup from './components/Auth/Signup.jsx';
import ProtectedRoute from './protected/ProtectedRoute.jsx';
import Dashboard from './protected/Dashboard/Dashboard.jsx';

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path='/' element={<PublicLayout><Home /></PublicLayout>} />
        <Route path='/login' element={<PublicLayout><Login /></PublicLayout>} />
        <Route path='/signup' element={<PublicLayout><Signup /></PublicLayout>} />
        <Route path='/dashboard' element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path='*' element={<Navigate to='/' />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
