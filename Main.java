import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.google.gson.*;

public class Main {

    private static final String ROOT_ENDPOINT =
            "https://devapigw.vidalhealthtpa.com/srm-quiz-task";

    public static void main(String[] args) {

        System.out.println(">> Execution started...\n");

        String studentId = "RA2311003010331"; // DO NOT CHANGE

        Map<String, Integer> scoreMap = new HashMap<>();
        Set<String> uniqueTracker = new HashSet<>();

        Gson jsonTool = new Gson();

        try {

            retrieveAndProcess(studentId, scoreMap, uniqueTracker, jsonTool);

            JsonArray ranking = prepareRanking(scoreMap);

            postResult(studentId, ranking);

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    // ------------------------------------------------------------
    // STEP 1: RETRIEVE DATA
    // ------------------------------------------------------------
    private static void retrieveAndProcess(String id,
                                           Map<String, Integer> scoreMap,
                                           Set<String> tracker,
                                           Gson jsonTool) throws Exception {

        for (int index = 0; index < 10; index++) {

            System.out.println(">> Poll Request #" + index);

            String api = ROOT_ENDPOINT + "/quiz/messages?regNo=" + id + "&poll=" + index;

            HttpURLConnection conn =
                    (HttpURLConnection) new URL(api).openConnection();

            conn.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                response.append(currentLine);
            }
            reader.close();

            JsonObject parsed = jsonTool.fromJson(response.toString(), JsonObject.class);
            JsonArray items = parsed.getAsJsonArray("events");

            for (JsonElement element : items) {

                JsonObject record = element.getAsJsonObject();

                String round = record.get("roundId").getAsString();
                String name = record.get("participant").getAsString();
                int marks = record.get("score").getAsInt();

                // unique key (changed format)
                String identity = name + "::" + round;

                if (tracker.add(identity)) {

                    scoreMap.put(name,
                            scoreMap.getOrDefault(name, 0) + marks);

                    System.out.println("Accepted -> " + name + " (" + marks + ")");

                } else {
                    System.out.println("Ignored duplicate -> " + identity);
                }
            }

            Thread.sleep(5000);
            System.out.println();
        }
    }

    // ------------------------------------------------------------
    // STEP 2: CREATE LEADERBOARD
    // ------------------------------------------------------------
    private static JsonArray prepareRanking(Map<String, Integer> scoreMap) {

        List<Map.Entry<String, Integer>> entries =
                new ArrayList<>(scoreMap.entrySet());

        entries.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1,
                               Map.Entry<String, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });

        JsonArray leaderboard = new JsonArray();
        int combined = 0;

        System.out.println(">> Final Ranking:\n");

        for (Map.Entry<String, Integer> entry : entries) {

            JsonObject obj = new JsonObject();
            obj.addProperty("participant", entry.getKey());
            obj.addProperty("totalScore", entry.getValue());

            leaderboard.add(obj);
            combined += entry.getValue();

            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        System.out.println("\n>> Combined Score = " + combined);

        return leaderboard;
    }

    // ------------------------------------------------------------
    // STEP 3: SUBMIT RESULT
    // ------------------------------------------------------------
    private static void postResult(String id, JsonArray board) throws Exception {

        URL url = new URL(ROOT_ENDPOINT + "/quiz/submit");

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        JsonObject payload = new JsonObject();
        payload.addProperty("regNo", id);
        payload.add("leaderboard", board);

        OutputStream out = conn.getOutputStream();
        out.write(payload.toString().getBytes("UTF-8"));
        out.flush();
        out.close();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();

        System.out.println("\n>> Submission Response:");
        System.out.println(result.toString());
    }
}