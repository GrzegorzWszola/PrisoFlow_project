import "./MainPage.css"

import TestPage from "./testPage/TestPage"
import Header from "../components/header/Header"

function Main() {
    return (
        <>
            <div className="MainPage">
                <div className="MainPage_Header">
                    <Header/>
                </div>
                <div>
                    <TestPage/>
                </div>
            </div>
        </> 
    )
}

export default Main