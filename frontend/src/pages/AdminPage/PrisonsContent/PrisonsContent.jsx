import { useState, useEffect } from "react";
import "../PrisonsContent/PrisonContent.css";
import { Edit2, Trash2, Save, X } from 'lucide-react';

export const PrisonsContent = () => {
    const [prisons, setPrisons] = useState([])
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [modal, setModal] = useState(false);
    const [isEdit, setIsEdit] = useState(false);
    const [prId, setPrId] = useState(null);
    const [formData, setFormData] = useState({
        id: "",
        name: "", 
        location: "",
        capacity: 0,
        securityLevel: "",
        date: "",
        numOfCells: 0,
        isActive: ""
    });

    const fetchPrisons = async() => {
        try {
            const response = await fetch(`${import.meta.env.VITE_API_URL}/api/prison/getAllPrisons`)

            if (!response.ok) {
                throw new Error('Błąd pobierania danych');
            }

            const data = await response.json()
            setPrisons(data)
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchPrisons()
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
        e.preventDefault()
        if (!formData.name || !formData.location || !formData.capacity || !formData.securityLevel || !formData.numOfCells || !formData.isActive){
            alert("Fill all required fields!");
            return;
        }

        if (!isEdit) {
            await addPrison();
        } else {
            await editPrison();
        }

        setFormData({
            id: "",
            name: "", 
            location: "",
            capacity: 0,
            securityLevel: "",
            date: "",
            numOfCells: 0,
            isActive: ""
        })
    }

    const addPrison = async () => {
        try {
            const res = await fetch(`${import.meta.env.VITE_API_URL}/api/prison/addPrison`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData)
            });

            setModal(false);
            await fetchPrisons();
        } catch (error) {
            console.error('Error while adding a prison:', error);
            alert('There was an error while adding prison.');
        }
    }

    const editPrison = async () => {
        try {
            const res = await fetch(`${import.meta.env.VITE_API_URL}/api/prison/editPrison/${prId}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData)
            });

            setModal(false);
            await fetchPrisons();
        } catch (error) {
            console.error('Error while editing a prison:', error);
            alert('There was an error while editing prison.');
        }
    }
    
    const deletePrison = async (id, name) => {
        const confirmDelete = window.confirm(
        `Do you want to delete prison: ${name}`   
        );
        if (!confirmDelete) {
        return;
        }

        try {
            const response = await fetch(`${import.meta.env.VITE_API_URL}/api/prison/deletePrison/${id}`, {
                method: 'DELETE',
                headers: {
                'Content-Type': 'application/json',
                }
        });

        if (!response.ok) {
            throw new Error('Could not delete prison');
        }
        setPrisons(prisons.filter(prison => prison.id !== id));
        alert('Prison deleted succesfully');
        await fetchPrisons();
        } catch (error) {
            console.error('Error while deleteing prison:', error);
            alert('Error while deleting prison try again.');
        }

    }


return(
<>
    <div className="addButton-prisons">
        <button onClick={() => {setModal(true); setIsEdit(false)}}>Add Prison</button>
    </div>

    {modal && (
        <div className="modalPage-prisons">
            <form onSubmit={handleSubmit} className="addForm-prisons">
                <div className="form-container-prisons">
                    <label>Name: </label>
                    <input type="text" name="name" value={formData.name} onChange={handleChange}/>
                </div>
                <div className="form-container-prisons">
                    <label>Location: </label>
                    <input type="text" name="location" value={formData.location} onChange={handleChange}/>
                </div>
                <div className="form-container-prisons">
                    <label>Capacity: </label>
                    <input type="number" name="capacity" min="0" max="1000" value={formData.capacity} onChange={handleChange}/>
                </div>
                <div className="form-container-prisons">
                    <label>Security Level: </label>
                    <select name="securityLevel" value={formData.securityLevel} onChange={handleChange}>
                        <option value="">-- choose security level --</option>
                        <option value="high">High</option>
                        <option value="normal">Normal</option>
                        <option value="low">Low</option>
                    </select>
                </div>
                <div className="form-container-prisons">
                    <label>Number of cells: </label>
                    <input type="number" name="numOfCells" min="0" value={formData.numOfCells} onChange={handleChange}/>
                </div>
                <div className="form-container-prisons">
                    <label>Active: </label>
                    <select name="isActive" value={formData.isActive} onChange={handleChange}>
                        <option value="">-- choose if prison is active --</option>
                        <option value="false">False</option>
                        <option value="true">True</option>
                    </select>
                </div>
                <div className="formButtons-prisons">
                    <button onClick={() => {setModal(false); setFormData({
                        name: "",
                        location: "",
                        capacity: 0,
                        securityLevel: "",
                        numOfCells: 0,
                        isActive: ""
                    })}}>Cancel</button>
                    <button type="submit">Submit</button>
                </div>
            </form>
        </div>
    )}

    <div className="prisonList-prisons">
        {prisons.map((prison) => (
            <div key={prison.id} className="card-prisons">
                <h2>{prison.name}</h2>
                <div className="cardContent-prisons">
                    <div className="container-prisons">
                        <div className="label containerI-prisons">Location:</div>
                        <div className="containerI-prisons">{prison.location}</div>
                    </div>
                    <div className="container-prisons">
                        <div className="label containerI-prisons">Capacity:</div>
                        <div className="containerI-prisons">{prison.capacity}</div>
                    </div>
                    <div className="container-prisons">
                        <div className="label containerI-prisons">Security level:</div>
                        <div className="containerI-prisons">{prison.securityLevel}</div>
                    </div>
                    <div className="container-prisons">
                        <div className="label containerI-prisons">Number of cells:</div>
                        <div className="containerI-prisons">{prison.numOfCells}</div>
                    </div>
                    <div className="container-prisons">
                        <div className="label containerI-prisons">Active:</div>
                        <div className="containerI-prisons">{(prison.isActive) ? "true" : "false"}</div>
                    </div>
                    <div className="buttons-prisons">
                        <button onClick={() => {setModal(true); setIsEdit(true); setPrId(prison.id); setFormData({
                            name: prison.name,
                            location: prison.location,
                            capacity: prison.capacity,
                            securityLevel: prison.securityLevel,
                            numOfCells: prison.numOfCells,
                            isActive: prison.isActive
                        })}}><Edit2/></button>
                        <button onClick={() => deletePrison(prison.id, prison.name)}><Trash2/></button>
                    </div>
                </div>
            </div>
        ))}
    </div>
</>
)
}