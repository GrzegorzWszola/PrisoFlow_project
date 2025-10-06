function TestPage() {

    const callApi = async () => {
        try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/api/user/hello`);
        const db_res = await fetch(`${import.meta.env.VITE_API_URL}/api/db/health`);
        if (!res.ok) throw new Error("Błąd zapytania");
        if (!db_res) throw new Error("Błąd zapytania");
        const data = await res.text();   // <-- zamiast .json()
        const db_data = await db_res.text();
        console.log("Odpowiedź backendu:", data);
        console.log("Odpowiedź bazy danych:", db_data);
        } catch (err) {
        console.error("❌ Błąd połączenia z backendem:", err);
        }
    };

    return (
        <>
            <div className="TestPage">
                <button className="TestPage_ApiButton" onClick={callApi}>Api Check</button>
            </div>
        </> 
    )
}

export default TestPage