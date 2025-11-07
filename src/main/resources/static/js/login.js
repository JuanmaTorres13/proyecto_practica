// =====================================================
// ================ INICIALIZACIÓN =====================
// =====================================================

/**
 * Inicializa el comportamiento del login y registro
 * al cargar completamente el DOM.
 */
document.addEventListener("DOMContentLoaded", function () {
    /** @type {HTMLElement} Contenedor principal del panel derecho (login / registro) */
    const loginRight = document.querySelector(".login-right");

    /** @type {string} Contenido original del login para restaurarlo luego */
    const originalHTML = loginRight.innerHTML;

    // Asigna los eventos iniciales del login
    attachLoginEvents(loginRight, originalHTML);
});


// =====================================================
// =================== UTILIDADES ======================
// =====================================================

/**
 * Muestra un mensaje temporal (error o éxito) sobre el formulario.
 * @param {HTMLElement} container - Contenedor donde se mostrará el mensaje.
 * @param {"error" | "success"} type - Tipo de mensaje a mostrar.
 * @param {string} text - Texto del mensaje.
 */
function showMessage(container, type, text) {
    /** @type {HTMLParagraphElement} */
    const msg = document.createElement("p");
    msg.className = type === "error" ? "error show" : "success show";
    msg.textContent = text;

    /** @type {HTMLFormElement | null} */
    const form = container.querySelector("form");
    if (form) {
        form.parentNode.insertBefore(msg, form);
        setTimeout(() => msg.remove(), 4000);
    }
}

/**
 * Aplica una animación de salida (fade-out) antes de ejecutar un callback.
 * @param {HTMLElement} element - Elemento al que se le aplica la animación.
 * @param {Function} callback - Función a ejecutar después de la animación.
 */
function fadeOut(element, callback) {
    element.classList.add("fade-out");
    setTimeout(() => {
        callback();
        element.classList.remove("fade-out");
        element.classList.add("fade-in");
    }, 300);
}


// =====================================================
// =================== FORMULARIOS =====================
// =====================================================

/**
 * Renderiza el formulario de registro dentro del contenedor.
 * @param {HTMLElement} container - Contenedor principal donde se renderiza el formulario.
 * @param {string} originalHTML - Contenido original del login para volver atrás.
 */
function renderRegisterForm(container, originalHTML) {
    container.innerHTML = `
        <div class="login-container register-container fade-in">
            <h2>Crear cuenta</h2>
            <p class="login-subtitle">Regístrate para descubrir los mejores eventos</p>

            <form id="registerForm">
                <div class="form-group">
                    <label for="nombre">Nombre completo</label>
                    <div class="input-wrapper">
                        <span class="input-icon">👤</span>
                        <input type="text" id="nombre" name="nombre" placeholder="Tu nombre" required>
                    </div>
                </div>

                <div class="form-group">
                    <label for="emailRegistro">Correo electrónico</label>
                    <div class="input-wrapper">
                        <span class="input-icon">📧</span>
                        <input type="email" id="emailRegistro" name="email" placeholder="tu@email.com" required>
                    </div>
                </div>

                <div class="form-group">
                    <label for="passwordRegistro">Contraseña</label>
                    <div class="input-wrapper">
                        <span class="input-icon">🔒</span>
                        <input type="password" id="passwordRegistro" name="password" placeholder="••••••••" required>
                    </div>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirmar contraseña</label>
                    <div class="input-wrapper">
                        <span class="input-icon">🔒</span>
                        <input type="password" id="confirmPassword" name="confirmPassword" placeholder="••••••••" required>
                    </div>
                </div>

                <button type="submit" class="submit-btn">Registrarse</button>
            </form>

            <p class="register-link">
                ¿Ya tienes una cuenta? <a href="#" id="backToLogin">Inicia sesión aquí</a>
            </p>
        </div>
    `;

    attachRegisterEvents(container, originalHTML);
}

/**
 * Renderiza nuevamente el formulario de login.
 * @param {HTMLElement} container - Contenedor principal donde se renderiza el login.
 * @param {string} originalHTML - Contenido HTML original del login.
 */
function renderLoginForm(container, originalHTML) {
    container.innerHTML = originalHTML;
    attachLoginEvents(container, originalHTML);
}


// =====================================================
// ===================== EVENTOS =======================
// =====================================================

/**
 * Asigna los eventos al formulario de login.
 * @param {HTMLElement} container - Contenedor principal del login.
 * @param {string} originalHTML - Contenido original del login.
 */
function attachLoginEvents(container, originalHTML) {
    /** @type {HTMLAnchorElement | null} */
    const registerLink = container.querySelector("#registerLink");

    if (registerLink) {
        registerLink.addEventListener("click", (e) => {
            e.preventDefault();
            fadeOut(container, () => renderRegisterForm(container, originalHTML));
        });
    }
}

/**
 * Asigna los eventos al formulario de registro.
 * @param {HTMLElement} container - Contenedor principal del registro.
 * @param {string} originalHTML - Contenido original del login.
 */
function attachRegisterEvents(container, originalHTML) {
    /** @type {HTMLAnchorElement} */
    const backToLogin = container.querySelector("#backToLogin");

    /** @type {HTMLFormElement} */
    const registerForm = container.querySelector("#registerForm");

    backToLogin.addEventListener("click", (e) => {
        e.preventDefault();
        fadeOut(container, () => renderLoginForm(container, originalHTML));
    });

    registerForm.addEventListener("submit", (e) => {
        e.preventDefault();

        /** @type {string} */
        const nombre = container.querySelector("#nombre").value.trim();
        /** @type {string} */
        const email = container.querySelector("#emailRegistro").value.trim();
        /** @type {string} */
        const pass = container.querySelector("#passwordRegistro").value;
        /** @type {string} */
        const confirm = container.querySelector("#confirmPassword").value;

        if (!nombre || !email || !pass || !confirm) {
            showMessage(container, "error", "Por favor, completa todos los campos.");
            return;
        }

        if (pass.length < 6) {
            showMessage(container, "error", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (pass !== confirm) {
            showMessage(container, "error", "Las contraseñas no coinciden.");
            return;
        }

        showMessage(container, "success", "¡Registro exitoso! Ahora puedes iniciar sesión.");

        // Redirige de nuevo al login luego de 2 segundos
        setTimeout(() => backToLogin.click(), 2000);
    });
}
