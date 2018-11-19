#Destined Glory
A very pre-alpha stat tracker for your PvP performance in the video game Destiny by
 [Bungie Software](https://www.bungie.net/).

## Goals 
* Experiment with full-stack applications in 100% Kotlin
* Able to share view-models and other code between frontend and backend (not there yet)
* Draw a pretty chart of your Crucible progress over time (have JSON, will chart in vNext) 

## Tech
This web application has a kotlin frontend in the /frontend folder 
and a kotlin backend in the /backend folder. Aside from the Kotlin standard library the main 
dependency is [KTOR library](https://ktor.io/) which facilitates most aspects of HTTP communication. 

## How To Run
Place a secrets.properties file containing your bungie API key in the backend folder with
contents like these:
```properties
bungieapikey=<put a real API key here>
```
Then use Gradle to build and run:
```
./gradlew run
```
The server will start at http://localhost:8008/
