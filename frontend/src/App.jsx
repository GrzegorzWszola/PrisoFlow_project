import { useState } from 'react'
import './App.css'
import Main from './pages/MainPage.jsx'
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  const [count, setCount] = useState(0)
console.log("VITE_API_URL =", import.meta.env.VITE_API_URL);
  return (
    <>
      <Main/>
      <ToastContainer 
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        closeOnClick
        pauseOnHover
        draggable
      />
    </>
  )
}

export default App
