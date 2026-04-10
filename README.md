# Student Event Booking - follow this guide
## Top level architecture diagram
![toplevel-diagram](https://https://github.com/Haru-Tachibana/StudentEventBooking/blob/main/top-level%20diagram.png "Top level architecture diagram")

## 1. Set up your external API keys

To run **external APIs** (maps, weather, Skiddle), create a file at the **project root** named **`api-keys.properties`** (it is gitignored; do not commit it). Add these three key-value pairs with your own keys:

```properties
external.api.google.maps.key=your-google-maps-api-key
external.api.skiddle.key=your-skiddle-api-key
external.api.weather.key=your-openweathermap-api-key
```

- **Where:** project root (same folder as `pom.xml`).
- **Filename:** `api-keys.properties`.
- **Keys:** Get Google Maps key from Google Cloud Console; Skiddle key from [Skiddle](https://www.skiddle.com/api/); OpenWeatherMap key from [OpenWeather](https://openweathermap.org/api).

## 2. Run com.student.eventbooking.StudentEventBookingApplication

To build and run backend

## 3. Install and run the website

From the **project root**:

```bash
cd client-app
npm install
npm run dev
```

The app will be at **http://localhost:5173**. Ensure the backend is running (e.g. on port 9090) if you use the API.
