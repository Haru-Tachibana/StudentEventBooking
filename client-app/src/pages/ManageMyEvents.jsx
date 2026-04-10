import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../utils/api';
import { getEventStatus } from '../utils/eventUtils';

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('en-GB', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
    });
}

function formatRating(event) {
    const avg = event.averageRating;
    const count = event.ratingCount;
    if (avg == null && (count == null || count === 0)) return null;
    const n = count != null && count > 0 ? count : 0;
    if (n === 0) return null;
    const rating = avg != null ? Number(avg).toFixed(1) : '—';
    return `★ ${rating} (${n} rating${n !== 1 ? 's' : ''})`;
}

export function ManageMyEvents() {
    const [published, setPublished] = useState([]);
    const [registrations, setRegistrations] = useState([]);
    const [currentStudent, setCurrentStudent] = useState(null);
    const [pendingRatingEventIds, setPendingRatingEventIds] = useState(new Set());
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState({ type: '', text: '' });
    const [actionLoading, setActionLoading] = useState(null);
    const [ratingLoading, setRatingLoading] = useState(null);
    const [ratingValue, setRatingValue] = useState({});

    async function loadAll() {
        setLoading(true);
        try {
            const [myEvents, myRegs, student] = await Promise.all([
                api.getMyEvents(),
                api.getMyRegistrations(),
                api.getCurrentStudent(),
            ]);
            setPublished(Array.isArray(myEvents) ? myEvents : []);
            setRegistrations(Array.isArray(myRegs) ? myRegs : []);
            setCurrentStudent(student || null);
            if (student && student.studentId) {
                const pending = await api.getPendingRatings(student.studentId);
                setPendingRatingEventIds(new Set((pending || []).map((e) => e.eventId)));
            } else {
                setPendingRatingEventIds(new Set());
            }
        } catch (err) {
            setMessage({ type: 'error', text: err.message || 'Failed to load data' });
            setPublished([]);
            setRegistrations([]);
            setPendingRatingEventIds(new Set());
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadAll();
    }, []);

    function showSuccess(text) {
        setMessage({ type: 'success', text });
    }

    function showError(text) {
        setMessage({ type: 'error', text });
    }

    function handleDelete(eventId) {
        if (!window.confirm('Are you sure you want to delete this event? This cannot be undone.')) return;
        setActionLoading(eventId);
        api.deleteEvent(eventId)
            .then(() => {
                showSuccess('Event deleted.');
                loadAll();
            })
            .catch((err) => showError(err.message || 'Failed to delete event'))
            .finally(() => setActionLoading(null));
    }

    function handleCancelRegistration(eventId) {
        setActionLoading(eventId);
        api.cancelRegistration(eventId)
            .then(() => {
                showSuccess('Registration cancelled.');
                loadAll();
            })
            .catch((err) => showError(err.message || 'Failed to cancel registration'))
            .finally(() => setActionLoading(null));
    }

    function handleSubmitRating(eventId) {
        const value = ratingValue[eventId];
        if (value == null || value < 1 || value > 5) {
            showError('Please select a rating from 1 to 5.');
            return;
        }
        if (!currentStudent || !currentStudent.studentId) {
            showError('You must be logged in to rate.');
            return;
        }
        setRatingLoading(eventId);
        api.submitRating(eventId, { studentId: currentStudent.studentId, rating: Number(value) })
            .then(() => {
                showSuccess('Rating saved.');
                setPendingRatingEventIds((prev) => {
                    const next = new Set(prev);
                    next.delete(eventId);
                    return next;
                });
                setRatingValue((prev) => {
                    const next = { ...prev };
                    delete next[eventId];
                    return next;
                });
            })
            .catch((err) => showError(err.message || 'Failed to submit rating'))
            .finally(() => setRatingLoading(null));
    }

    if (loading) {
        return (
            <div className="text-center py-12 text-ntu-tundora">Loading...</div>
        );
    }

    return (
        <div>
            <div className="mb-8">
                <h1 className="text-2xl font-semibold text-ntu-dark mb-2">Manage my events</h1>
                <p className="text-ntu-tundora">Review and edit events you published, upcoming events you registered, and rate past events you attended.</p>
            </div>

            {message.text && (
                <div
                    className={`mb-6 p-4 rounded text-sm ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-50 text-red-700'}`}
                >
                    {message.text}
                </div>
            )}

            <section className="mb-10">
                <h2 className="text-xl font-semibold text-ntu-dark mb-4 border-b border-ntu-rose/20 pb-2">Events published by me</h2>
                {(() => {
                    const upcomingPublished = published.filter((e) => getEventStatus(e) !== 'past');
                    if (upcomingPublished.length === 0) {
                        return <p className="text-ntu-tundora">You have no upcoming or current events that you published.</p>;
                    }
                    return (
                        <ul className="space-y-4">
                            {upcomingPublished.map((event) => (
                                <li
                                    key={event.eventId}
                                    className="bg-white rounded-lg border border-gray-200 p-4 flex flex-wrap items-center justify-between gap-3"
                                >
                                    <div className="min-w-0 flex-1">
                                        <Link
                                            to={`/event/${event.eventId}`}
                                            className="font-semibold text-ntu-dark hover:text-ntu-rose"
                                        >
                                            {event.title}
                                        </Link>
                                        <p className="text-sm text-ntu-tundora mt-1">
                                            {formatDate(event.date)} · {event.time} · {event.venue}
                                        </p>
                                        <p className="text-sm text-ntu-tundora">
                                            {event.attendees?.length || 0} / {event.maxParticipants} attendees
                                        </p>
                                        {formatRating(event) && (
                                            <p className="text-sm text-ntu-pink font-medium mt-1">{formatRating(event)}</p>
                                        )}
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <Link
                                            to={`/event/${event.eventId}/edit`}
                                            className="px-4 py-2 border border-ntu-rose text-ntu-rose rounded-md hover:bg-ntu-rose/5 font-medium"
                                        >
                                            Edit
                                        </Link>
                                        <button
                                            type="button"
                                            onClick={() => handleDelete(event.eventId)}
                                            disabled={actionLoading === event.eventId}
                                            className="px-4 py-2 bg-ntu-pink text-white rounded-md hover:bg-ntu-pink font-medium disabled:opacity-50"
                                        >
                                            {actionLoading === event.eventId ? 'Deleting...' : 'Delete'}
                                        </button>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    );
                })()}
            </section>

            <section className="mb-10">
                <h2 className="text-xl font-semibold text-ntu-dark mb-4 border-b border-ntu-rose/20 pb-2">My upcoming events</h2>
                {(() => {
                    const upcomingRegistrations = registrations.filter((e) => getEventStatus(e) !== 'past');
                    if (upcomingRegistrations.length === 0) {
                        return <p className="text-ntu-tundora">You are not registered for any upcoming or current events.</p>;
                    }
                    return (
                        <ul className="space-y-4">
                            {upcomingRegistrations.map((event) => (
                                <li
                                    key={event.eventId}
                                    className="bg-white rounded-lg border border-gray-200 p-4 flex flex-wrap items-center justify-between gap-3"
                                >
                                    <div className="min-w-0 flex-1">
                                        <Link
                                            to={`/event/${event.eventId}`}
                                            className="font-semibold text-ntu-dark hover:text-ntu-rose"
                                        >
                                            {event.title}
                                        </Link>
                                        <p className="text-sm text-ntu-tundora mt-1">
                                            {formatDate(event.date)} · {event.time} · {event.venue}
                                        </p>
                                        <p className="text-sm text-ntu-tundora">
                                            {event.attendees?.length || 0} / {event.maxParticipants} attendees
                                        </p>
                                        {formatRating(event) && (
                                            <p className="text-sm text-ntu-pink font-medium mt-1">{formatRating(event)}</p>
                                        )}
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <button
                                            type="button"
                                            onClick={() => handleCancelRegistration(event.eventId)}
                                            disabled={actionLoading === event.eventId}
                                            className="px-4 py-2 border accent-ntu-pink text-ntu-pink rounded-md hover:bg-white font-medium disabled:opacity-50"
                                        >
                                            {actionLoading === event.eventId ? 'Cancelling...' : 'Cancel registration'}
                                        </button>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    );
                })()}
            </section>

            <section>
                <h2 className="text-xl font-semibold text-ntu-dark mb-4 border-b border-ntu-rose/20 pb-2">My past events</h2>
                {(() => {
                    const pastPublished = published.filter((e) => getEventStatus(e) === 'past');
                    const pastAttended = registrations.filter((e) => getEventStatus(e) === 'past');
                    const seen = new Set();
                    const pastEvents = [];
                    for (const e of pastAttended) {
                        if (!seen.has(e.eventId)) {
                            seen.add(e.eventId);
                            pastEvents.push({ event: e, attended: true });
                        }
                    }
                    for (const e of pastPublished) {
                        if (!seen.has(e.eventId)) {
                            seen.add(e.eventId);
                            pastEvents.push({ event: e, attended: false });
                        }
                    }
                    pastEvents.sort((a, b) => {
                        const dA = new Date(a.event.date + 'T' + a.event.time).getTime();
                        const dB = new Date(b.event.date + 'T' + b.event.time).getTime();
                        return dB - dA;
                    });
                    if (pastEvents.length === 0) {
                        return <p className="text-ntu-tundora">No past events.</p>;
                    }
                    return (
                        <ul className="space-y-4">
                            {pastEvents.map(({ event, attended }) => {
                                const isPublisher = currentStudent && event.publisherId === currentStudent.studentId;
                                const canRate = !isPublisher && attended && pendingRatingEventIds.has(event.eventId);
                                const alreadyRated = !isPublisher && attended && !pendingRatingEventIds.has(event.eventId);
                                const showRatingForm = !isPublisher && attended;
                                let ratingButtonLabel = 'Rate this event';
                                if (alreadyRated) {
                                    ratingButtonLabel = 'Update rating';
                                }
                                return (
                                    <li
                                        key={event.eventId}
                                        className="bg-white rounded-lg border border-gray-200 p-4 flex flex-wrap items-center justify-between gap-3"
                                    >
                                        <div className="min-w-0 flex-1">
                                            <Link
                                                to={`/event/${event.eventId}`}
                                                className="font-semibold text-ntu-dark hover:text-ntu-rose"
                                            >
                                                {event.title}
                                            </Link>
                                            <p className="text-sm text-ntu-tundora mt-1">
                                                {formatDate(event.date)} · {event.time} · {event.venue}
                                            </p>
                                            <p className="text-sm text-ntu-tundora">
                                                {event.attendees?.length || 0} / {event.maxParticipants} attendees
                                            </p>
                                            {formatRating(event) && (
                                                <p className="text-sm text-ntu-pink font-medium mt-1">{formatRating(event)}</p>
                                            )}
                                        </div>
                                        <div className="flex items-center gap-2">
                                            {isPublisher ? (
                                                <span className="text-sm text-ntu-tundora">You can not rate events published by you.</span>
                                            ) : showRatingForm ? (
                                                <>
                                                    {alreadyRated && <span className="text-sm text-ntu-tundora">You have rated. Change below:</span>}
                                                    <select
                                                        value={ratingValue[event.eventId] ?? ''}
                                                        onChange={(e) => setRatingValue((prev) => ({ ...prev, [event.eventId]: e.target.value }))}
                                                        className="px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                                        disabled={ratingLoading === event.eventId}
                                                    >
                                                        <option value="">Rate 1-5</option>
                                                        <option value="1">1</option>
                                                        <option value="2">2</option>
                                                        <option value="3">3</option>
                                                        <option value="4">4</option>
                                                        <option value="5">5</option>
                                                    </select>
                                                    <button
                                                        type="button"
                                                        onClick={() => handleSubmitRating(event.eventId)}
                                                        disabled={ratingLoading === event.eventId}
                                                        className="px-4 py-2 bg-ntu-rose text-white rounded-md hover:bg-ntu-pink font-medium disabled:opacity-50"
                                                    >
                                                        {ratingLoading === event.eventId ? 'Submitting...' : ratingButtonLabel}
                                                    </button>
                                                </>
                                            ) : null}
                                        </div>
                                    </li>
                                );
                            })}
                        </ul>
                    );
                })()}
            </section>
        </div>
    );
}