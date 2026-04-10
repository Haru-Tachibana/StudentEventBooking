import { useState, useEffect } from 'react';
import { EventCard } from '../components/EventCard';
import { api } from '../utils/api';
import { getEventStatus } from '../utils/eventUtils';

//`GET {base}/events` (load all events; filtering is client-side).

const EVENT_TYPES = ['All Types', 'Workshop', 'Seminar', 'Career', 'Sports', 'Cultural', 'Social'];

export function Home() {
    const [events, setEvents] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [searchType, setSearchType] = useState('All Types');
    const [searchLocation, setSearchLocation] = useState('');
    const [loading, setLoading] = useState(true);

    const [publicEvents, setPublicEvents] = useState([]);
    const [publicLoading, setPublicLoading] = useState(false);
    const [publicError, setPublicError] = useState('');
    const [locationStatus, setLocationStatus] = useState('');
    const [locationLabel, setLocationLabel] = useState(null);

    useEffect(() => {
        loadEvents();
    }, []);

    useEffect(() => {
        if (!navigator.geolocation) return;
        navigator.geolocation.getCurrentPosition(
            async (position) => {
                const { latitude, longitude } = position.coords;
                try {
                    const data = await api.reverseGeocode(latitude, longitude);
                    setLocationLabel(data.displayName || data.postcode || 'your area');
                } catch {
                    setLocationLabel('your area');
                }
            },
            () => setLocationLabel('your area'),
            { enableHighAccuracy: false, timeout: 5000, maximumAge: 300000 }
        );
    }, []);

    async function loadEvents() {
        setLoading(true);
        try {
            const data = await api.getAllEvents();
            const all = Array.isArray(data) ? data : [];
            const upcomingOnly = all.filter((e) => getEventStatus(e) !== 'past');
            setEvents(upcomingOnly);
            setFiltered(upcomingOnly);
        } catch (err) {
            console.warn('Failed to load events:', err);
            setEvents([]);
            setFiltered([]);
        } finally {
            setLoading(false);
        }
    }

    function handleFilter() {
        let result = [...events];
        if (searchType && searchType !== 'All Types') {
            result = result.filter((e) => e.type && e.type.toLowerCase() === searchType.toLowerCase());
        }
        if (searchLocation.trim()) {
            const loc = searchLocation.toLowerCase();
            result = result.filter(
                (e) =>
                    (e.location && e.location.toLowerCase().includes(loc)) ||
                    (e.venue && e.venue.toLowerCase().includes(loc))
            );
        }
        setFiltered(result);
    }

    function handleReset() {
        setSearchType('All Types');
        setSearchLocation('');
        setFiltered(events);
    }


    async function handleGetMyLocation() {
        setPublicLoading(true);
        setPublicError('');
        setLocationStatus('');
        setPublicEvents([]);
        if (!navigator.geolocation) {
            setPublicError('Geolocation is not supported by your browser.');
            setPublicLoading(false);
            return;
        }
        navigator.geolocation.getCurrentPosition(
            async (position) => {
                const { latitude, longitude } = position.coords;
                setLocationStatus(`Using location (${latitude.toFixed(4)}, ${longitude.toFixed(4)})`);
                try {
                    const [eventsData, placeData] = await Promise.all([
                        api.getNearbyPublicEvents(latitude, longitude, 20),
                        api.reverseGeocode(latitude, longitude).catch(() => null),
                    ]);
                    setPublicEvents(Array.isArray(eventsData) ? eventsData : []);
                    if (!Array.isArray(eventsData) || eventsData.length === 0) setPublicError('No public events found nearby.');
                    if (placeData?.displayName || placeData?.postcode) setLocationLabel(placeData.displayName || placeData.postcode);
                } catch (err) {
                    setPublicError(err.message || 'Could not load nearby events.');
                } finally {
                    setPublicLoading(false);
                }
            },
            () => {
                setPublicError('Could not get your location. Allow location access and try again.');
                setPublicLoading(false);
            }
        );
    }

    return (
        <div>
            <div className="mb-6">
                <h1 className="text-2xl font-semibold text-ntu-dark mb-1">
                    Events near {locationLabel ?? 'you'}
                </h1>
                <p className="text-ntu-tundora text-sm">
                    Discover and register for upcoming events
                </p>
            </div>

            {/* Search – single bar, no big card */}
            <div className="flex flex-wrap items-end gap-3 mb-8 pb-6 border-b border-gray-200">
                <div className="flex-1 min-w-[120px]">
                    <label htmlFor="type" className="block text-xs font-medium text-ntu-tundora mb-1">Type</label>
                    <select
                        id="type"
                        value={searchType}
                        onChange={(e) => setSearchType(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark focus:border-ntu-rose focus:ring-1 focus:ring-ntu-rose"
                    >
                        {EVENT_TYPES.map((t) => (
                            <option key={t} value={t}>{t}</option>
                        ))}
                    </select>
                </div>
                <div className="flex-1 min-w-[140px]">
                    <label htmlFor="location" className="block text-xs font-medium text-ntu-tundora mb-1">Location / Postcode</label>
                    <input
                        id="location"
                        placeholder="e.g. NG1 1AA"
                        value={searchLocation}
                        onChange={(e) => setSearchLocation(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark focus:border-ntu-rose focus:ring-1 focus:ring-ntu-rose"
                    />
                </div>
                <div className="flex gap-2">
                    <button
                        type="button"
                        onClick={handleFilter}
                        className="py-2 px-4 bg-ntu-rose text-white rounded-md hover:bg-ntu-pink font-medium"
                    >
                        Search
                    </button>
                    <button
                        type="button"
                        onClick={handleReset}
                        className="py-2 px-4 border border-gray-300 rounded-md text-ntu-dark hover:bg-gray-50 font-medium"
                    >
                        Reset
                    </button>
                </div>
            </div>

            {/* Public events near you */}
            <section className="mb-10">
                <h2 className="text-xl font-semibold text-ntu-dark mb-3">Public events near you</h2>
                <p className="text-ntu-tundora text-sm mb-4">
                    Get events from Skiddle by your current location.
                </p>
                <button
                    type="button"
                    onClick={handleGetMyLocation}
                    disabled={publicLoading}
                    className="py-2 px-4 border-2 border-ntu-rose text-ntu-rose rounded-md hover:bg-ntu-rose/5 font-medium disabled:opacity-50"
                >
                    {publicLoading ? 'Loading…' : 'Get my location'}
                </button>
                {locationStatus && (
                    <p className="text-xs text-ntu-tundora mt-2">{locationStatus}</p>
                )}
                {publicError && (
                    <p className="text-sm text-amber-700 mt-2">{publicError}</p>
                )}
                {publicEvents.length > 0 && (
                    <ul className="mt-4 space-y-3">
                        {publicEvents.map((evt, i) => (
                            <li key={evt.id || i} className="flex gap-3 items-start text-sm">
                                {evt.largeimageurl && (
                                    <img src={evt.largeimageurl} alt="" className="w-16 h-16 object-cover rounded flex-shrink-0" />
                                )}
                                <div className="min-w-0">
                                    <a
                                        href={evt.link || '#'}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="font-medium text-ntu-rose hover:text-ntu-pink hover:underline"
                                    >
                                        {evt.eventname || 'Event'}
                                    </a>
                                    {evt.venue && (
                                        <p className="text-ntu-tundora">{evt.venue.name || evt.venue}</p>
                                    )}
                                    {evt.startdate && (
                                        <p className="text-ntu-tundora text-xs">
                                            {new Date(evt.startdate).toLocaleString('en-GB')}
                                        </p>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </section>

            
            <h2 className="text-xl font-semibold text-ntu-dark mb-4">Upcoming university events</h2>
            {loading ? (
                <p className="text-center py-12 text-ntu-tundora">Loading events...</p>
            ) : filtered.length === 0 ? (
                <p className="text-center py-12 text-ntu-tundora">No upcoming events found. Try adjusting your filters or check the Events archive for past events.</p>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filtered.map((event) => (
                        <EventCard key={event.eventId} event={event} />
                    ))}
                </div>
            )}
        </div>
    );
}