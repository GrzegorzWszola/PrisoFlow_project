import { useState, useEffect } from "react";
import "../UsersContent/UserContent.css"
import { Edit2, Trash2, Save, X } from 'lucide-react';

export const UsersContent = () => {
    const [users, setUsers] = useState([])
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [modal, setModal] = useState(false);
    const [isEdit, setIsEdit] = useState(false);
    const [usId, setUsId] = useState(null);
    const [formData, setFormData] = useState({
        username: "",
        email: "",
        password: "",
        role: "",
    });

    const fetchUsers = async() => {
        try {
            const response = await fetch(`${import.meta.env.VITE_API_URL}/api/user/allUsers`)

            if (!response.ok) {
                throw new Error('Błąd pobierania danych');
            }
            
            const data = await response.json();
            setUsers(data)
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchUsers()
    }, [])

    if (loading) {
        return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-xl">Ładowanie...</div>
        </div>
        )
    }
    if (error) return <div>Błąd: {error}</div>;

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.username || !formData.email || !formData.password || !formData.role) {
            alert("Fill all required fields!");
            return;
        }

        if (!isEdit) {
            await addUser();
        } else {
            await editUser();
        }
        

        setFormData({
            username: "",
            email: "",
            password: "",
            role: ""
        });
    }

    const addUser = async () => {
        try {
            const res = await fetch(`${import.meta.env.VITE_API_URL}/api/user/addUser`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData)
            });

            setModal(false);
            await fetchUsers();
        } catch (error) {
            console.error('Error while adding a user:', error);
            alert('There was an error while adding user.');
        }
    };


    const editUser = async () => {
        try {
            const res = await fetch(`${import.meta.env.VITE_API_URL}/api/user/editUser/${usId}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData)
            });

            setModal(false);
            await fetchUsers();
        } catch (error) {
            console.error('Error while editing a user:', error);
            alert('There was an error while editing user.');
        }
    }

    const deleteUser = async (userId, username) => {
        const confirmDelete = window.confirm(
        `Do you want to delete user: ${username}`   
        );
        if (!confirmDelete) {
        return;
        }

        try {
        const response = await fetch(`${import.meta.env.VITE_API_URL}/api/user/deleteUser/${userId}`, {
            method: 'DELETE',
            headers: {
            'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error('Could not delete user');
        }
        setUsers(users.filter(user => user.userId !== userId));
        alert('User deleted succesfully');
        await fetchUsers();
        } catch (error) {
        console.error('Error while deleteing user:', error);
        alert('Error while deleting user try again.');
        }
    };

return(
<>
    <div className="addButton-users">
        <button onClick={() => {setModal(true); setIsEdit(false)}}>Add User</button>
    </div>

    {modal && (
        <div className="modalPage-users">
            <form onSubmit={handleSubmit} className="addForm-users">
                <div className="form-container-users">
                    <label>Username: </label>
                    <input type="text" name="username" value={formData.username} onChange={handleChange}/>
                </div>
                <div className="form-container-users">
                    <label>Email: </label>
                    <input type="email" name="email" value={formData.email} onChange={handleChange}/>
                </div>
                <div className="form-container-users">
                    <label>Password: </label>
                    <input type="password" name="password" value={formData.password} onChange={handleChange}/>
                </div>
                <div className="form-container-users">
                    <label>Role: </label>
                    <select name="role" value={formData.role} onChange={handleChange}>
                        <option value="">-- choose role --</option>
                        <option value="admin">Admin</option>
                        <option value="user">User</option>
                    </select>
                </div>
                <div className="formButtons-users">
                    <button onClick={() => {setModal(false); setFormData({
                        username: "",
                        email: "",
                        password: "",
                        role: ""
                    })}}>Cancel</button>
                    <button type="submit">Submit</button>
                </div>
            </form>
        </div>
    )}

    <div className="userList-users">
        {users.map((user) => (
            <div key={user.id} className="card-users-item">
                <div className="container-users">
                    <div className="label containerI-users">Username:</div>
                    <div className="containerI-users">{user.username}</div>
                </div>
                <div className="container-users">
                    <div className="label containerI-users">Email:</div>
                    <div className="containerI-users">{user.email}</div>
                </div>
                <div className="container-users">
                    <div className="label containerI-users">Role:</div>
                    <div className="containerI-users">{user.role}</div>
                </div>
                <div className="buttons-users">
                    <button onClick={() => {setModal(true); setIsEdit(true); setUsId(user.id); setFormData({
                        username: user.username,
                        email: user.email,
                        password: user.password,
                        role: user.role
                    })}}><Edit2/></button>
                    <button onClick={() => deleteUser(user.id, user.username)}><Trash2/></button>
                </div>
            </div>
        ))}
    </div>
</>
)
}