Example result of OpenWeatherMap.

- `sunrise` and `sunset` are unix format UTC; so same as `java.util.Date`
- `timezone` shift in seconds from UTC

```json
{
    "coord": {
        "lon": 4.7009,
        "lat": 50.8796
    },
    "weather": [
        {
            "id": 500,
            "main": "Rain",
            "description": "light rain",
            "icon": "10n"
        }
    ],
    "base": "stations",
    "main": {
        "temp": 283.28,
        "feels_like": 282.7,
        "temp_min": 282.47,
        "temp_max": 284.13,
        "pressure": 1015,
        "humidity": 90
    },
    "visibility": 10000,
    "wind": {
        "speed": 4.12,
        "deg": 290
    },
    "rain": {
        "1h": 0.13
    },
    "clouds": {
        "all": 100
    },
    "dt": 1663356000,
    "sys": {
        "type": 2,
        "id": 2010413,
        "country": "BE",
        "sunrise": 1663305469,
        "sunset": 1663350880
    },
    "timezone": 7200,
    "id": 2792482,
    "name": "Leuven",
    "cod": 200
}```