import { Link } from "react-router-dom"
import { useAuth } from '../../auth/AuthContext';
import "./Header.css"

import ThemeButton from "./ThemeToggle"

function Header() {
    const { isLoggedIn, user, logout, isAdmin } = useAuth();

return (
<>
    <div className="Header">
        <Link to="/" className="Header_Title">
            Prison<span className="Header_Title_Flow">Flow</span>
        </Link>
        <div className="Header_Nav">   
            <div className="Header_login_logout">
                {!isLoggedIn ? (
                    <Link to="/login">
                    <button>Login</button>
                    </Link>
                ) : (
                    <div className="logout_component">
                        <span>Hello, {user?.username}!</span>
                        {isAdmin() && (
                            <Link to="/admin"><button>Admin Panel</button></Link>
                        )}
                        <button onClick={logout}>Logout</button>
                    </div>
                )}
            </div>
            <div className="Theme_button">
                <ThemeButton/>
            </div>
            
        </div>
    </div>
</> 
)
}

export default Header