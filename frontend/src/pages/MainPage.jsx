import "./MainPage.css"
import { Route, Routes } from 'react-router-dom';

import Header from "../components/header/Header"
import LoginPage from "./LoginPage/LoginPage"
import TestPage from "./testPage/TestPage"
import AdminPage from "./AdminPage/AdminPage"
import ProtectedRoute from "../auth/ProtectedRoute"

function Main() {
    return (
        <>
            <div className="MainPage">
                <div className="MainPage_Header">
                    <Header/>
                </div>
                    {/* <AdminPage/> */}
                <div className="MainPage_Routes">
                    <Routes>
                        <Route path="/" element={<TestPage />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route 
                            path="/admin" 
                            element={<ProtectedRoute element={<AdminPage />} requiredRole="admin" />} 
                        />
                    </Routes>
                </div>
            </div>
        </> 
    )
}

export default Main