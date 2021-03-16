const fs = require('fs');
const readline = require('readline');
const {google} = require('googleapis');


function Connect(FetchDataCB) {

    process.chdir(__dirname);
    // If modifying these scopes, delete token.json.
    const SCOPES = ['https://www.googleapis.com/auth/spreadsheets.readonly'];
    // The file token.json stores the user's access and refresh tokens, and is
    // created automatically when the authorization flow completes for the first
    // time.
    const TOKEN_PATH = 'token.json';

    // Load client secrets from a local file.

    fs.readFile('credentials.json', (err, content) => {
        if (err) return console.log('Error loading client secret file:', err);
        // Authorize a client with credentials, then call the Google Sheets API.
        authorize(JSON.parse(content), FetchDataCB);
    });

    function authorize(credentials, FetchDataCB) {
        const {client_secret, client_id, redirect_uris} = credentials.installed;
        const oAuth2Client = new google.auth.OAuth2(
            client_id, client_secret, redirect_uris[0]);

        // Check if we have previously stored a token.
        fs.readFile(TOKEN_PATH, (err, token) => {
            if (err) return getNewToken(oAuth2Client, FetchDataCB);
            oAuth2Client.setCredentials(JSON.parse(token));
            FetchDataCB(oAuth2Client);
        });
    }

    /**
     * Get and store new token after prompting for user authorization, and then
     * execute the given callback with the authorized OAuth2 client.
     * @param {google.auth.OAuth2} oAuth2Client The OAuth2 client to get token for.
     * @param {getEventsCallback} callback The callback for the authorized client.
     */
    function getNewToken(oAuth2Client, FetchDataCB) {
        console.log("get new token")
        const authUrl = oAuth2Client.generateAuthUrl({
            access_type: 'offline',
            scope: SCOPES,
        });
        console.log('Authorize this app by visiting this url:', authUrl);
        const rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout,
        });
        rl.question('Enter the code from that page here: ', (code) => {
            rl.close();
            oAuth2Client.getToken(code, (err, token) => {
            if (err) return console.error('Error while trying to retrieve access token', err);
            oAuth2Client.setCredentials(token);
            // Store the token to disk for later program executions
            fs.writeFile(TOKEN_PATH, JSON.stringify(token), (err) => {
                if (err) return console.error(err);
                console.log('Token stored to', TOKEN_PATH);
            });
            FetchDataCB(oAuth2Client);
            });
        });
    }
}

//fetch kutsub välja Connecti, mis loob ühenduse googleSheetsiga ja siis küsib vastavalt spreadsheetId-le ja rang-ile vajaliku data
function Fetch(spreadsheetId, range, callback, token){
    Connect(function FetchDataFromSheet(auth) {
        const sheets = google.sheets({version: 'v4', auth});
        sheets.spreadsheets.values.get({
          spreadsheetId: spreadsheetId,
          range: range,
        }, (err, res) => {
          if (err) return console.log('The API returned an error: ' + err);
          callback(res.data, token)
        });
      });
}


module.exports.Fetch = Fetch;