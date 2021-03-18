const fs = require('fs');
const request = require('request');
const Fetcher = require('./fromSheets');

process.chdir(__dirname);

function ProcessDataCB(source, CBfunction){
    process.chdir(__dirname);

    const listOfData = source.values;

    //save all headers in a variable
    let headers = listOfData[0];
    headers.forEach( function(element, index) {
        headers[index] = element.toLowerCase().trim();
    });
    headers.shift();
    listOfData.shift();
    console.log(headers)

    let Phrases = {};

    listOfData.forEach((data, dataIndex) => {
        let obj = {};
        data.shift();
        data.forEach((element, index) => {
            const key = headers[index];
            if (headers[index] !== "drawable" && headers[index] !== "redirectview" && headers[index] !== "texttospeech"){
                obj[headers[index]]=element.toLowerCase().trim();
            };
        });

        let key = data[1].toLowerCase().trim().replace(/ /g, "_");

        Phrases[key] = obj;
    })


    let objectToWrite = JSON.stringify({"translations": Phrases}, null, 4);
    CBfunction(objectToWrite);
};

function WritePhrasesJSON (sheetsData){
    ProcessDataCB(sheetsData, function WriteLangs(sheetsData){
        fs.writeFileSync('data/translations.json', sheetsData);
        console.log('generating data/translations.json');
    });
}

Fetcher.Fetch('1Rgu-WPPCIjC2k0ss6HqmqV6Wbj_PqViKjSX8G_RqdYg', 'Sheet1', WritePhrasesJSON);



