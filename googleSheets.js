const fs = require('fs');
const request = require('request');
const Fetcher = require('./fromSheets');

console.log("google sheets script")


// t88kataloogiks skripti kataloog
process.chdir(__dirname);

function ProcessDataCB(source, CBfunction){
    process.chdir(__dirname);
    let headers = source.values[0];
    source.values.shift();
    console.log(headers);

    let target = new Array()
    source.values.forEach(element => {
        let language_o = {}
        for (const [key, value] of Object.entries(headers)) {
            language_o[value] = element[key]
        };
        target.push(language_o);
    });

    let objectToWrite = JSON.stringify(target, null, 4);
    CBfunction(objectToWrite)

    //fs.writeFileSync('../data/ISOLanguages.json', objectToWrite);
}


function WritePhrasesJSON (sheetsData){
    ProcessDataCB(sheetsData, function WriteLangs(sheetsData){
        console.log(sheetsData);
        fs.writeFileSync('phrases.json', sheetsData);
    });
}

//Fetcher.Fetch(spreadsheetId, range, ProcessDataCB)
Fetcher.Fetch('1Rgu-WPPCIjC2k0ss6HqmqV6Wbj_PqViKjSX8G_RqdYg', 'Sheet1', WritePhrasesJSON)


