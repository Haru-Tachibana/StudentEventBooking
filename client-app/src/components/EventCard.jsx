import { Link } from 'react-router-dom';

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('en-GB', {
        weekday: 'short',
        month: 'short',
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

export function EventCard({ event }) {
    const spots = event.maxParticipants - (event.attendees?.length || 0);
    const isFull = spots === 0;

    return (
        <Link to={`/event/${event.eventId}`}>
            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden hover:shadow-lg hover:border-gray-300 transition-all cursor-pointer group">
                {event.imageUrl && (
                    <div className="w-full h-48 overflow-hidden">
                        <img
                            src={event.imageUrl}
                            alt={event.title}
                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        />
                    </div>
                )}
                <div className="p-5">
                    <div className="flex items-center gap-2 mb-3">
            <span className="text-xs px-2.5 py-1 bg-ntu-rose/10 text-ntu-rose rounded-full font-medium">
              {event.type}
            </span>
                        {event.cost === 0 && (
                            <span className="text-xs px-2.5 py-1 bg-green-100 text-green-800 rounded-full font-medium">
                Free
              </span>
                        )}
                    </div>
                    <h3 className="font-semibold text-lg text-ntu-dark mb-3 line-clamp-2 group-hover:text-ntu-rose transition-colors">
                        {event.title}
                    </h3>
                    <div className="flex gap-2 text-sm text-ntu-tundora mb-2">
                        <span>{formatDate(event.date)} · {event.time}</span>
                    </div>
                    <div className="text-sm text-ntu-tundora mb-2">{formatDuration(event.duration)}</div>
                    <div className="text-sm text-ntu-tundora truncate mb-2">{event.venue}</div>
                    <div className="text-sm text-ntu-tundora mb-2">
                        {event.attendees?.length || 0} / {event.maxParticipants} attendees
                    </div>
                    {event.cost > 0 && (
                        <div className="text-sm text-ntu-tundora">£{Number(event.cost).toFixed(2)}</div>
                    )}
                    <div className="mt-4 pt-4 border-t border-gray-200">
                        {isFull ? (
                            <span className="text-sm text-red-600 font-medium">Event Full</span>
                        ) : (
                            <span className="text-sm text-ntu-tundora">{spots} spots remaining</span>
                        )}
                    </div>
                </div>
            </div>
        </Link>
    );
}