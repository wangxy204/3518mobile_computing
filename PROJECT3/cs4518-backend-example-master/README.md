# Updated Servlet For App Engine Flexible Environment
I just upgraded the old version of [this repo](https://github.com/GoogleCloudPlatform/firebase-appengine-backend)
You still need to follow the instruction from this[page](https://cloud.google.com/solutions/mobile/mobile-firebase-app-engine-flexible)
Don't need to worry about dependency of google cloud storage. I already include it in pom.xml.

## Requirements
* [Apache Maven](http://maven.apache.org) 3.3.9 or greater
* [Google Cloud SDK](https://cloud.google.com/sdk/) support latest version 180.*
* `gcloud components install app-engine-java`

## Setup

Use either:

* `gcloud init`
* `gcloud auth application-default login`

Change path in web.xml:

* init-param credential
* init-param databaseUrl

Put you credential json file in WEB-INF



### Running locally

    $ mvn jetty:run-exploded
  
### Deploying

    $ mvn appengine:deploy
   
### Tips: How to setup Google Cloud Storage in Java
very similar to Firebase

    $ Storage storage = StorageOptions
    .newBuilder()
    .setCredentials(ServiceAccountCredentials.fromStream(config.getServletContext().getResourceAsStream(credential)))
    .build()
    .getService()


