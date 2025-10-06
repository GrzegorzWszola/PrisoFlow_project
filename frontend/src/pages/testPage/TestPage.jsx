import { testConnection, testDbConnection } from "../../api/api"

function TestPage() {
    return (
        <>
            <div className="TestPage">
                <button className="TestPage_ApiButton" onClick={testConnection}>Api Check</button>
                <button className="TestPage_DBButton" onClick={testDbConnection}>DB check</button>
            </div>
        </> 
    )
}

export default TestPage