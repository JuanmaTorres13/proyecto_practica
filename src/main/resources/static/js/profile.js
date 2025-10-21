// Elementos del DOM
const menuItems = document.querySelectorAll('.menu-item');
const tabContents = document.querySelectorAll('.tab-content');
const editBtn = document.getElementById('editBtn');
const saveBtn = document.getElementById('saveBtn');
const cancelBtn = document.getElementById('cancelBtn');
const editActions = document.getElementById('editActions');

// Campos del formulario
const nombreInput = document.getElementById('nombre');
const emailInput = document.getElementById('email');
const telefonoInput = document.getElementById('telefono');
const ciudadInput = document.getElementById('ciudad');
const fechaNacimientoInput = document.getElementById('fechaNacimiento');
const bioTextarea = document.getElementById('bio');

// Variables para guardar datos originales
let originalData = {};

// Navegación entre tabs
menuItems.forEach(item => {
    item.addEventListener('click', () => {
        // Remover active de todos los items
        menuItems.forEach(mi => mi.classList.remove('active'));
        
        // Agregar active al item clickeado
        item.classList.add('active');
        
        // Obtener el tab a mostrar
        const tabName = item.getAttribute('data-tab');
        
        // Ocultar todos los tabs
        tabContents.forEach(tc => tc.classList.remove('active'));
        
        // Mostrar el tab seleccionado
        const selectedTab = document.getElementById(tabName + 'Tab');
        if (selectedTab) {
            selectedTab.classList.add('active');
        }
    });
});

// Funcionalidad de edición del perfil
editBtn.addEventListener('click', () => {
    // Guardar datos originales
    originalData = {
        nombre: nombreInput.value,
        email: emailInput.value,
        telefono: telefonoInput.value,
        ciudad: ciudadInput.value,
        fechaNacimiento: fechaNacimientoInput.value,
        bio: bioTextarea.value
    };
    
    // Habilitar campos para edición
    nombreInput.removeAttribute('readonly');
    emailInput.removeAttribute('readonly');
    telefonoInput.removeAttribute('readonly');
    ciudadInput.removeAttribute('readonly');
    fechaNacimientoInput.removeAttribute('readonly');
    bioTextarea.removeAttribute('readonly');
    
    // Cambiar foco visual
    nombreInput.focus();
    
    // Mostrar botones de guardar/cancelar
    editBtn.style.display = 'none';
    editActions.style.display = 'flex';
});

// Guardar cambios
saveBtn.addEventListener('click', () => {
    // Aquí puedes agregar la lógica para enviar los datos al servidor
    console.log('Datos guardados:', {
        nombre: nombreInput.value,
        email: emailInput.value,
        telefono: telefonoInput.value,
        ciudad: ciudadInput.value,
        fechaNacimiento: fechaNacimientoInput.value,
        bio: bioTextarea.value
    });
    
    // Actualizar nombre en el sidebar
    const userName = document.getElementById('userName');
    const userEmail = document.getElementById('userEmail');
    const userAvatar = document.getElementById('userAvatar');
    
    userName.textContent = nombreInput.value;
    userEmail.textContent = emailInput.value;
    
    // Actualizar avatar con iniciales
    const initials = nombreInput.value
        .split(' ')
        .map(n => n[0])
        .join('')
        .toUpperCase();
    userAvatar.textContent = initials;
    
    // Deshabilitar campos
    nombreInput.setAttribute('readonly', true);
    emailInput.setAttribute('readonly', true);
    telefonoInput.setAttribute('readonly', true);
    ciudadInput.setAttribute('readonly', true);
    fechaNacimientoInput.setAttribute('readonly', true);
    bioTextarea.setAttribute('readonly', true);
    
    // Mostrar botón de editar
    editBtn.style.display = 'flex';
    editActions.style.display = 'none';
    
    // Mostrar mensaje de éxito (opcional)
    alert('Perfil actualizado correctamente');
});

// Cancelar edición
cancelBtn.addEventListener('click', () => {
    // Restaurar datos originales
    nombreInput.value = originalData.nombre;
    emailInput.value = originalData.email;
    telefonoInput.value = originalData.telefono;
    ciudadInput.value = originalData.ciudad;
    fechaNacimientoInput.value = originalData.fechaNacimiento;
    bioTextarea.value = originalData.bio;
    
    // Deshabilitar campos
    nombreInput.setAttribute('readonly', true);
    emailInput.setAttribute('readonly', true);
    telefonoInput.setAttribute('readonly', true);
    ciudadInput.setAttribute('readonly', true);
    fechaNacimientoInput.setAttribute('readonly', true);
    bioTextarea.setAttribute('readonly', true);
    
    // Mostrar botón de editar
    editBtn.style.display = 'flex';
    editActions.style.display = 'none';
});

// Funcionalidad de los botones de favoritos
document.querySelectorAll('.btn-remove').forEach(btn => {
    btn.addEventListener('click', function() {
        if (confirm('¿Deseas eliminar este evento de favoritos?')) {
            const card = this.closest('.favorite-card');
            card.style.opacity = '0';
            card.style.transform = 'scale(0.9)';
            setTimeout(() => {
                card.remove();
                // Actualizar contador
                const favCount = document.getElementById('favoritesCount');
                favCount.textContent = parseInt(favCount.textContent) - 1;
            }, 300);
        }
    });
});

// Funcionalidad de logout
document.querySelector('.logout-btn').addEventListener('click', () => {
    if (confirm('¿Seguro que deseas cerrar sesión?')) {
        // Aquí puedes agregar la lógica de logout
        window.location.href = '/login';
    }
});

// Animación de entrada para las tarjetas
window.addEventListener('load', () => {
    const cards = document.querySelectorAll('.ticket-card, .favorite-card');
    cards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';
            
            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 50);
        }, index * 100);
    });
});