## Introduction

This project is an Android application that integrates with various APIs to provide a 
dynamic and interactive dashbord for the software and game developmet class. The app collects data
from sources related to the class to convienently display important information like daily activities, student tracker information
, and due dates to be easily availible on the CleverTouch class display.

This README is aimed at **developers** who want to understand the code structure and functionality. 
Users should refer to the project wiki for usage instructions.
 The README is organized into sections explaining the purpose and functionality of important files in the application,
along with detailed code explanations for each function.

### Project Description
This section aims to clearly define the preblem the project is trying to solve and how it acomplishes it
#### Problem
Software and Game Development needs a better way to view information related to
daily routienes in the class.
#### Solution
Develop a app to run on the CleverTouch which can dynamically get information from the 
student tracker, Spotify, and Google API's to display on the screen. also develop a web UI to
interact with the app and host the web interface on the Clevertouch.


## Key Features
- **MainActivity**: Manages the core user interface and app functionality.
- **WebServer (API)**: Handles all API requests and manages server communication.
- **Utils**: Utility functions for common operations throughout the app.
- **GoogleAPIManager**: Integrates Google APIs, including Google Calendar and Google Slides.
- **SpotifyManager**: Manages interactions with the Spotify API for playing music.
- **StudentTracker**: Tracks and displays student check-in/out data and other relevant info.
- **Web Homepage**: Provides a web-based dashboard for students to interact with.
- **AdminDash**: Admin interface for managing app settings, including restarting the app in case of failures.
- **DBPannel**: Manages database interactions and provides a panel for viewing and managing data.

## Installation
To set up and run the project:
1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
2. Navigate to the project folder:
   ```bash
   cd <project-folder>
   ```
3. Open the project in Android Studio.


## Getting Started

1. Run the project on a compatible device (Android or emulator).

2. Follow the setup instructions for the required APIs (Google, Spotify, etc.).

3. Launch the app to see the main interface, and access the web homepage or admin dashboard as needed.

## Documentation

For detailed documentation on each component, check the following files:

- [Variables](docs/Variables.md)
- [MainActivity](docs/MainActivity.md)
- [WebServer (API)](docs/WebServer.md)
- [Utils](docs/Utils.md)
- [GoogleAPIManager](docs/GoogleAPIManager.md)
- [SpotifyManager](docs/SpotifyManager.md)
- [StudentTracker](docs/StudentTracker.md)
- [Web Homepage](docs/WebHomepage.md)
- [AdminDash](docs/AdminDash.md)
- [DBPannel](docs/DBPannel.md)