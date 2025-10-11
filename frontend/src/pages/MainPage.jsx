import "./MainPage.css"

import Header from "../components/header/Header"
import LoginPage from "./LoginPage/LoginPage"
import TestPage from "./testPage/TestPage"

function Main() {
    return (
        <>
            <div className="MainPage">
                <div className="MainPage_Header">
                    <Header/>
                </div>
                    <TestPage/>
                {/* <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<TestPage />} />
                        <Route path="/login" element={<LoginPage />} />
                    </Routes>
                </BrowserRouter> */}
            </div>
        </> 
    )
}

export default Main