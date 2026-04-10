import { useState, useEffect } from 'react';
import { EventCard } from '../components/EventCard';
import { api } from '../utils/api';
import { getEventStatus } from '../utils/eventUtils';

export function EventsArchive() {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function load() {
            setLoading(true);
            try {
                const data = await api.getAllEvents();
                const all = Array.isArray(data) ? data : [];
                const past = all.filter((e) => getEventStatus(e) === 'past');
                past.sort((a, b) => {
                    const tA = new Date(a.date + 'T' + a.time).getTime();
                    const tB = new Date(b.date + 'T' + b.time).getTime();
                    return tB - tA;
                });
                setEvents(past);
            } catch (err) {
                console.warn('Failed to load archive:', err);
                setEvents([]);
            } finally {
                setLoading(false);
            }
        }
        load();
    }, []);

    return (
        <div>
            <div className="mb-8">
                <h1 className="text-2xl font-semibold text-ntu-dark mb-2">Events archive</h1>
                <p className="text-ntu-tundora">
                    Past events and their ratings. Only events that have ended are shown here.
                </p>
            </div>
            {loading ? (
                <p className="text-center py-12 text-ntu-tundora">Loading archive...</p>
            ) : events.length === 0 ? (
                <p className="text-center py-12 text-ntu-tundora">No past events in the archive.</p>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {events.map((event) => (
                        <EventCard key={event.eventId} event={event} />
                    ))}
                </div>
            )}
        </div>
    );
}