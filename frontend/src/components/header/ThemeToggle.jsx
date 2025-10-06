import { useEffect, useState } from "react";

import { Moon, Beef } from "lucide-react";

export default function ThemeToggle() {
  const [theme, setTheme] = useState(() =>
    window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light"
  );

  useEffect(() => {
    document.documentElement.classList.remove("light", "dark");
    document.documentElement.classList.add(theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(theme === "dark" ? "light" : "dark");
  };

  return (
<button
    onClick={toggleTheme}
    style={{
    minWidth: "120px",
    display: "flex",
    alignItems: "center",
    gap: "0.5em",
    justifyContent: "center", // kolor z CSS
    }}
    >
        {theme === "dark" ? <Moon size={20} /> : <Beef size={20} />}
        {theme === "dark" ? "Dark" : "Grim"}
</button>
  );
}
