* Why are you building custom cache instead of using some in-memory db (Redis, Memcached, etc)
* Ideally docker in development builds application inside container. This way allows to not install any development tools when you develop. Can you describe steps, how to implement it.
* Why you are using spring.jpa.open-in-view=false in configuration?  Why developers forced to set it? Why it is antipattern? (Same questions for defer-datasource-initialization: true)
* Tests... What tests MUST be written (at least) 