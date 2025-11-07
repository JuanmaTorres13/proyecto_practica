const loginForm = document.getElementById("loginForm");
const mensaje = document.getElementById("mensaje");

// Login
loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const response = await fetch("/usuarios/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
            credentials: "include" // 🔑 enviar cookies al backend
        });

        if (response.ok) {
            // ✅ Login correcto → redirigir a la página de perfil
            window.location.href = "/usuarios/profile";
        } else {
            // ❌ Error en login → mostrar mensaje
            const text = await response.text();
            mensaje.textContent = "Error al iniciar sesión: " + text;
        }
    } catch (error) {
        console.error("Error:", error);
        mensaje.textContent = "Error al conectar con el servidor.";
    }
});
