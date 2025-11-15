import { useState, useEffect } from "react";
import "../Dashboard/Dashboard.css"

export const Dashboard = () => {
    const [wiezienia, setWiezienia] = useState([]);
    const [visits, setVisits] = useState([]);
    const [incidents, setIncidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [dataPobrania, setDataPobrania] = useState(null);

    useEffect(() => {
        const fetchDashboard = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_API_URL}/api/db/dashboard`);
            
            if (!response.ok) {
            throw new Error('Błąd pobierania danych');
            }
            
            const data = await response.json();
            setWiezienia(data.prisons)
            setVisits(data.visits)
            setIncidents(data.incidents)
            setDataPobrania(new Date())
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
        };

        fetchDashboard();
    }, []);

    const getProgressColor = (percentage) => {
        if (percentage >= 100) return "bg-red-500";
        if (percentage >= 80) return "bg-orange-500";
        if (percentage >= 60) return "bg-yellow-500";
        return "bg-green-500";
    };

    const getTextColor = (percentage) => {
        if (percentage >= 100) return "text-red-600";
        if (percentage >= 80) return "text-orange-600";
        if (percentage >= 60) return "text-yellow-600";
        return "text-green-600";
    };

    if (loading) {
        return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-xl">Ładowanie...</div>
        </div>
        )
    }
    if (error) return <div>Błąd: {error}</div>;

    function getSeverityColor(severity) {
        switch (severity.toLowerCase()) {
            case "high":
            return "red";
            case "critical":
            return "darkred"; // burgundowy
            case "normal":
            return "black";
            case "low":
            return "green";
            default:
            return "gray";
        }
    }

    return(
    <>
        <div className="dashboard">
            <div className="container">
                <h1 className="title">Prisons</h1>
                
                <div className="prison-list">
                {wiezienia.map((wiezienie) => (
                    <div key={wiezienie.id} className="prison-card">
                    <div className="card-header">
                        <div>
                        <h3 className="prison-name">{wiezienie.name}</h3>
                        <p className="prison-location">📍 {wiezienie.location}</p>
                        </div>
                        <span className={`occupancy ${getTextColor(wiezienie.occupancyPercentage)}`}>
                        {wiezienie.occupancyPercentage}%
                        </span>
                    </div>

                    {/* Pasek postępu */}
                    <div className="progress-section">
                        <div className="progress-info">
                        <span>Occupancy:</span>
                        <span>{wiezienie.currentInmates} / {wiezienie.capacity}</span>
                        </div>
                        <div className="progress-bar">
                        <div
                            className={`progress-fill ${getProgressColor(wiezienie.occupancyPercentage)}`}
                            style={{ width: `${wiezienie.occupancyPercentage}%` }}
                        >
                            {wiezienie.occupancyPercentage >= 20 && (
                            <span className="progress-text">{wiezienie.occupancyPercentage}%</span>
                            )}
                        </div>
                        </div>
                    </div>

                    {/* Dodatkowe info */}
                    <div className="details">
                        <div>
                        <span className="label">Capacity:</span> {wiezienie.capacity}
                        </div>
                        <div>
                        <span className="label">Inmates:</span> {wiezienie.currentInmates}
                        </div>
                        {wiezienie.occupancyPercentage > 100 && (
                        <span className="overcapacity">⚠️ Overfilled</span>
                        )}
                    </div>
                    </div>
                ))}
                </div>
            </div>
            
            <div className="upcomingVisits">
                <h1 className="title">Visits</h1>
                <div className="visits-list">
                    {visits.map((visit) => (
                        <div key={visit.visit_datetime} className="prison-card">
                            <div className="card-header">
                                <h3>Inmate: {visit.criminal_first_name} {visit.criminal_last_name}</h3>
                                <h3>Visitor: {visit.visitor_first_name} {visit.visitor_last_name}</h3>
                            </div>
                            <div className="details">
                                <span className="label">Prison: </span>{visit.prison_name}
                                <span className="label">Realtionship: </span>{visit.relationship}
                                <span className="lable">Date: </span>{new Date(visit.visit_datetime).toLocaleString()}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            <div className="latestIncidents">
                <h1 className="title">Latest incidents</h1>
                <div className="incident-list">
                    {incidents.map((incident) => (
                        <div key={incident.incident_id} className="prison-card">
                                <h2>
                                    Severity:{" "}
                                    <span
                                    style={{
                                        color: getSeverityColor(incident.severity),
                                        textTransform: "capitalize",
                                    }}
                                    >
                                    {incident.severity}
                                    </span>
                                </h2>
                            <div className="card-header">
                                <h3>Inmate: {incident.criminal_first_name} {incident.criminal_last_name}</h3>
                                <h3>Officer: {incident.officer_first_name} {incident.officer_last_name}</h3>
                            </div>
                            <div style={{padding: ".5rem"}}>{incident.descryption}</div>
                            <div className="details">
                                <span className="label">Prison: </span>{incident.prison_name}
                                <span className="label">Incident Type: </span> {incident.incident_type}
                                <span className="lable">Date: </span>{new Date(incident.incident_datetime).toLocaleString()}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    </>
    );
}