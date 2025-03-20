// 1. Load the Google API client library
function loadGoogleAuth() {
  const script = document.createElement('script');
  script.src = 'https://apis.google.com/js/platform.js';
  script.onload = initGoogleAuth;
  document.head.appendChild(script);
}

// 2. Initialize Google Auth
function initGoogleAuth() {
  gapi.load('auth2', function() {
    gapi.auth2.init({
      client_id: 'YOUR_CLIENT_ID_HERE.apps.googleusercontent.com',
    }).then(function(auth2) {
      // Attach sign-in handler to a button
      document.getElementById('googleSignInButton').addEventListener('click', function() {
        auth2.signIn().then(onSignIn);
      });
      
      // Check if user is already signed in
      if (auth2.isSignedIn.get()) {
        onSignIn(auth2.currentUser.get());
      }
    });
  });
}

// 3. Handle successful sign-in
function onSignIn(googleUser) {
  const profile = googleUser.getBasicProfile();
  const userName = profile.getName();
  const userEmail = profile.getEmail();
  
  // Store user info for later use when submitting songs
  sessionStorage.setItem('userName', userName);
  sessionStorage.setItem('userEmail', userEmail);
  
  // Update UI to show logged-in state
  document.getElementById('userInfo').textContent = `Logged in as: ${userName}`;
  document.getElementById('loginSection').classList.add('hidden');
  document.getElementById('songSubmitSection').classList.remove('hidden');
}


document.getElementById('add-song-form').addEventListener('submit', async (event) => {
    event.preventDefault(); // Prevent the form from refreshing the page

    const userName = sessionStorage.getItem('userName') || '';
    const userEmail = sessionStorage.getItem('userEmail') || '';
    const song = document.getElementById('song').value.trim();
    const artist = document.getElementById('artist').value.trim();

    const messageDiv = document.getElementById('message');
    messageDiv.innerHTML = ''; // Clear previous messages

    if (!song || !artist) {
        messageDiv.textContent = 'Both song and artist are required!';
        return;
    }
    if (userName == ''){
        messageDiv.textContent = 'Sucessful login required!';
        return;
    }

    try {
        const response = await fetch(`/add-song?song=${song}&artist=${artist}`, {
            method: 'POST',
        });

        if (response.ok) {
            messageDiv.textContent = 'Song added to the queue successfully!';
        } else {
            const error = await response.text();
            messageDiv.textContent = `Error: ${error}`;
        }
    } catch (error) {
        console.error('Error adding song to queue:', error);
        messageDiv.textContent = 'Failed to add song to queue. Please try again.';
    }
});
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
document.addEventListener('DOMContentLoaded', loadGoogleAuth);
fetchQueue()