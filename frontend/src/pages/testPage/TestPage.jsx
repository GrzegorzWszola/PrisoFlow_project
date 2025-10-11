import { testConnection, testDbConnection, testCreateTable, testCreatePrRecord, testReadPrRecord, testDropRecord, testDropTable} from "../../api/apiTests"

function TestPage() {
    return (
        <>
            <div className="TestPage">
                <button className="TestPage_ApiButton" onClick={testConnection}>Api Check</button>
                <button className="TestPage_DBButton" onClick={testDbConnection}>DB check</button>
                <button onClick={testCreateTable}>Create Table</button>
                <button onClick={testCreatePrRecord}>Create Prison Record</button>
                <button onClick={testReadPrRecord}>Read Prison Record</button>
                <button onClick={testDropRecord}>Drop Record</button>
                <button onClick={testDropTable}>Drop Table</button>
            </div>
        </> 
    )
}

export default TestPage