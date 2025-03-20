const StudentsPage = document.getElementById('studentsPage')

function pageHandler() {
    let pages = {
        "dashboardPage": "dashboardBtn",
        "studentsPage": "studentsBtn",
        "settingsPage": "settingsBtn",
    }
    let currentPage = "dashboardPage";
    let currentBtn = "dashboardBtn"
    Object.keys(pages).forEach((pageId) => {
        const page = document.getElementById(pageId);
        const btn = document.getElementById(pages[pageId]);
        btn.onclick = function() {
            if (currentPage == pageId) return;
            document.getElementById(currentPage).className = "";
            document.getElementById(currentBtn).className = "";
            page.className = "shown";
            btn.className = "selected";
            currentPage = pageId;
            currentBtn = pages[pageId];
        }
    })
}
pageHandler();

let studentSignals = {}

function CreateStudentsPage(students) {
    const template = `
     <td class="studentName">
        <i class="fa-solid fa-user fa-xl"></i>
        <h2></h2>
        <h3></h3>
    </td>
    <td class="txt status"></td>
    <td class="txt nfc"></td>
    <td class="txt date"></td>
`;
    Object.values(students).forEach((data) => {
        if (!data.nfcId) return;
        let student = new Student(
            data.firstName,
            data.lastName,
            data.id,
            data.nfcId,
            data.currentPunchout
        );
    })
}

fetch('https://student-tracker-api.azurewebsites.net/api/student/getall', {
     method: 'GET',
     headers: { ApiKey: 'NFJejnqGdi' }
 })
     .then(response => {
         if (!response.ok) {
             throw new Error('Network response was not ok ' + response.statusText);
         }
         return response.json();
     })
     .then(data => {
         CreateStudentsPage(data)
     })
     .catch(error => {
         console.error('There was a problem with the fetch operation:', error);
     });

 var connection = new signalR.HubConnectionBuilder()
     .configureLogging(signalR.LogLevel.Debug)
     .withUrl("https://student-tracker-api.azurewebsites.net/punchoutHub", {
         accessTokenFactory: () => {
             return "NFJejnqGdi";
         },
         transport: signalR.HttpTransportType.WebSockets,
     })
     .build();

 connection.start().then(function () {
     //console.log("Connection successful");
 }).catch(function (err) {
     return console.error(err.toString());
 });

 connection.on("PunchoutCreated", function (punchout, student) {
     console.log(punchout, student);
 });

 connection.on("PunchoutClosed", function (info) {
     console.log(info);
 });

