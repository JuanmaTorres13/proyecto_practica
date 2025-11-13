// Elementos del DOM
const eventForm = document.getElementById('eventForm');
const previewBtn = document.getElementById('previewBtn');
const previewSidebar = document.getElementById('previewSidebar');
const previewClose = document.getElementById('previewClose');
const addTicketBtn = document.getElementById('addTicketBtn');
const ticketTypesContainer = document.getElementById('ticketTypes');
const successModal = document.getElementById('successModal');
const saveDraftBtn = document.getElementById('saveDraftBtn');
const viewEventBtn = document.getElementById('viewEventBtn');
const createAnotherBtn = document.getElementById('createAnotherBtn');

// Elementos de vista previa
const previewImage = document.getElementById('previewImage');
const previewType = document.getElementById('previewType');
const previewTitle = document.getElementById('previewTitle');
const previewArtist = document.getElementById('previewArtist');
const previewDate = document.getElementById('previewDate');
const previewTime = document.getElementById('previewTime');
const previewLocation = document.getElementById('previewLocation');
const previewDesc = document.getElementById('previewDesc');
const previewPrice = document.getElementById('previewPrice');

// Contador de tipos de entrada
let ticketTypeCount = 1;

// Abrir/Cerrar vista previa en móvil
previewBtn.addEventListener('click', () => {
    previewSidebar.classList.toggle('mobile-show');
});

previewClose.addEventListener('click', () => {
    previewSidebar.classList.remove('mobile-show');
});

// Añadir nuevo tipo de entrada
addTicketBtn.addEventListener('click', () => {
    ticketTypeCount++;
    
    const ticketTypeHTML = `
        <div class="ticket-type-item">
            <div class="form-grid">
                <div class="form-group">
                    <label>Tipo de Entrada *</label>
                    <input type="text" name="ticketTypeName[]" placeholder="Ej: General, VIP, Platea" required>
                </div>

                <div class="form-group">
                    <label>Precio (€) *</label>
                    <input type="number" name="ticketTypePrice[]" placeholder="0.00" step="0.01" min="0" required>
                </div>

                <div class="form-group">
                    <label>Cantidad Disponible *</label>
                    <input type="number" name="ticketTypeQuantity[]" placeholder="100" min="1" required>
                </div>

                <div class="form-group">
                    <label>Descripción</label>
                    <input type="text" name="ticketTypeDescription[]" placeholder="Detalles de esta entrada">
                </div>
            </div>
            <button type="button" class="btn-remove-ticket">
                <span>🗑️</span>
                Eliminar
            </button>
        </div>
    `;
    
    ticketTypesContainer.insertAdjacentHTML('beforeend', ticketTypeHTML);
    
    // Añadir evento al botón de eliminar
    const removeButtons = ticketTypesContainer.querySelectorAll('.btn-remove-ticket');
    removeButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            if (ticketTypesContainer.children.length > 1) {
                this.closest('.ticket-type-item').remove();
                ticketTypeCount--;
            } else {
                alert('Debe haber al menos un tipo de entrada');
            }
        });
    });
    
    // Actualizar vista previa
    updatePreview();
});

// Evento para eliminar el primer tipo de entrada (si se añaden más)
document.addEventListener('click', (e) => {
    if (e.target.closest('.btn-remove-ticket')) {
        const btn = e.target.closest('.btn-remove-ticket');
        if (ticketTypesContainer.children.length > 1) {
            btn.closest('.ticket-type-item').remove();
            ticketTypeCount--;
            updatePreview();
        } else {
            alert('Debe haber al menos un tipo de entrada');
        }
    }
});

// Actualizar vista previa en tiempo real
function updatePreview() {
    // Tipo de evento
    const eventType = document.querySelector('input[name="eventType"]:checked');
    if (eventType) {
        const typeLabels = {
            'cine': '🎬 Cine',
            'festival': '🎪 Festival',
            'concierto': '🎵 Concierto'
        };
        previewType.textContent = typeLabels[eventType.value] || 'Evento';
    }
    
    // Información básica
    const eventName = document.getElementById('eventName').value;
    previewTitle.textContent = eventName || 'Nombre del Evento';
    
    const eventArtist = document.getElementById('eventArtist').value;
    previewArtist.textContent = eventArtist || 'Artista';
    
    const eventDescription = document.getElementById('eventDescription').value;
    previewDesc.textContent = eventDescription || 'Descripción del evento...';
    
    const eventImage = document.getElementById('eventImage').value;
    if (eventImage) {
        previewImage.src = eventImage;
    }
    
    // Ubicación y fecha
    const eventDate = document.getElementById('eventDate').value;
    if (eventDate) {
        const date = new Date(eventDate);
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        previewDate.textContent = date.toLocaleDateString('es-ES', options);
    } else {
        previewDate.textContent = 'Fecha';
    }
    
    const eventTime = document.getElementById('eventTime').value;
    previewTime.textContent = eventTime || 'Hora';
    
    const eventVenue = document.getElementById('eventVenue').value;
    const eventCity = document.getElementById('eventCity').value;
    if (eventVenue && eventCity) {
        previewLocation.textContent = `${eventVenue}, ${eventCity}`;
    } else if (eventVenue || eventCity) {
        previewLocation.textContent = eventVenue || eventCity;
    } else {
        previewLocation.textContent = 'Ubicación';
    }
    
    // Precio mínimo
    const prices = document.querySelectorAll('input[name="ticketTypePrice[]"]');
    let minPrice = Infinity;
    prices.forEach(input => {
        const price = parseFloat(input.value);
        if (price && price < minPrice) {
            minPrice = price;
        }
    });
    
    if (minPrice !== Infinity) {
        previewPrice.textContent = `€${minPrice.toFixed(2)}`;
    } else {
        previewPrice.textContent = '€0.00';
    }
}

// Escuchar cambios en todos los inputs del formulario
eventForm.addEventListener('input', updatePreview);
eventForm.addEventListener('change', updatePreview);

// Guardar como borrador
saveDraftBtn.addEventListener('click', () => {
    const formData = new FormData(eventForm);
    const data = {};
    
    for (let [key, value] of formData.entries()) {
        if (key.includes('[]')) {
            const cleanKey = key.replace('[]', '');
            if (!data[cleanKey]) {
                data[cleanKey] = [];
            }
            data[cleanKey].push(value);
        } else {
            data[key] = value;
        }
    }
    
    // Guardar en localStorage
    localStorage.setItem('eventDraft', JSON.stringify(data));
    
    alert('✅ Borrador guardado correctamente');
});

// Cargar borrador si existe
window.addEventListener('load', () => {
    const draft = localStorage.getItem('eventDraft');
    if (draft && confirm('Se encontró un borrador guardado. ¿Deseas cargarlo?')) {
        const data = JSON.parse(draft);
        
        // Rellenar campos básicos
        for (let key in data) {
            if (!key.includes('ticketType')) {
                const input = eventForm.querySelector(`[name="${key}"]`);
                if (input) {
                    if (input.type === 'radio') {
                        const radio = eventForm.querySelector(`[name="${key}"][value="${data[key]}"]`);
                        if (radio) radio.checked = true;
                    } else if (input.type === 'checkbox') {
                        input.checked = data[key] === 'on';
                    } else {
                        input.value = data[key];
                    }
                }
            }
        }
        
        updatePreview();
    }
});

// Enviar formulario
eventForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    // Validar que haya al menos un tipo de entrada
    const ticketPrices = document.querySelectorAll('input[name="ticketTypePrice[]"]');
    if (ticketPrices.length === 0) {
        alert('⚠️ Debes añadir al menos un tipo de entrada');
        return;
    }
    
    // Recopilar datos del formulario
    const formData = new FormData(eventForm);
    const data = {
        ticketTypes: []
    };
    
    // Procesar datos
    for (let [key, value] of formData.entries()) {
        if (key.includes('[]')) {
            const cleanKey = key.replace('[]', '');
            if (!data[cleanKey]) {
                data[cleanKey] = [];
            }
            data[cleanKey].push(value);
        } else {
            data[key] = value;
        }
    }
    
    // Organizar tipos de entrada
    if (data.ticketTypeName) {
        for (let i = 0; i < data.ticketTypeName.length; i++) {
            data.ticketTypes.push({
                name: data.ticketTypeName[i],
                price: data.ticketTypePrice[i],
                quantity: data.ticketTypeQuantity[i],
                description: data.ticketTypeDescription[i]
            });
        }
    }
    
    // Aquí harías la petición al backend
    console.log('Datos del evento:', data);
    
    // Simular envío
    setTimeout(() => {
        // Limpiar borrador
        localStorage.removeItem('eventDraft');
        
        // Mostrar modal de éxito
        successModal.classList.add('show');
    }, 500);
});

// Botones del modal de éxito
viewEventBtn.addEventListener('click', () => {
    // Redirigir a la página del evento
    window.location.href = '/eventos';
});

createAnotherBtn.addEventListener('click', () => {
    successModal.classList.remove('show');
    eventForm.reset();
    updatePreview();
    window.scrollTo({ top: 0, behavior: 'smooth' });
});

// Cerrar modal al hacer clic fuera
successModal.addEventListener('click', (e) => {
    if (e.target === successModal) {
        successModal.classList.remove('show');
    }
});

// Validación de imagen URL
document.getElementById('eventImage').addEventListener('blur', function() {
    const url = this.value;
    if (url) {
        const img = new Image();
        img.onload = () => {
            previewImage.src = url;
        };
        img.onerror = () => {
            alert('⚠️ La URL de la imagen no es válida');
            this.value = '';
        };
        img.src = url;
    }
});

// Validación de fechas
document.getElementById('eventDate').addEventListener('change', function() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const selectedDate = new Date(this.value);
    
    if (selectedDate < today) {
        alert('⚠️ La fecha del evento no puede ser anterior a hoy');
        this.value = '';
    }
});

document.getElementById('eventEndDate').addEventListener('change', function() {
    const startDate = document.getElementById('eventDate').value;
    const endDate = this.value;
    
    if (startDate && endDate && new Date(endDate) < new Date(startDate)) {
        alert('⚠️ La fecha de fin no puede ser anterior a la fecha de inicio');
        this.value = '';
    }
});

// Inicializar vista previa
updatePreview();