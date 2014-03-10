var container = require('vertx/container');
var console   = require('vertx/console');

var conf = {
  host: '127.0.0.1',
  port: 8888
};

container.deployModule("io.stalk~mod-shortener-server~0.1.8", conf);
container.deployModule("io.stalk~mod-shortener-persister~0.0.2");
