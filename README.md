# heroku-kafka-spring
Small example to connect to a Heroku hosted Kafka cluster with SSL.

On start-up connects to the specified cluster defined in application.properties using the SSL cert info also defined in these variables.

On a Heroku app these environment variables will be exposed if you have provisioned or attached a Heroku Kafka addon.

Then it will produce a few messages and start a consumer on the same topic that will log the received messages.
