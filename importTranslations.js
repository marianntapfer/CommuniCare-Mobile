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
    console.log("All headers", headers);
    //Delete the header row
    listOfData.shift();


    // get all category names
    let categorys = [];
    source.values.forEach((item) => {
      categorys.push(item[0].toLowerCase().trim());
    });
    categorys = [... new Set(categorys)];

    let Phrases = {}
    categorys.forEach(item => {
        Phrases[item] = []
    })

    listOfData.forEach((data, dataIndex) => {
        const category = data[0].toLowerCase().trim();
        let obj = {}
        data.shift();
        data.forEach((element, index) => {
            const key = headers[index]
            obj[headers[index]]=element.toLowerCase().trim();
        });

        Phrases[category].push(obj)
    })


    let objectToWrite = JSON.stringify(Phrases, null, 4);
    CBfunction(objectToWrite)
}


function WritePhrasesJSON (sheetsData){
    ProcessDataCB(sheetsData, function WriteLangs(sheetsData){
        // console.log(sheetsData);
        fs.writeFileSync('data/translations.json', sheetsData);
    });
}

//Fetcher.Fetch(spreadsheetId, range, ProcessDataCB)
Fetcher.Fetch('1Rgu-WPPCIjC2k0ss6HqmqV6Wbj_PqViKjSX8G_RqdYg', 'Sheet1', WritePhrasesJSON)

// {
//           "key":"pain"
//           "translations":{
//             "estonian": "valu tüüp",
//             "english": "pain",
//             "arabic": "الالم",
//             "russian male": "тип боли",
//             "russian female": "тип боли",
//             "german": "schmerz",
//             "finnish": "",
//             "dutch": "pijn",
//             "italian": "dolore"
//           }
//         }



