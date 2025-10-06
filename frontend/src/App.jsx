import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import Main from './pages/MainPage.jsx'

function App() {
  const [count, setCount] = useState(0)
console.log("VITE_API_URL =", import.meta.env.VITE_API_URL);
  return (
    <>
      <Main/>
    </>
  )
}

export default App
