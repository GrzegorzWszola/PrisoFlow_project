import "./Header.css"

import ThemeButton from "./ThemeToggle"

function Header() {
return (
<>
    <div className="Header">
        <div className="Header_Title">
            Prison<span className="Header_Title_Flow">Flow</span>
        </div>
        <div>   
            {/* <AdmingButton/> */}
            <ThemeButton/>
        </div>
    </div>
</> 
)
}

export default Header