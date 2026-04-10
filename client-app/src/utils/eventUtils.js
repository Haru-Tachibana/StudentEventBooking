/**
 * Classify event by comparing (start + duration) with real-world current time.
 * @param {{ date: string, time: string, duration: number }} event - date YYYY-MM-DD, time HH:mm, duration minutes
 * @returns {'past'|'current'|'upcoming'}
 */
export function getEventStatus(event) {
    if (!event || !event.date || !event.time || event.duration == null) {
        return 'upcoming';
    }
    const start = new Date(event.date + 'T' + event.time);
    if (isNaN(start.getTime())) {
        return 'upcoming';
    }
    const end = new Date(start.getTime() + event.duration * 60000);
    const now = Date.now();
    if (now > end) {
        return 'past';
    }
    if (now >= start.getTime()) {
        return 'current';
    }
    return 'upcoming';
}