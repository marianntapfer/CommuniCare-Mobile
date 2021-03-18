const fs = require('fs');
const request = require('request');
const Fetcher = require('./fromSheets');

process.chdir(__dirname);

function ProcessDataCB(source, CBfunction){
    process.chdir(__dirname);

    const listOfData = source.values;
    let viewObject = {};
    viewObject["elements"] = [];

    listOfData.forEach((data, index) => {
        if (data[0].toLowerCase().trim() !== "settings"){

            let obj = {
                      "label": data[2].toLowerCase().trim().replace(/ /g, "_"),
                      "drawable": data[11],
                      "viewCategory": data[0].toLowerCase().trim().replace(/ /g, "_"),
                      "viewRedirect":data[9],
                      "textToSpeech": data[10],
                    };
            viewObject["elements"].push(obj);
        };

    });

    viewObject.elements.shift();

    viewObject.elements.forEach((item, index) => {
        item["id"]= index
    })


    CBfunction(JSON.stringify(viewObject, null, 4));
}

function WritePhrasesJSON (sheetsData){
    ProcessDataCB(sheetsData, function WriteLangs(sheetsData){
        fs.writeFileSync('data/viewObjects.json', sheetsData);
        console.log("writing data/viewObjects.json")
    });
}

Fetcher.Fetch('1Rgu-WPPCIjC2k0ss6HqmqV6Wbj_PqViKjSX8G_RqdYg', 'Sheet1', WritePhrasesJSON)



