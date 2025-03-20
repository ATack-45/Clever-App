class Student {
    constructor(firstName, lastName, ID, NFC, currentPunchout) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.ID = ID;
        this.NFC = NFC;
        this.In = currentPunchout ? true : false;
        this.doms = [];
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
        let student = document.createElement("tr");
        student.innerHTML = template;

        student.querySelector(".studentName h2").innerHTML = this.fullName;
        student.querySelector(".studentName h3").innerHTML = ID;

        student.querySelector(".nfc").innerHTML = NFC;

        student.querySelector(".status").innerHTML = currentPunchout
            ? "OUT"
            : "IN";
        student.querySelector(".status").className += currentPunchout
            ? " out"
            : " in";
        student.querySelector(".date").innerHTML += "Coming Soon";
        StudentsPage.querySelector("tbody").appendChild(student);

        const coutTemplate = `
        <td class="studentName">
           <i class="fa-solid fa-user fa-xl"></i>
           <h2></h2>
           <h3></h3>
       </td>
       <td class="txt reason"></td>
       <td class="txt duration"></td>
   `;
        let currentOut = document.createElement("tr");
        currentOut.innerHTML = coutTemplate;

        currentOut.className = currentPunchout ? "" : "hidden"

        currentOut.querySelector(".studentName h2").innerHTML = this.firstName;
        currentOut.querySelector(".studentName h3").innerHTML = ID;
        console.log(currentPunchout)
        currentOut.querySelector(".reason").innerHTML = currentPunchout ? currentPunchout.reason : ''

        currentOut.querySelector(".duration").className += currentPunchout ? currentPunchout.duration : ''
            ? " out"
            : " in";
        document.getElementById('coutTable').appendChild(currentOut)
    }

    Update() {}

    Punch(status) {}
}
