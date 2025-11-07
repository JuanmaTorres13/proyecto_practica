/**
 * ---------- INICIALIZACIÓN ----------
 */
document.addEventListener("DOMContentLoaded", function () {
  const loginRight = document.querySelector(".login-right");
  const originalHTML = loginRight.innerHTML;

  initLogin(loginRight, originalHTML);
});

/**
 * ---------- UTILIDADES ----------
 */

/**
 * Muestra un mensaje temporal de error o éxito sobre el formulario.
 * @param {HTMLElement} container - Contenedor donde se mostrará el mensaje.
 * @param {"error"|"success"} type - Tipo de mensaje (color/estilo).
 * @param {string} text - Texto del mensaje.
 */
function showMessage(container, type, text) {
  const msg = document.createElement("p");
  msg.className = type === "error" ? "error show" : "success show";
  msg.textContent = text;

  const form = container.querySelector("form");
  form.parentNode.insertBefore(msg, form);
  setTimeout(() => msg.remove(), 4000);
}

/**
 * Efecto de desvanecimiento antes de cambiar de formulario.
 * @param {HTMLElement} element
 * @param {Function} callback
 */
function fadeOut(element, callback) {
  element.classList.add("fade-out");
  setTimeout(() => {
    callback();
    element.classList.remove("fade-out");
    element.classList.add("fade-in");
  }, 300);
}

/**
 * Valida el formato de email.
 * Debe tener al menos un carácter antes del @, un dominio y un TLD válido.
 * @param {string} email
 * @returns {boolean}
 */
function validarEmail(email) {
  const patron = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
  return patron.test(email);
}

/**
 * ---------- LOGIN ----------
 */

/**
 * Inicializa el formulario de login.
 * @param {HTMLElement} container
 * @param {string} originalHTML
 */
function initLogin(container, originalHTML) {
  attachLoginEvents(container, originalHTML);
  attachRegisterLink(container, originalHTML);
}

/**
 * Enlace para cambiar al registro.
 */
function attachRegisterLink(container, originalHTML) {
  const registerLink = container.querySelector("#registerLink");
  if (!registerLink) return;

  registerLink.addEventListener("click", (e) => {
    e.preventDefault();
    fadeOut(container, () => renderRegisterForm(container, originalHTML));
  });
}

/**
 * Eventos del formulario de login y envío al backend.
 */
function attachLoginEvents(container, originalHTML) {
  const loginForm = container.querySelector("#loginForm");
  if (!loginForm) return;

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = container.querySelector("#email").value.trim();
    const password = container.querySelector("#password").value;

    // Validaciones en front
    if (!email) {
      showMessage(container, "error", "Por favor ingresa tu email.");
      return;
    }

    if (!validarEmail(email)) {
      showMessage(container, "error", "Formato de email no válido. Ejemplo: usuario@eventzone.com");
      return;
    }

    if (!password) {
      showMessage(container, "error", "Por favor ingresa tu contraseña.");
      return;
    }

    try {
      const response = await fetch("/usuarios/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        showMessage(container, "success", "Inicio de sesión exitoso. Redirigiendo...");
        setTimeout(() => window.location.href = "/usuarios/profile", 1500);
        return;
      }

      // Manejo de errores según backend
      const text = await response.text();
      showMessage(container, "error", text || "Credenciales incorrectas.");

    } catch (error) {
      console.error("Error al iniciar sesión:", error);
      showMessage(container, "error", "Error de conexión con el servidor.");
    }
  });
}

/**
 * ---------- REGISTRO ----------
 */

/**
 * Renderiza formulario de registro.
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
            <input type="text" id="nombre" placeholder="Tu nombre" required>
          </div>
        </div>

        <div class="form-group">
          <label for="emailRegistro">Correo electrónico</label>
          <div class="input-wrapper">
            <span class="input-icon">📧</span>
            <input type="email" id="emailRegistro" placeholder="tu@email.com" required>
          </div>
        </div>

        <div class="form-group">
          <label for="passwordRegistro">Contraseña</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input type="password" id="passwordRegistro" placeholder="••••••••" required>
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirmar contraseña</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input type="password" id="confirmPassword" placeholder="••••••••" required>
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
 * Eventos de registro y envío al backend.
 */
function attachRegisterEvents(container, originalHTML) {
  const backToLogin = container.querySelector("#backToLogin");
  backToLogin.addEventListener("click", (e) => {
    e.preventDefault();
    fadeOut(container, () => renderLoginForm(container, originalHTML));
  });

  const registerForm = container.querySelector("#registerForm");
  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const nombre = container.querySelector("#nombre").value.trim();
    const email = container.querySelector("#emailRegistro").value.trim();
    const pass = container.querySelector("#passwordRegistro").value;
    const confirm = container.querySelector("#confirmPassword").value;

    if (!nombre || !email || !pass || !confirm) {
      showMessage(container, "error", "Por favor, completa todos los campos.");
      return;
    }

    if (!validarEmail(email)) {
      showMessage(container, "error", "Formato de email no válido. Ejemplo: usuario@eventzone.com");
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

    try {
      const response = await fetch("/usuarios/registro", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nombre, email, password: pass }),
      });

      const text = await response.text();

      if (response.ok) {
        showMessage(container, "success", "¡Registro exitoso! Ahora puedes iniciar sesión.");
        setTimeout(() => backToLogin.click(), 2000);
      } else {
        showMessage(container, "error", text || "Error al registrar el usuario.");
      }
    } catch (error) {
      console.error("Error al registrar:", error);
      showMessage(container, "error", "Error de conexión con el servidor.");
    }
  });
}

/**
 * Restaura el formulario de login.
 */
function renderLoginForm(container, originalHTML) {
  container.innerHTML = originalHTML;
  initLogin(container, originalHTML);
}
