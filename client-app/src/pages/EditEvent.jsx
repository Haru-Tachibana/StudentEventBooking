import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../utils/api';

const TYPES = ['Workshop', 'Seminar', 'Career', 'Sports', 'Cultural', 'Social'];
const POSTCODE_REGEX = /^[A-Z]{1,2}\d{1,2}[A-Z]?\s?\d[A-Z]{2}$/i;

export function EditEvent() {
    const { eventId } = useParams();
    const navigate = useNavigate();
    const [form, setForm] = useState({
        eventId: '',
        publisherId: '',
        title: '',
        type: '',
        date: '',
        time: '',
        duration: '',
        location: '',
        venue: '',
        cost: '',
        maxParticipants: '',
        description: '',
    });
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const [pageLoading, setPageLoading] = useState(true);

    useEffect(() => {
        if (!eventId) {
            setPageLoading(false);
            return;
        }
        api.getEvent(eventId)
            .then((event) => {
                setForm({
                    eventId: event.eventId || '',
                    publisherId: event.publisherId || '',
                    title: event.title || '',
                    type: event.type || '',
                    date: event.date || '',
                    time: event.time || '',
                    duration: event.duration != null ? String(event.duration) : '',
                    location: event.location || '',
                    venue: event.venue || '',
                    cost: event.cost != null ? String(event.cost) : '',
                    maxParticipants: event.maxParticipants != null ? String(event.maxParticipants) : '',
                    description: event.description || '',
                });
            })
            .catch(() => setMessage('Event not found or you do not have permission to edit it.'))
            .finally(() => setPageLoading(false));
    }, [eventId]);

    const handleChange = (field, value) => {
        setForm((prev) => ({ ...prev, [field]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const {
            title, type, date, time, duration,
            location, venue, cost, maxParticipants, description,
        } = form;
        if (!title || !type || !date || !time || !duration || !location || !venue || !description) {
            setMessage('All required fields must be filled');
            return;
        }
        if (!POSTCODE_REGEX.test(location.replace(/\s/g, ''))) {
            setMessage('Please enter a valid UK postcode (e.g. NG1 1AA)');
            return;
        }
        const costNum = parseFloat(cost);
        const maxNum = parseInt(maxParticipants, 10);
        const durNum = parseInt(duration, 10);
        if (isNaN(costNum) || costNum < 0) {
            setMessage('Cost must be a valid number');
            return;
        }
        if (isNaN(maxNum) || maxNum < 1) {
            setMessage('Max participants must be at least 1');
            return;
        }
        if (isNaN(durNum) || durNum < 1) {
            setMessage('Duration must be at least 1 minute');
            return;
        }
        setLoading(true);
        setMessage('');
        try {
            await api.updateEvent(eventId, {
                eventId: form.eventId,
                publisherId: form.publisherId,
                title,
                type,
                date,
                time,
                duration: durNum,
                location: location.trim().toUpperCase(),
                venue,
                cost: costNum,
                maxParticipants: maxNum,
                description,
            });
            setMessage('Event updated successfully.');
            setTimeout(() => navigate('/manage-events'), 1500);
        } catch (err) {
            setMessage(err.message || 'Failed to update event. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    if (pageLoading) {
        return (
            <div className="text-center py-12 text-ntu-tundora">Loading event...</div>
        );
    }

    if (!form.eventId) {
        return (
            <div className="max-w-3xl mx-auto">
                <p className="text-ntu-tundora mb-4">{message || 'Event not found.'}</p>
                <button
                    type="button"
                    onClick={() => navigate('/manage-events')}
                    className="px-4 py-2 bg-ntu-rose text-white rounded-md hover:bg-ntu-pink"
                >
                    Back to Manage my events
                </button>
            </div>
        );
    }

    return (
        <div className="max-w-3xl mx-auto">
            <div className="mb-8">
                <h1 className="text-2xl font-semibold text-ntu-dark mb-2">Edit Event</h1>
                <p className="text-ntu-tundora">Update event details.</p>
            </div>
            <div className="bg-white rounded-lg border border-gray-200 p-8">
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-ntu-dark mb-2">Publisher (you)</label>
                            <input
                                value={form.publisherId}
                                readOnly
                                className="w-full px-3 py-2 border border-gray-200 rounded-md text-ntu-tundora bg-gray-50"
                            />
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-ntu-dark mb-2">Event Title *</label>
                        <input
                            placeholder="e.g. Java Workshop 01"
                            value={form.title}
                            onChange={(e) => handleChange('title', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                            disabled={loading}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-ntu-dark mb-2">Event Type *</label>
                        <select
                            value={form.type}
                            onChange={(e) => handleChange('type', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                            disabled={loading}
                        >
                            <option value="">Select type</option>
                            {TYPES.map((t) => (
                                <option key={t} value={t}>{t}</option>
                            ))}
                        </select>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-ntu-dark mb-2">Date *</label>
                            <input
                                type="date"
                                value={form.date}
                                onChange={(e) => handleChange('date', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                disabled={loading}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-ntu-dark mb-2">Time *</label>
                            <input
                                type="time"
                                value={form.time}
                                onChange={(e) => handleChange('time', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                disabled={loading}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-ntu-dark mb-2">Duration (min) *</label>
                            <input
                                type="number"
                                min="1"
                                placeholder="120"
                                value={form.duration}
                                onChange={(e) => handleChange('duration', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                disabled={loading}
                            />
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-ntu-dark mb-2">Venue *</label>
                        <input
                            placeholder="e.g. MAE 205, Clifton Campus"
                            value={form.venue}
                            onChange={(e) => handleChange('venue', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                            disabled={loading}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-ntu-dark mb-2">UK Postcode *</label>
                        <input
                            placeholder="e.g. NG11 8NS"
                            value={form.location}
                            onChange={(e) => handleChange('location', e.target.value.toUpperCase())}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                            disabled={loading}
                        />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-ntu-dark mb-2">Cost (£) *</label>
                            <input
                                type="number"
                                min="0"
                                step="0.01"
                                placeholder="0"
                                value={form.cost}
                                onChange={(e) => handleChange('cost', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                disabled={loading}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-ntu-dark mb-2">Max Participants *</label>
                            <input
                                type="number"
                                min="1"
                                placeholder="50"
                                value={form.maxParticipants}
                                onChange={(e) => handleChange('maxParticipants', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                disabled={loading}
                            />
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-ntu-dark mb-2">Description *</label>
                        <textarea
                            placeholder="Detailed information about the event..."
                            value={form.description}
                            onChange={(e) => handleChange('description', e.target.value)}
                            rows={6}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark resize-none"
                            disabled={loading}
                        />
                    </div>
                    <div className="flex gap-3">
                        <button
                            type="submit"
                            className="flex-1 py-2.5 bg-ntu-rose text-white font-medium rounded-md hover:bg-ntu-pink disabled:opacity-50"
                            disabled={loading}
                        >
                            {loading ? 'Saving...' : 'Save changes'}
                        </button>
                        <button
                            type="button"
                            onClick={() => navigate('/manage-events')}
                            className="px-4 py-2.5 border border-gray-300 rounded-md text-ntu-dark hover:bg-gray-50 font-medium"
                        >
                            Cancel
                        </button>
                    </div>
                    {message && (
                        <div
                            className={`p-4 rounded text-sm ${message.toLowerCase().includes('success') ? 'bg-green-100 text-green-800' : 'bg-red-50 text-red-700'}`}
                        >
                            {message}
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
}