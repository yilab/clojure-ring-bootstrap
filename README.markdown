# ring-bootstrap

Ring bootstrap is an internal simgle core inclusion at Crowdflower to bring ring-based
service in line with the functionality provided by other API toolkits
like Yammer's DropWizard. It does this by exposing functions to make
the following trivial:

1. Add metrics to your API handlers, via clj-metrics and the ring middleware.
2. Add a page for health checks to your handler, so that external clients can
   instrument you and detect misconfigurations and failures without grepping logs.
3. Register your provider into a zookeeper cluster at an arbitrary
   path point. This makes it easy for clients to your service
   providers and allows for rolling restarts.

## Usage

First, include the library as a dependency. In your `project.clj` file
add the dependency:

        ["com.crowdflower/clojure-ring-bootstrap" "0.1.0"]

Then when you register a handler you can add `/metrics/` and
`/healthcheck/` responders with the middleware function
`bootstrap-middleware` found in `clojure-ring-bootstrap.core`.
Please note that these are folded into your actual ring handler, so
they are probably not appropriate for exposing to external clients.

### Adding Healthchecks and Metrics

`bootstrap-middleware` takes three key arguments, but the most
important is `:healthchecks`, which takes a map containing healthcheck
names and healthcheck functions.

E.g.,

    (defhandler my-api ...)
    (def app (handler/api (bootstrap-middleware my-api
                                                :healthchecks {
                                                    :check-database (fn [] ...)
                                                    :check-perms    (fn [] ...)})))


Your endpoint will then respond to '/metrics/' and '/healthchecks/'.

### Healthcheck Output

Every time that the `/healthcheck/` endpoint is hit, it calls each of your named healthcheck
functions and reports their output. If any of the healthchecks return false or throw an uncaught
exception, they "fail" and the page will report a status code `500`. The contents of the page also reflect
the output, making the healthchecks suitable for human inspection. If all tests pass, it reports a `200`.


    > curl -v http://localhost:8080/healthcheck/
    * About to connect() to localhost port 8080 (#0)
    *   Trying ::1... connected
    * Connected to localhost (::1) port 8080 (#0)
    > GET /healthcheck/ HTTP/1.1
    > User-Agent: curl/7.21.4
    > Host: localhost:8080
    > Accept: */*
    >
    < HTTP/1.1 200 OK
    < Date: Fri, 03 Aug 2012 00:51:15 GMT
    < Content-Type: text/html;charset=ISO-8859-1
    < Content-Length: 255
    < Server: Jetty(7.6.1.v20120215)
    <
    <!DOCTYPE html>
    * Connection #0 to host localhost left intact
    * Closing connection #0
    <html><header><title>Healthchecks</title></header>
    <body><section><p>The following healthchecks have been registered:</p>
    <dl><dt class="check_name">successful</dt>
        <dd class="check_result">
          <span class="success">success</span>
    </dd></dl></section></body></html>

### Metrics Output

For `/metrics/` you should expect output like:


    > curl http://localhost/metrics/
    {"ring.responses.rate.4xx":{"type":"meter","rates":{"1":0.015991117074135343,"
    5":0.0033057092356765017,"15":0.0011080303990206543}},"ring.responses.rate.5xx":
    {"type":"meter","rates":{"1":0.0,"5":0.0,"15":0.0}},"ring.handling-time.DELETE": ...

For a full treatment on this output, please refer to the
[metrics-clojure documentation](http://metrics-clojure.readthedocs.org/en/latest/index.html).


### Zookeeper Registration (Jetty only)

At Crowdflower, we try to follow a Boundary-inspiried policy of registering internal service providers into
zookeeper, and using that data to connect rather than providing a proxy for every internal service. This
allows us to do rolling restarts and allows clients to make intelligent decisions about which service
to connect to.

Our Zookeeper registration code is not ring middleware but rather a Jetty configuratior. It registers
a lifecycle observer so that the zookeeper registration is removed *before* graceful shutdown is complete.

You can register a jetty service at server creation time like so:

    (defn launch-site [handler port]
       (let [configurator (zk-service-configurator :zk-connect "zk1:2181,zk2:2182,zk3:2181"
                                                   :path "/path/in/zookeeper"
                                                   :contents {"version" "0.1.0"})]
         (ring/run-jetty handler {:port port :configurator configurator})))

This will register a sequential, ephemeral node in zookeeper `/path/in/zookeeper/provider-000001`.
The data attribute of the node will be a json representation of your contents hash merged with
the  default keys, "hostname", "ports", and "port". Port is the preferred binding, but in some cases multiple
ports may be exposed and "ports" captures that. These details should be sufficient to allow a client to
connect.

In some cases the default behavior of the configurator to determine your machine's hostname. In this
case, a static string or fn may be passed to `zk-service-configurator` under the key `:hostname` to
override the default behavior.

Please be advised that this feature is not quite 100% robust, in that we do not properly handle
connection losses to zookeeper at the time of the 0.1.0 release. This shortcoming should be corrected.

## Planned Features

We're working to improve this library. On our roadmap are the following features:

0. Testing is immanent. (Sometimes you just gotta write a README, you know?)
1. Allow failing healthchecks to remove or re-add the system from zookeeper.
2. Allow for dynamic contents in zookeeper hashes with variable periodicity.
3. Better behavior when the zookeeper server cannot be found.
4. A simple service discovery shim to pair with this.


## License


Copyright (C) 2012 Crowdflower
Distributed under the Apache Public License.
Please see LICENSE for more details.
