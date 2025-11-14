 /**
 * ---------- INICIALIZACIÓN ----------
 */
document.addEventListener("DOMContentLoaded", () => {
  const loginRight = document.querySelector(".login-right");
  const originalHTML = loginRight.innerHTML;

  initLogin(loginRight, originalHTML);
});

/**
 * ---------- UTILIDADES ----------
 */

/**
 * Muestra un mensaje temporal de error o éxito sobre el formulario.
 */
function showMessage(container, type, text) {
  const oldMsg = container.querySelector(".temp-msg");
  if (oldMsg) oldMsg.remove();

  const msg = document.createElement("p");
  msg.className = `temp-msg ${type === "error" ? "error show" : "success show"}`;
  msg.textContent = text;

  const form = container.querySelector("form");
  form.parentNode.insertBefore(msg, form);
  setTimeout(() => msg.remove(), 4000);
}

/**
 * Crea un efecto de desvanecimiento antes de cambiar de formulario.
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
 * Valida si el email tiene formato correcto.
 */
function validarEmail(email) {
  // Requiere texto antes y después del @, y un dominio válido
  const patron = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return patron.test(email);
}

/**
 * ---------- LOGIN ----------
 */
function initLogin(container, originalHTML) {
  attachLoginEvents(container, originalHTML);
  attachRegisterLink(container, originalHTML);
}

/**
 * Asigna evento al enlace “Regístrate”.
 */
function attachRegisterLink(container, originalHTML) {
  const registerLink = container.querySelector("#registerLink");
  if (registerLink) {
    registerLink.addEventListener("click", (e) => {
      e.preventDefault();
      fadeOut(container, () => renderRegisterForm(container, originalHTML));
    });
  }
}

/**
 * Asigna el evento de login, usando el botón o el submit.
 */
function attachLoginEvents(container, originalHTML) {
  const loginBtn = container.querySelector(".submit-btn");
  const loginForm = container.querySelector("#loginForm");

  if (!loginForm || !loginBtn) return;

  // Escuchar click del botón (en lugar de depender solo del submit)
  loginBtn.addEventListener("click", async (e) => {
    e.preventDefault();
    await handleLogin(container);
  });

  // También permitir enter (submit normal)
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    await handleLogin(container);
  });
}

/**
 * Lógica de login centralizada.
 */
async function handleLogin(container) {
  const email = container.querySelector("#email").value.trim();
  const password = container.querySelector("#password").value;

  if (!email || !password) {
    showMessage(container, "error", "Completa todos los campos.");
    return;
  }

  // Validación estricta de formato de email
  if (!validarEmail(email)) {
    showMessage(
      container,
      "error",
      "Formato de correo no válido. Ejemplo: usuario@eventzone.com"
    );
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
	    const data = await response.json(); 
	    showMessage(container, "success", "Inicio de sesión exitoso. Redirigiendo...");

	    setTimeout(() => {
	        if (data.rol === "ADMIN") {
	            window.location.href = "/admin/panel"; // página del admin
	        } else {
	            window.location.href = "/usuarios/profile"; // página normal
	        }
	    }, 1500);
	    return;
	}

    // Muestra mensajes específicos según el código
    if (response.status === 404) {
      showMessage(container, "error", "Usuario no registrado.");
    } else if (response.status === 401) {
      showMessage(container, "error", "Contraseña incorrecta.");
    } else {
      const text = await response.text();
      showMessage(container, "error", text || "Error al iniciar sesión.");
    }
  } catch (error) {
    console.error("Error al iniciar sesión:", error);
    showMessage(container, "error", "Error de conexión con el servidor.");
  }
}

/**
 * ---------- REGISTRO ----------
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

        <button id="registerBtn" type="submit" class="submit-btn">Registrarse</button>
      </form>

      <p class="register-link">
        ¿Ya tienes una cuenta? <a href="#" id="backToLogin">Inicia sesión aquí</a>
      </p>
    </div>
  `;

  attachRegisterEvents(container, originalHTML);
}

/**
 * Eventos del formulario de registro.
 */
function attachRegisterEvents(container, originalHTML) {
  const backToLogin = container.querySelector("#backToLogin");
  const registerBtn = container.querySelector("#registerBtn");

  backToLogin.addEventListener("click", (e) => {
    e.preventDefault();
    fadeOut(container, () => renderLoginForm(container, originalHTML));
  });

  registerBtn.addEventListener("click", async (e) => {
    e.preventDefault();
    await handleRegister(container, originalHTML);
  });
}

/**
 * Lógica del registro centralizada.
 */
async function handleRegister(container, originalHTML) {
  const nombre = container.querySelector("#nombre").value.trim();
  const email = container.querySelector("#emailRegistro").value.trim();
  const pass = container.querySelector("#passwordRegistro").value;
  const confirm = container.querySelector("#confirmPassword").value;

  if (!nombre || !email || !pass || !confirm) {
    showMessage(container, "error", "Por favor, completa todos los campos.");
    return;
  }

  if (!validarEmail(email)) {
    showMessage(container, "error", "Formato de correo no válido.");
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
      setTimeout(() => fadeOut(container, () => renderLoginForm(container, originalHTML)), 2000);
    } else {
      showMessage(container, "error", text || "Error al registrar el usuario.");
    }
  } catch (error) {
    console.error("Error al registrar:", error);
    showMessage(container, "error", "Error de conexión con el servidor.");
  }
}

/**
 * ---------- RESTAURAR LOGIN ----------
 */
function renderLoginForm(container, originalHTML) {
  container.innerHTML = originalHTML;
  initLogin(container, originalHTML);
}

