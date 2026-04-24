# Quiz Leaderboard Processor

## Description

This project is a small Java program that I wrote to work with quiz data from an API. It collects the data, removes any duplicate entries, calculates the total score for each participant, and then builds a leaderboard. Once everything is processed, it sends the final result back to the API.

---

## How It Works

* Fetches quiz data from the API in multiple steps
* Skips duplicate entries to keep the data clean
* Adds up scores for each participant
* Sorts everyone based on their total score
* Sends the final leaderboard back to the API

---

## Sample Output

Diana : 470
Ethan : 455
Fiona : 440

Total Score = 1365

---

## Technologies Used

* Java
* Gson (to handle JSON data)
* HTTPURLConnection (to make API calls)

---

## How to Run

1. Open the project in Eclipse
2. Add the Gson library
3. Run `Main.java`
4. Wait for it to finish (takes around 50 seconds because of API calls)

---

## Note

The API only checks the first valid submission for a register number. Running it again will just return a summary instead of validation.

---

## Author

RA2311003010331
