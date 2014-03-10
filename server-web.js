var container = require('vertx/container');

var config = {
  "web_root": "./www",
  "port": 8080
};

container.deployModule("io.vertx~mod-web-server~2.0.0-final", config);