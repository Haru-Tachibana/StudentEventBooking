import { getIdToken } from '../firebase/auth';

// Dev: same-origin /api → Vite proxy (avoids CORS). Prod/Docker: full URL from build env.
let API_BASE;
if (import.meta.env.DEV) {
    API_BASE = '/api';
} else if (import.meta.env.VITE_API_BASE_URL) {
    API_BASE = import.meta.env.VITE_API_BASE_URL;
} else {
    API_BASE = 'http://localhost:9090/StudentEventBooking/webresources';
}

async function authHeaders() {
    const token = await getIdToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return headers;
}

async function handleResponse(res) {
    if (!res.ok) {
        const err = await res.json().catch(() => ({ error: res.statusText }));
        throw new Error(err.error || `HTTP ${res.status}`);
    }
    return res.json();
}

export const api = {
    registerStudent: async (body) => {
        const res = await fetch(`${API_BASE}/students`, {
            method: 'POST',
            headers: await authHeaders(),
            body: JSON.stringify(body),
        });
        return handleResponse(res);
    },

    getAllEvents: async () => {
        const res = await fetch(`${API_BASE}/events`, { headers: await authHeaders() });
        return handleResponse(res);
    },

    getEvent: async (eventId) => {
        const res = await fetch(`${API_BASE}/events/${eventId}`, { headers: await authHeaders() });
        return handleResponse(res);
    },

    searchEvents: async (params = {}) => {
        const q = new URLSearchParams(params).toString();
        const url = q ? `${API_BASE}/events?${q}` : `${API_BASE}/events`;
        const res = await fetch(url, { headers: await authHeaders() });
        return handleResponse(res);
    },

    registerForEvent: async (eventId) => {
        const res = await fetch(
            `${API_BASE}/events/${eventId}/register`,
            { method: 'POST', headers: await authHeaders() }
        );
        return handleResponse(res);
    },

    getCurrentStudent: async () => {
        const res = await fetch(`${API_BASE}/students/me`, { headers: await authHeaders() });
        return handleResponse(res);
    },

    createEvent: async (event) => {
        const res = await fetch(`${API_BASE}/events`, {
            method: 'POST',
            headers: await authHeaders(),
            body: JSON.stringify(event),
        });
        return handleResponse(res);
    },

    getMyEvents: async () => {
        const res = await fetch(`${API_BASE}/events/my-events`, { headers: await authHeaders() });
        return handleResponse(res);
    },

    getMyRegistrations: async () => {
        const res = await fetch(`${API_BASE}/events/my-registrations`, { headers: await authHeaders() });
        return handleResponse(res);
    },

    updateEvent: async (eventId, event) => {
        const res = await fetch(`${API_BASE}/events/${eventId}`, {
            method: 'PUT',
            headers: await authHeaders(),
            body: JSON.stringify(event),
        });
        return handleResponse(res);
    },

    deleteEvent: async (eventId) => {
        const res = await fetch(`${API_BASE}/events/${eventId}`, {
            method: 'DELETE',
            headers: await authHeaders(),
        });
        return handleResponse(res);
    },

    cancelRegistration: async (eventId) => {
        const res = await fetch(`${API_BASE}/events/${eventId}/cancel-registration`, {
            method: 'POST',
            headers: await authHeaders(),
        });
        return handleResponse(res);
    },

    getEventRating: async (eventId) => {
        const res = await fetch(`${API_BASE}/events/${eventId}/rating`, { headers: await authHeaders() });
        return handleResponse(res);
    },

    submitRating: async (eventId, body) => {
        const res = await fetch(`${API_BASE}/events/${eventId}/ratings`, {
            method: 'POST',
            headers: await authHeaders(),
            body: JSON.stringify(body),
        });
        return handleResponse(res);
    },

    getPendingRatings: async (studentId) => {
        const res = await fetch(`${API_BASE}/students/${studentId}/pending-ratings`, { headers: await authHeaders() });
        return handleResponse(res);
    },

    getWeather: async (postcode) => {
        const res = await fetch(
            `${API_BASE}/external/weather?postcode=${encodeURIComponent(postcode)}`,
            { headers: await authHeaders() }
        );
        return handleResponse(res);
    },

    getMapEmbedUrl: async (postcodeOrPlace) => {
        const key = postcodeOrPlace.match(/^[A-Z0-9\s]{5,8}$/i) ? 'postcode' : 'place';
        const res = await fetch(
            `${API_BASE}/external/map-embed-url?${key}=${encodeURIComponent(postcodeOrPlace)}`,
            { headers: await authHeaders() }
        );
        return handleResponse(res);
    },

    getNearbyPublicEvents: async (latitude, longitude, radiusMiles = 20) => {
        const params = new URLSearchParams({
            latitude: String(latitude),
            longitude: String(longitude),
            radius: String(radiusMiles),
        });
        const res = await fetch(
            `${API_BASE}/external/nearby-events?${params}`,
            { headers: await authHeaders() }
        );
        return handleResponse(res);
    },

    reverseGeocode: async (latitude, longitude) => {
        const params = new URLSearchParams({
            latitude: String(latitude),
            longitude: String(longitude),
        });
        const res = await fetch(
            `${API_BASE}/external/reverse-geocode?${params}`,
            { headers: await authHeaders() }
        );
        return handleResponse(res);
    },
};