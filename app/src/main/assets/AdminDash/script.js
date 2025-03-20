// Function to fetch the queue
async function fetchQueue() {
    console.log('Fetching queue...');
    try {
        const response = await fetch('/get-queue'); // Fetch data from the /get-queue endpoint

        if (!response.ok) {
            console.error('Error fetching queue:', response.statusText);
            return;
        }

        // Parse the JSON response into an array of songs
        const queue = await response.json(); // Assuming the server returns an array of songs

        // Find the table element to populate
        const table = document.getElementById('queue-table');
        table.innerHTML = ''; // Clear any existing rows in the table

        // Loop through the queue and display each song in the table
        queue.forEach((song) => {
            const row = document.createElement('tr'); // Create a new table row
            row.innerHTML = `
                <td>${song.name} / ${song.artist}</td>

            `;
            table.appendChild(row); // Append the new row to the table
        });
    } catch (error) {
        console.error('Error fetching queue:', error);
    }
}

// Function to remove a song from the queue
function removeSongFromQueue(uri) {
    const playlistLink = document.getElementById("playlist-link").value; // Get the playlist link from the input field

     const url = `/remove-from-queue?uri=${uri}&playlistLink=${playlistLink}`;

    // Send the request to the backend
    fetch(url, {
        method: 'POST',
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            throw new Error(`Error: ${response.status} - ${response.statusText}`);
        }
    })
    .then(data => {
        console.log(data); // Handle the response here
    })
    .catch(error => {
        console.error('Error:', error);
    });
}


document.getElementById("ban-song-form").addEventListener("submit", function (event) {
    event.preventDefault(); // Prevent the default form submission

    // Get the song, artist, and genre values from the form
    const songName = document.getElementById("song-name").value;
    const artistName = document.getElementById("artist").value;


    // Validate inputs


    // Create the URL for the request (including the song name, artist, and genre)
    const url = `/ban-song?song=${songName}&artist=${artistName}`;

    // Send the request to the backend
    fetch(url, {
        method: 'POST',
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            throw new Error(`Error: ${response.status} - ${response.statusText}`);
        }
    })
    .then(message => {
        // Display the success message
        alert(message); // Optionally, update the UI to reflect the successful ban
    })
    .catch(error => {
        console.error("Error banning song:", error);
        alert("Failed to ban song. Please try again.");
    });
});

function fetchBannedList() {
    fetch('/get-Banned')
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("ban-table");
            tableBody.innerHTML = ""; // Clear previous entries

            data.forEach(item => {
                const row = document.createElement("tr");
                row.innerHTML = `<td>${item.song} / ${item.artist}</td>`;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error("Error fetching banned list:", error));
}

// Fetch the banned list when the page loads


// Add an event listener to the button to fetch the banned list when clicked
document.getElementById('fetchBannedBtn').addEventListener('click', fetchBannedList);


function promptForName() {
    let name = prompt("Please enter the name of the breather:");
    if (name) {
        // Send the name to the backend (you can use fetch to hit your backend endpoint)
        fetch('/setBreather', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name: name })
        })
        .then(response => response.json())
        .then(data => {
            // Do something with the response, e.g., confirm the name is set
            console.log(data.message);
        })
        .catch(error => console.error('Error:', error));
    }
}
let toggled = false
function toggleQueue(){
    if(toggled){
       document.getElementById("queue-btn").style.color = 'green'
       toggled = false
    }
    if(!toggled){
      document.getElementById("queue-btn").style.color = 'red'
      toggled = true
    }
}
// Validate the session and fetch the queue on page load
fetchQueue();
fetchBannedList();

