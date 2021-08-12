# GeoLocator

### Mandatory requirements

For further reference, please consider the following sections:

* It should be a simple java application based on Spring Boot framework.
* The application should have a REST API that allows users to search by address and get all addresses by coordinates.
* The result should be printed in JSON format.
* Your code should be version controlled and publicly accessible for us to review (github/bit-bucket/gitlab/etc)

### Nice to have

* Implement Authorization/Authentication for result endpoints.
* Implement Cache for storing very often using coordinates.
* Deploy application to Docker container (Dockerfile artifact).

### Task description:

* You need to implement application that uses third party api [Nominatim API](https://nominatim.org/release-docs/develop/api/Overview/) and create the following endpoints:
* To search by address and save coordinates(latitude,longitude) in H2 DB or any Persistence Db
* To get all addresses by coordinates that have been stored in DB. The application must be called by REST API for receiving all addresses from [Nominatim API](https://nominatim.org/release-docs/develop/api/Overview/)  by saved coordinates.

### Solution

#### Technologies:

- Java 15
- Spring Boot 2.5
- H2 database
- Lombok

#### Implementation and Settings:

* Implemented fetching data from [Nominatim API](https://nominatim.org/release-docs/develop/api/Overview/) using json, jsonv2, geojson and geocodejson formats 
* For data fetching from Nominatim program uses only [Nominatim /search](https://nominatim.org/release-docs/develop/api/Search/) and [Nominatim /lookup](https://nominatim.org/release-docs/develop/api/Lookup/) 
  because [Nominatim /reverse](https://nominatim.org/release-docs/develop/api/Reverse/) returns an imprecise, approximate result.  
  And lookup for a single OSM object by its OSM id using [Nominatim /reverse](https://nominatim.org/release-docs/develop/api/Reverse/) is deprecated now by Nominatim.  
  Instead of that, program generates OSM ID's using data in Nominatim response jsons and uses them to search by [Nominatim /lookup](https://nominatim.org/release-docs/develop/api/Lookup/)  
  Client must not generate these OSM ID's itself, but can place  JSON request body in into request.   
  This JSON request body must contain only points of coordinates in such format:  
  `[{"lat": 52.444761,"lon": 13.4002923 }, {"lat": 52.470678199999995,"lon": 13.385307250760587 }, {"lat": 52.4553875,"lon": 13.384646 }]`  
  Client also can send requests with empty or null set of points to receive all addresses by all stored coordinates.  
  But this option is available only for users with `ADMIN` role
* All response json data converted to own simple json, jsonv2, geojson and geocodejson formats (removed some metadata)  
* Output of json, jsonv2 and geojson now is similar except of geocodejson.   
  Therefore `rating-cache.savememory` property was set as `true`. This option prevents from caching same data.   
  But if needs to change output data and make different formats for json, jsonv2 and geojson then you have to set `rating-cache.savememory` property as `false`  
* Cache can work in `multi-thread` and `single-thread` modes. `multi-thread` mode is thread-safe, but it consumes more memory than `single-thread`.   
  Set necessary active profile in `application.yaml` to change this mode.   
* You can also make fine-tuning of cache by changing `rating-cache` params
* If you want to change H2 DB settings, you have to change `datasource` params also in `application.yaml` property file

#### How to test:

* Run application -> use `GeoLocator.postman_collection.json` to test for base cases in Postman
* If you want to create custom requests or `curl`'s -> please use user and admin credentials from `data.sql` file