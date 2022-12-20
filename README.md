# Weather Tracking App
Weather application in Kotlin using Jetpack Compose

## Features
- Search for locations to track weather, the API will display results in an autocomplete textview
- Select location to view daily and hourly forecasts
- Add locations to a watch list. List has swipe to dismiss functionality and pull to refresh
- Display Daily and hourly forecast
- Settings menu to manipulate UI components and notification behavior

## Libraries
This application uses the following libraries
- Retrofit for web service calls
- Koin for dependency Injection
- Kotlinx Serialization for parsing JSON
- Room for database access
- Coil for image loading from API
- Preferences Datastore for settings backend with custom UI
- Workmanager for scheduling background API calls and sending notifications
- Google play location services for local forecast notifications


## Clean Architecture

The application is structured to follow the clean architecture design pattern. Inner layers such as
the domain and data/use cases are located in the core module.
The outer framework and presentation layers reside in the app module utilizing the MVVM pattern with flow

Some Highlights include:

-Reactive UIs using Flow and coroutines for asynchronous operations.
- User Interface built using Jetpack Compose
- A single-activity architecture, using Jetpack navigation.
- A presentation layer that contains a screen (View) and a ViewModel per screen (or feature).
- A data layer with a repository. Repositories abstracted using interfaces

## Testing
- Navigation and settings tests located under the androidTest Directory 


![alt text]([http://url/to/img.png](https://play-lh.googleusercontent.com/2uEutGCYAEupu4gfrGQVRahFyWgGdRiuxXjTheIHn4s52OMiVkrT3mALI23zgu98y14=w2560-h1440-rw))


