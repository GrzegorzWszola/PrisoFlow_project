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
 * Calls endpoint `/api/user/login` and returns the response
 *
 * @async
 * @function    login
 * @param       {string} username - Username
 * @param       {string} password - Password
 * @returns     {Promise<object>} Response from server
 * @throws      {Error} If the connection fails
 */
export const login = async (username, password) => {
    try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/api/user/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const response = await res.json(); 
        if (!res.ok) {
            throw new Error(response.error || 'Login failed');
        }
        
        console.log("Database response:", response);
        return response;
    } catch (err) {
        console.error("Error while connecting to the backend", err);
        throw err;
    }
};