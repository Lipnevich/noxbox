let fs = require('fs');
fs.readFile('template.html', 'UTF8', function(err, data) {

  console.log(data);

  let result = data.replace('$ooo', 'RUS_VALUE');

  console.log(result);

  fs.writeFile('en.html', result, function (err) {
    if (err) throw err;
    console.log('Saved!');
  });

});
