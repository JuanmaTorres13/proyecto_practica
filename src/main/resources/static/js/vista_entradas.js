// Datos de ejemplo
const ticketsData = [
    {
        id: 1,
        eventName: "Inception",
        eventType: "cine",
        typeIcon: "🎬",
        ticketType: "VIP",
        date: "2025-12-20",
        time: "20:30",
        location: "Cinepolis Centro",
        city: "Córdoba",
        address: "Av. Principal 123",
        price: "12.50",
        quantity: 2,
        sala: "Sala 3",
        language: "Inglés subtitulado",
        director: "Christopher Nolan"
    },
    {
        id: 2,
        eventName: "Coldplay World Tour",
        eventType: "concierto",
        typeIcon: "🎵",
        ticketType: "General",
        date: "2025-12-25",
        time: "21:00",
        location: "Estadio Municipal",
        city: "Sevilla",
        address: "Calle del Estadio 45",
        price: "75.00",
        quantity: 1,
        artist: "Coldplay",
        venue: "Estadio Municipal",
        doors: "19:00"
    },
    {
        id: 3,
        eventName: "Festival Primavera Sound",
        eventType: "festival",
        typeIcon: "🎪",
        ticketType: "Pase 3 días",
        date: "2026-06-01",
        time: "16:00",
        location: "Parc del Fòrum",
        city: "Barcelona",
        address: "Parc del Fòrum s/n",
        price: "220.00",
        quantity: 1,
        days: 3,
        lineup: "Arctic Monkeys, The Strokes, Lorde"
    },
    {
        id: 4,
        eventName: "Dune: Part Two",
        eventType: "cine",
        typeIcon: "🎬",
        ticketType: "Estándar",
        date: "2025-12-15",
        time: "18:00",
        location: "Cines Yelmo",
        city: "Madrid",
        address: "Gran Vía 28",
        price: "9.50",
        quantity: 3,
        sala: "Sala IMAX",
        language: "Español",
        director: "Denis Villeneuve"
    }
];

function renderTickets(tickets) {
    const container = document.getElementById('ticketsContainer');
    const emptyState = document.getElementById('emptyState');
    
    if (tickets.length === 0) {
        container.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    container.style.display = 'grid';
    emptyState.style.display = 'none';
    
    container.innerHTML = tickets.map(ticket => `
        <div class="ticket-card" onclick="viewTicketDetails(${ticket.id})">
            <div class="ticket-header">
                <span class="ticket-type-badge">${ticket.ticketType}</span>
                <h3>${ticket.eventName}</h3>
                <div class="event-type">${ticket.typeIcon} ${ticket.eventType.charAt(0).toUpperCase() + ticket.eventType.slice(1)}</div>
            </div>
            <div class="ticket-body">
                <div class="qr-code">📱</div>
                <div class="ticket-info">
                    <div class="info-row">
                        <span class="info-icon">📅</span>
                        <span class="info-label">Fecha:</span>
                        <span class="info-value">${formatDate(ticket.date)}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-icon">🕐</span>
                        <span class="info-label">Hora:</span>
                        <span class="info-value">${ticket.time}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-icon">📍</span>
                        <span class="info-label">Lugar:</span>
                        <span class="info-value">${ticket.location}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-icon">🏙️</span>
                        <span class="info-label">Ciudad:</span>
                        <span class="info-value">${ticket.city}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-icon">🎟️</span>
                        <span class="info-label">Cantidad:</span>
                        <span class="info-value">${ticket.quantity} entrada(s)</span>
                    </div>
                    ${getEventSpecificInfo(ticket)}
                </div>
                <div class="ticket-price">
                    ${ticket.price}€ × ${ticket.quantity} = ${(parseFloat(ticket.price) * ticket.quantity).toFixed(2)}€
                </div>
                <div class="ticket-actions">
                    <button class="btn btn-primary" onclick="event.stopPropagation(); downloadTicket(${ticket.id})">
                        📥 Descargar
                    </button>
                    <button class="btn btn-secondary" onclick="event.stopPropagation(); shareTicket(${ticket.id})">
                        🔗 Compartir
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

function getEventSpecificInfo(ticket) {
    switch(ticket.eventType) {
        case 'cine':
            return `
                <div class="info-row">
                    <span class="info-icon">🎬</span>
                    <span class="info-label">Sala:</span>
                    <span class="info-value">${ticket.sala}</span>
                </div>
                <div class="info-row">
                    <span class="info-icon">🌐</span>
                    <span class="info-label">Idioma:</span>
                    <span class="info-value">${ticket.language}</span>
                </div>
            `;
        case 'concierto':
            return `
                <div class="info-row">
                    <span class="info-icon">🎤</span>
                    <span class="info-label">Artista:</span>
                    <span class="info-value">${ticket.artist}</span>
                </div>
                <div class="info-row">
                    <span class="info-icon">🚪</span>
                    <span class="info-label">Puertas:</span>
                    <span class="info-value">${ticket.doors}</span>
                </div>
            `;
        case 'festival':
            return `
                <div class="info-row">
                    <span class="info-icon">📆</span>
                    <span class="info-label">Duración:</span>
                    <span class="info-value">${ticket.days} días</span>
                </div>
                <div class="info-row">
                    <span class="info-icon">🎸</span>
                    <span class="info-label">Lineup:</span>
                    <span class="info-value">${ticket.lineup}</span>
                </div>
            `;
        default:
            return '';
    }
}

function formatDate(dateString) {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('es-ES', options);
}

function filterTickets() {
    const typeFilter = document.getElementById('filterType').value;
    const dateFilter = document.getElementById('filterDate').value;
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let filtered = ticketsData.filter(ticket => {
        const matchesType = typeFilter === 'all' || ticket.eventType === typeFilter;
        const ticketDate = new Date(ticket.date);
        const matchesDate = dateFilter === 'all' || 
            (dateFilter === 'upcoming' && ticketDate >= today) ||
            (dateFilter === 'past' && ticketDate < today);
        const matchesSearch = ticket.eventName.toLowerCase().includes(searchTerm) ||
            ticket.city.toLowerCase().includes(searchTerm) ||
            ticket.location.toLowerCase().includes(searchTerm);
        
        return matchesType && matchesDate && matchesSearch;
    });

    renderTickets(filtered);
}

function viewTicketDetails(id) {
    alert(`Ver detalles completos de la entrada #${id}`);
}

function downloadTicket(id) {
    alert(`Descargando entrada #${id}...`);
}

function shareTicket(id) {
    alert(`Compartiendo entrada #${id}...`);
}

// Event listeners
document.getElementById('filterType').addEventListener('change', filterTickets);
document.getElementById('filterDate').addEventListener('change', filterTickets);
document.getElementById('searchInput').addEventListener('input', filterTickets);

// Renderizado inicial
renderTickets(ticketsData);