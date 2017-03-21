# redesigned-guacamole

Displays MQTT messages by storing them in Redis. Used to show what is being played on the Hackeriet chromecast.

## Building

Set environent

    MQTT_PASS=password
    MQTT_URL='tcp://localhost:15014'
    MQTT_USER=user
    REDIS_URL='redis://user:password@localhost:12879'

Then compile and run with `lein`

    lein run

## TODO

* Make pretty
* Store time
* Show arbitrary topics
* Pagination
