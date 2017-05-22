"use strict";
const TAG = "PBPTestError";
const OUT = "/test_errors";

var fs = require('fs');
var file = process.argv[2];


var content = fs
  .readFileSync(file)
  .toString()
  .split("\n")
  .filter((l) => l.indexOf(TAG) !== -1)
  .map((l) => l.split(TAG + ": ")[1]);

var currentName = null;
var currentContent = null;

console.log("Total lines: " + content.length);

var writeToDisk = function(name, content) {
  console.log("Writing " + name + ".jpg");
  fs.writeFileSync(__dirname + OUT + "/" + name + ".jpg", content, 'base64');
};

content.forEach(function(line) {
  // Remove any leftover from previous run
  if(line.indexOf("--START") !== -1) {
    var files = fs.readdirSync(__dirname + OUT);
    files = files.filter((f) => f.indexOf('.jpg') !== -1);

    if(files.length > 0) {
      console.log("Cleaning " + files);
      files.forEach((f) => fs.unlinkSync(__dirname + OUT + "/" + f));
    }
    return;
  }

  if(line.indexOf("Error:") !== -1) {
    if(currentName) {
      writeToDisk(currentName, currentContent);
    }
    currentName = line.substr(6);
    currentContent = "";
    return;
  }

  currentContent += line;
});

writeToDisk(currentName, currentContent);
