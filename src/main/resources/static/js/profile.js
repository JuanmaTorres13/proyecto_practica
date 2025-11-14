'use strict';

/**
 * ======================================
 * PERFIL DE USUARIO - EVENTZONE
 * Controla la carga, edición y actualización del perfil de usuario,
 * la navegación entre pestañas, el manejo de favoritos y la sesión.
 * ======================================
 */

/* ====== VARIABLES GLOBALES ====== */
const menuItems = document.querySelectorAll('.menu-item');
const tabContents = document.querySelectorAll('.tab-content');
const editBtn = document.getElementById('editBtn');
const saveBtn = document.getElementById('saveBtn');
const cancelBtn = document.getElementById('cancelBtn');
const editActions = document.getElementById('editActions');

const nombreInput = document.getElementById('nombre');
const emailInput = document.getElementById('email');
const telefonoInput = document.getElementById('telefono');
const ciudadInput = document.getElementById('ciudad');
const fechaNacimientoInput = document.getElementById('fechaNacimiento');
const bioTextarea = document.getElementById('bio');

let originalData = {};

/* ====== INICIALIZACIÓN ====== */
window.addEventListener('DOMContentLoaded', () => {
    navTab();
    editarPerfil();
    guardarCambios();
    cancelarEdit();
    favoritos();
    logout();
});

cargarPerfil();

/* =========================================================
   CARGAR PERFIL DESDE EL SERVIDOR
   ========================================================= */
/**
 * Obtiene los datos del perfil del usuario autenticado y los muestra en el formulario.
 * @async
 * @function cargarPerfil
 */
async function cargarPerfil() {
    try {
        const res = await fetch("/usuarios/me", {
            method: "GET",
            credentials: "include"
        });

        if (!res.ok) throw new Error("No autenticado");

        const data = await res.json();

        // Cargar datos en los campos
        nombreInput.value = data.nombre;
        emailInput.value = data.email;
        telefonoInput.value = data.telefono || '';
        ciudadInput.value = data.ciudad || '';
        bioTextarea.value = data.bio || '';
        fechaNacimientoInput.value = data.fechaNacimiento || '';

        // Actualizar barra lateral
        document.getElementById('userName').textContent = data.nombre;
        document.getElementById('userEmail').textContent = data.email;
        const initials = data.nombre.split(' ').map(n => n[0]).join('').toUpperCase();
        document.getElementById('userAvatar').textContent = initials;

    } catch (error) {
        console.error(error);
        window.location.href = "/usuarios/login";
    }
}

/* =========================================================
   NAVEGACIÓN ENTRE PESTAÑAS
   ========================================================= */
/**
 * Controla la navegación entre las diferentes secciones del perfil.
 * @function navTab
 */
function navTab() {
    menuItems.forEach(item => {
        item.addEventListener('click', () => {
            menuItems.forEach(mi => mi.classList.remove('active'));
            item.classList.add('active');

            const tabName = item.getAttribute('data-tab');
            tabContents.forEach(tc => tc.classList.remove('active'));

            const selectedTab = document.getElementById(tabName + 'Tab');
            if (selectedTab) selectedTab.classList.add('active');
        });
    });
}

/* =========================================================
   EDICIÓN DE PERFIL
   ========================================================= */
/**
 * Habilita los campos del formulario para su edición.
 * @function editarPerfil
 */
function editarPerfil() {
    editBtn.addEventListener('click', () => {
        originalData = {
            nombre: nombreInput.value,
            email: emailInput.value,
            telefono: telefonoInput.value,
            ciudad: ciudadInput.value,
            fechaNacimiento: fechaNacimientoInput.value,
            bio: bioTextarea.value
        };

        [nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea]
            .forEach(el => el.removeAttribute('readonly'));

        nombreInput.focus();
        editBtn.style.display = 'none';
        editActions.style.display = 'flex';
    });
}

/* =========================================================
   GUARDAR CAMBIOS DEL PERFIL
   ========================================================= */
/**
 * Envía los datos actualizados del perfil al servidor y muestra una notificación con SweetAlert2.
 * @async
 * @function guardarCambios
 */
function guardarCambios() {
    saveBtn.addEventListener('click', async () => {
        const payload = {
            nombre: nombreInput.value,
            email: emailInput.value,
            telefono: telefonoInput.value,
            ciudad: ciudadInput.value,
            bio: bioTextarea.value,
            fechaNacimiento: fechaNacimientoInput.value
        };

        try {
            const res = await fetch("/usuarios/me", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify(payload)
            });

            if (!res.ok) throw new Error("Error al actualizar perfil");

            // Actualizar vista
            document.getElementById('userName').textContent = nombreInput.value;
            document.getElementById('userEmail').textContent = emailInput.value;
            const initials = nombreInput.value.split(' ').map(n => n[0]).join('').toUpperCase();
            document.getElementById('userAvatar').textContent = initials;

            // Restaurar interfaz
            [nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea]
                .forEach(el => el.setAttribute('readonly', true));
            editBtn.style.display = 'flex';
            editActions.style.display = 'none';

            // Alerta de éxito
            Swal.fire({
                icon: 'success',
                title: '¡Perfil actualizado!',
                showConfirmButton: false,
                timer: 2000
            });

        } catch (error) {
            console.error(error);
            Swal.fire({
                icon: 'error',
                title: 'Error al guardar cambios',
                text: error.message
            });
        }
    });
}

/* =========================================================
   CANCELAR EDICIÓN
   ========================================================= */
/**
 * Restaura los valores originales del perfil y bloquea los campos nuevamente.
 * @function cancelarEdit
 */
function cancelarEdit() {
    cancelBtn.addEventListener('click', () => {
        nombreInput.value = originalData.nombre;
        emailInput.value = originalData.email;
        telefonoInput.value = originalData.telefono;
        ciudadInput.value = originalData.ciudad;
        fechaNacimientoInput.value = originalData.fechaNacimiento;
        bioTextarea.value = originalData.bio;

        [nombreInput, emailInput, telefonoInput, ciudadInput, fechaNacimientoInput, bioTextarea]
            .forEach(el => el.setAttribute('readonly', true));

        editBtn.style.display = 'flex';
        editActions.style.display = 'none';

        Swal.fire({
            icon: 'info',
            title: 'Edición cancelada',
            showConfirmButton: false,
            timer: 1500
        });
    });
}

/* =========================================================
   FAVORITOS
   ========================================================= */
/**
 * Permite eliminar eventos de la lista de favoritos con confirmación.
 * @function favoritos
 */
function favoritos() {
    document.querySelectorAll('.btn-remove').forEach(btn => {
        btn.addEventListener('click', function() {
            Swal.fire({
                title: '¿Desea eliminar este evento de favoritos?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Sí, eliminar',
                cancelButtonText: 'Cancelar'
            }).then((result) => {
                if (result.isConfirmed) {
                    const card = this.closest('.favorite-card');
                    card.style.opacity = '0';
                    card.style.transform = 'scale(0.9)';
                    card.style.transition = 'all 0.3s';
                    setTimeout(() => {
                        card.remove();
                        const favCount = document.getElementById('favoritesCount');
                        favCount.textContent = parseInt(favCount.textContent) - 1;
                    }, 300);

                    Swal.fire({
                        icon: 'success',
                        title: 'Evento eliminado',
                        showConfirmButton: false,
                        timer: 1500
                    });
                }
            });
        });
    });
}

/* =========================================================
   CERRAR SESIÓN
   ========================================================= */
/**
 * Muestra confirmación antes de cerrar sesión.
 * @function logout
 */
function logout() {
    document.querySelector('.logout-btn').addEventListener('click', () => {
        Swal.fire({
            title: '¿Desea cerrar sesión?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, salir',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = '/usuarios/login';
            }
        });
    });
}

/* =========================================================
   ANIMACIONES DE ENTRADA
   ========================================================= */
/**
 * Añade una animación de entrada para las tarjetas de entradas y favoritos.
 * @function animaciones
 */
window.addEventListener('load', () => {
    const cards = document.querySelectorAll('.ticket-card, .favorite-card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s ease';

        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 50 + (index * 100));
    });
});
