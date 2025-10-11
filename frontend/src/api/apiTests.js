/**
 * Calls endpoint `/api/user/hello` and returnes the text confiramtion
 *
 * @async
 * @function    testConnection
 * @returns     {Promise<string>}   Response from server
 * @throws      {Error}             If the connection fails
 */
export const testConnection = async () => {
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/user/hello`);

    if (!res.ok) throw new Error("Call error");
    const data = await res.text(); 
    console.log("Backend response:", data);
    } catch (err) {
    console.error("❌ Error while connecting to the backend:", err);
    }
};

/**
 * Calls endpoint `/api/db/health` and returnes the text confiramtion
 *
 * @async
 * @function    testDbConnection
 * @returns     {Promise<string>}   Response from server
 * @throws      {Error}             If the connection fails
 */
export const testDbConnection = async () => {
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/db/health`);

    if (!res.ok) throw new Error("Call error");
    const data = await res.text(); 
    console.log("Database response:", data);
    } catch (err) {
    console.error("❌ Error while connecting to the database:", err);
    }
};

/**
 * Calls endpoint `/api/db/createTable` and returnes the text confiramtion
 *
 * @async
 * @function    testCreateTable
 * @returns     {Promise<string>}   Response from server
 * @throws      {Error}             If the connection fails
 */
export const testCreateTable = async () => {
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/db/createTable`);

    if (!res.ok) throw new Error("Call error");
    const data = await res.text(); 
    console.log("Database response:", data);
    } catch (err) {
    console.error("❌ Error while connecting to the database:", err);
    }
};

/**
 * Calls endpoint `/api/db/testCreatePrRecord` and returnes the text confiramtion
 *
 * @async
 * @function    testCreatePrRecord
 * @returns     {Promise<string>}   Response from server
 * @throws      {Error}             If the connection fails
 */
export const testCreatePrRecord = async () => {
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/db/testCreatePrRecord`);

    if (!res.ok) throw new Error("Call error");
    const data = await res.text(); 
    console.log("Database response:", data);
    } catch (err) {
    console.error("❌ Error while connecting to the database:", err);
    }
};

/**
 * Calls endpoint `/api/db/testReadPrRecord` and returnes the text confiramtion
 *
 * @async
 * @function    testReadPrRecord
 * @returns     {Promise<string>}   Response from server
 * @throws      {Error}             If the connection fails
 */
export const testReadPrRecord = async () => {
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/db/testReadPrRecord`);

    if (!res.ok) throw new Error("Call error");
    const data = await res.json(); 
    console.log("Database response:", data);
    } catch (err) {
    console.error("❌ Error while connecting to the database:", err);
    }
};

/**
 * Calls endpoint `/api/db/testDropRecord` and returnes the text confiramtion
 *
 * @async
 * @function    testDropRecord
 * @returns     {Promise<string>}   Response from server
 * @throws      {Error}             If the connection fails
 */
export const testDropRecord = async () => {
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/db/testDropRecord`);

    if (!res.ok) throw new Error("Call error");
    const data = await res.text(); 
    console.log("Database response:", data);
    } catch (err) {
    console.error("❌ Error while connecting to the database:", err);
    }
};

/**
 * Calls endpoint `/api/db/testDropTable` and returnes the text confiramtion
 *
 * @async
 * @function    testDropTable
 * @returns     {Promise<string>}   Response from server
 * @throws      {Error}             If the connection fails
 */
export const testDropTable = async () => {
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/db/testDropTable`);

    if (!res.ok) throw new Error("Call error");
    const data = await res.text(); 
    console.log("Database response:", data);
    } catch (err) {
    console.error("❌ Error while connecting to the database:", err);
    }
};