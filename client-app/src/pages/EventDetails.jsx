import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../utils/api';
import { getEventStatus } from '../utils/eventUtils';

// `GET {base}/events/{eventId}` – load event
// `POST {base}/events/{eventId}/register?studentId=...` – register for event
// `GET {base}/external/weather?postcode=...` – when user clicks “Check weather”
// `GET {base}/external/map-embed-url?postcode=...` (or `?place=...`) – when user clicks “Show map”

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('en-GB', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    });
}

function formatDuration(minutes) {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h > 0 && m > 0) return `${h}h ${m}m`;
    if (h > 0) return `${h}h`;
    return `${m}m`;
}

export function EventDetails() {
    const { eventId } = useParams();
    const navigate = useNavigate();
    const [event, setEvent] = useState(null);
    const [currentStudent, setCurrentStudent] = useState(null);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [weather, setWeather] = useState(null);
    const [mapEmbedUrl, setMapEmbedUrl] = useState(null);
    const [weatherLoading, setWeatherLoading] = useState(false);
    const [mapLoading, setMapLoading] = useState(false);
    const [pendingRatingEventIds, setPendingRatingEventIds] = useState(new Set());
    const [ratingValue, setRatingValue] = useState(5);
    const [ratingLoading, setRatingLoading] = useState(false);

    useEffect(() => {
        if (eventId) loadEvent();
    }, [eventId]);

    useEffect(() => {
        api.getCurrentStudent()
            .then((student) => {
                setCurrentStudent(student);
                if (student && student.studentId) {
                    return api.getPendingRatings(student.studentId).then((pending) => {
                        setPendingRatingEventIds(new Set((pending || []).map((e) => e.eventId)));
                    });
                }
            })
            .catch(() => setCurrentStudent(null));
    }, []);

    async function loadEvent() {
        try {
            const data = await api.getEvent(eventId);
            setEvent(data);
        } catch (err) {
            setEvent(null);
        }
    }

    const handleRegister = async (e) => {
        e.preventDefault();
        if (!event) return;
        if ((event.attendees?.length || 0) >= event.maxParticipants) {
            setMessage('Sorry, this event is full');
            return;
        }
        setLoading(true);
        setMessage('');
        try {
            await api.registerForEvent(event.eventId);
            setMessage('You\'ve successfully registered for the event.');
            loadEvent();
            api.getCurrentStudent().then(setCurrentStudent).catch(() => setCurrentStudent(null));
        } catch (err) {
            setMessage(err.message || 'Registration failed. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    const handleCancelRegistration = async (e) => {
        e.preventDefault();
        if (!event) return;
        setLoading(true);
        setMessage('');
        try {
            await api.cancelRegistration(event.eventId);
            setMessage('Registration cancelled.');
            loadEvent();
            api.getCurrentStudent().then(setCurrentStudent).catch(() => setCurrentStudent(null));
        } catch (err) {
            setMessage(err.message || 'Failed to cancel registration.');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmitRating = async (e) => {
        e.preventDefault();
        if (!event || !currentStudent) return;
        setRatingLoading(true);
        setMessage('');
        try {
            await api.submitRating(event.eventId, { studentId: currentStudent.studentId, rating: ratingValue });
            setMessage('Rating submitted. Thank you!');
            setPendingRatingEventIds((prev) => {
                const next = new Set(prev);
                next.delete(event.eventId);
                return next;
            });
            loadEvent();
        } catch (err) {
            setMessage(err.message || 'Failed to submit rating.');
        } finally {
            setRatingLoading(false);
        }
    };

    async function loadWeather() {
        if (!event?.location) return;
        setWeatherLoading(true);
        setWeather(null);
        try {
            const data = await api.getWeather(event.location);
            setWeather(data);
        } catch (err) {
            setWeather({ error: 'Weather data unavailable' });
        } finally {
            setWeatherLoading(false);
        }
    }

    async function loadMap() {
        if (!event?.location) return;
        setMapLoading(true);
        setMapEmbedUrl(null);
        try {
            const data = await api.getMapEmbedUrl(event.location);
            if (data && data.embedUrl) setMapEmbedUrl(data.embedUrl);
        } catch (err) {
            console.warn('Map embed failed:', err);
        } finally {
            setMapLoading(false);
        }
    }

    if (event === undefined) {
        return (
            <div className="text-center py-12 text-ntu-tundora">Loading...</div>
        );
    }

    if (!event) {
        return (
            <div className="text-center py-12">
                <p className="text-ntu-tundora mb-4">Event not found</p>
                <button
                    type="button"
                    onClick={() => navigate('/')}
                    className="px-4 py-2 bg-ntu-rose text-white rounded-md hover:bg-ntu-pink"
                >
                    Back to Events
                </button>
            </div>
        );
    }

    const spots = event.maxParticipants - (event.attendees?.length || 0);
    const isFull = spots === 0;
    const isRegistered = currentStudent && event.attendees && event.attendees.includes(currentStudent.studentId);
    const eventStatus = getEventStatus(event);
    const isPublisher = currentStudent && event.publisherId === currentStudent.studentId;
    const canCancelRegistration = isRegistered && eventStatus !== 'past';
    const canRate = !isPublisher && isRegistered && eventStatus === 'past' && pendingRatingEventIds.has(event.eventId);
    const alreadyRated = !isPublisher && isRegistered && eventStatus === 'past' && !pendingRatingEventIds.has(event.eventId);
    const showRatingForm = canRate || alreadyRated;
    let ratingSubmitLabel = 'Submit rating';
    if (alreadyRated) {
        ratingSubmitLabel = 'Update rating';
    }
    const publisherCannotRate = isPublisher && eventStatus === 'past';

    return (
        <div>
            <button
                type="button"
                onClick={() => navigate('/')}
                className="mb-6 flex items-center gap-2 text-ntu-rose hover:text-ntu-pink font-medium"
            >
                Back to events
            </button>

            {event.imageUrl && (
                <div className="mb-6 rounded-lg overflow-hidden">
                    <img src={event.imageUrl} alt={event.title} className="w-full h-64 object-cover" />
                </div>
            )}

            <header className="mb-8">
                <div className="flex flex-wrap items-center gap-2 mb-2">
                    <span className="text-sm px-3 py-1 bg-ntu-rose/10 text-ntu-rose rounded-full">
                        {event.type}
                    </span>
                    {event.cost === 0 && (
                        <span className="text-sm px-3 py-1 bg-green-100 text-green-800 rounded-full">Free</span>
                    )}
                    {event.averageRating != null && event.ratingCount != null && event.ratingCount > 0 && (
                        <span className="text-sm px-3 py-1 bg-amber-100 text-amber-800 rounded-full font-medium">
                            ★ {Number(event.averageRating).toFixed(1)} ({event.ratingCount} rating{event.ratingCount !== 1 ? 's' : ''})
                        </span>
                    )}
                </div>
                <h1 className="text-3xl font-semibold text-ntu-dark mb-2">{event.title}</h1>
                <p className="text-ntu-tundora">
                    {formatDate(event.date)} · {event.time} · {formatDuration(event.duration)}
                </p>
                <p className="text-ntu-tundora">
                    {event.venue}, {event.location}
                </p>
            </header>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-8">
                    <section>
                        <h2 className="text-xl font-semibold text-ntu-dark mb-3 border-b border-ntu-rose/20 pb-2">Event details</h2>
                        <p className="text-ntu-tundora leading-relaxed whitespace-pre-wrap">{event.description}</p>
                        <p className="text-ntu-tundora mt-4">
                            <strong className="text-ntu-dark">Attendees:</strong> {event.attendees?.length || 0} / {event.maxParticipants}
                            {event.cost > 0 && (
                                <> · <strong className="text-ntu-dark">Cost:</strong> £{Number(event.cost).toFixed(2)}</>
                            )}
                        </p>
                        {event.averageRating != null && event.ratingCount != null && event.ratingCount > 0 && (
                            <p className="text-ntu-tundora mt-2">
                                <strong className="text-ntu-dark">Rating:</strong> ★ {Number(event.averageRating).toFixed(1)} ({event.ratingCount} rating{event.ratingCount !== 1 ? 's' : ''})
                            </p>
                        )}
                    </section>

                    <section>
                        <h2 className="text-xl font-semibold text-ntu-dark mb-3 border-b border-ntu-rose/20 pb-2">Location</h2>
                        <p className="text-ntu-tundora font-medium">{event.venue}</p>
                        <p className="text-ntu-tundora text-sm mb-3">{event.location}</p>
                        <button
                            type="button"
                            onClick={loadMap}
                            disabled={mapLoading}
                            className="text-ntu-rose hover:underline font-medium disabled:opacity-50"
                        >
                            {mapLoading ? 'Loading map...' : 'Show map'}
                        </button>
                        {mapEmbedUrl && (
                            <div className="mt-4 aspect-video w-full rounded overflow-hidden border border-gray-200">
                                <iframe
                                    title="Event location"
                                    src={mapEmbedUrl}
                                    width="100%"
                                    height="100%"
                                    style={{ border: 0 }}
                                    allowFullScreen
                                    loading="lazy"
                                    referrerPolicy="no-referrer-when-downgrade"
                                />
                            </div>
                        )}
                    </section>

                    <section>
                        <div className="flex items-center justify-between flex-wrap gap-2">
                            <h2 className="text-xl font-semibold text-ntu-dark border-b border-ntu-rose/20 pb-2">Weather</h2>
                            <button
                                type="button"
                                onClick={loadWeather}
                                disabled={weatherLoading}
                                className="px-3 py-1.5 text-sm border border-gray-300 rounded-md text-ntu-dark hover:bg-gray-50 disabled:opacity-50"
                            >
                                {weatherLoading ? 'Loading...' : 'Check weather'}
                            </button>
                        </div>
                        {weather && (
                            <div className="text-ntu-tundora mt-3">
                                {weather.error ? (
                                    <p>{weather.error}</p>
                                ) : (
                                    <>
                                        <p>Temperature: {weather.temperatureCelsius != null ? `${weather.temperatureCelsius}°C` : '—'}</p>
                                        <p>Weather code: {weather.weatherCode != null ? weather.weatherCode : '—'}</p>
                                    </>
                                )}
                            </div>
                        )}
                    </section>
                </div>

                <div className="lg:col-span-1">
                    <section className="sticky top-24 border-l border-gray-200 pl-6">
                        <h2 className="text-xl font-semibold text-ntu-dark mb-4">
                            {eventStatus === 'past' ? 'Past event' : 'Register for event'}
                        </h2>
                        {currentStudent ? (
                            <div className="space-y-4">
                                <p className="text-sm text-ntu-tundora">
                                    Logged in as {currentStudent.name} ({currentStudent.studentId})
                                </p>
                                {publisherCannotRate && (
                                    <p className="text-sm text-ntu-tundora">You can not rate events published by you.</p>
                                )}
                                {showRatingForm && (
                                    <form onSubmit={handleSubmitRating} className="space-y-3">
                                        {alreadyRated && <p className="text-sm text-ntu-tundora">You have rated. You can change your rating below.</p>}
                                        <label className="block text-sm font-medium text-ntu-dark">Rate this event</label>
                                        <select
                                            value={ratingValue}
                                            onChange={(e) => setRatingValue(Number(e.target.value))}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                            disabled={ratingLoading}
                                        >
                                            <option value="1">1</option>
                                            <option value="2">2</option>
                                            <option value="3">3</option>
                                            <option value="4">4</option>
                                            <option value="5">5</option>
                                        </select>
                                        <button
                                            type="submit"
                                            className="w-full py-2.5 bg-ntu-rose text-white font-medium rounded-md hover:bg-ntu-pink disabled:opacity-50"
                                            disabled={ratingLoading}
                                        >
                                            {ratingLoading ? 'Submitting...' : ratingSubmitLabel}
                                        </button>
                                    </form>
                                )}
                                {canCancelRegistration && (
                                    <form onSubmit={handleCancelRegistration}>
                                        <button
                                            type="submit"
                                            className="w-full py-2.5 border border-amber-600 text-amber-700 font-medium rounded-md hover:bg-amber-50 disabled:opacity-50"
                                            disabled={loading}
                                        >
                                            {loading ? 'Cancelling...' : 'Cancel registration'}
                                        </button>
                                    </form>
                                )}
                                {!isRegistered && eventStatus !== 'past' && (
                                    <form onSubmit={handleRegister}>
                                        <button
                                            type="submit"
                                            className="w-full py-2.5 bg-ntu-rose text-white font-medium rounded-md hover:bg-ntu-pink disabled:opacity-50"
                                            disabled={loading || isFull}
                                        >
                                            {loading ? 'Registering...' : isFull ? 'Event full' : 'Register now'}
                                        </button>
                                    </form>
                                )}
                                {message && (
                                    <div
                                        className={`p-3 rounded text-sm ${message.includes('Success') || message.includes('cancelled') || message.includes('Rating submitted') ? 'bg-green-100 text-green-800' : 'bg-red-50 text-red-700'}`}
                                    >
                                        {message}
                                    </div>
                                )}
                            </div>
                        ) : (
                            <p className="text-sm text-ntu-tundora">
                                Load your student profile to register.
                            </p>
                        )}
                        <p className="mt-4 text-sm text-ntu-tundora">
                            Event ID: {event.eventId} · Publisher: {event.publisherId}
                        </p>
                    </section>
                </div>
            </div>
        </div>
    );
}