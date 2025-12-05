import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class DataScraper {
    private static final String API_KEY = "";
    private static final String TAG = "java";
    private static final int CHUNK_SIZE = 2500; // 4 chunks, CHUNK_SIZE * 4 = 10,000 threads

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<Integer> processedIds = new HashSet<>();
    private static int threadCount = 0;

    public static void main(String[] args) {
        try {
            System.out.println("Step 0: Creating custom filter...");

            String includeFields = "question.answers;question.body;question.comments;question.creation_date;question.last_activity_date;question.last_edit_date;question.link;question.owner;question.question_id;question.score;question.tags;question.title;question.view_count;question.answer_count;question.is_answered;answer.body;answer.comments;answer.creation_date;answer.last_activity_date;answer.last_edit_date;answer.owner;answer.score;answer.is_accepted;comment.body;comment.creation_date;comment.owner;comment.score;owner.account_id;owner.display_name;owner.link;owner.profile_image;owner.reputation;owner.user_id;owner.user_type";
            String createFilterUrl = String.format(
                    "https://api.stackexchange.com/2.3/filters/create?base=default&unsafe=false&include=%s&key=%s",
                    includeFields.replace(";", "%3B"), API_KEY
            );

            String filterResponse = makeApiCall(createFilterUrl);
            JsonNode filterRoot = mapper.readTree(filterResponse);
            String customFilter = filterRoot.get("items").get(0).get("filter").asText();

            System.out.println("Custom filter created: " + customFilter);

            System.out.println("Step 1: Scraping 10,000 diverse threads...");

            File threadsDir = new File("src/main/resources/data");
            threadsDir.mkdirs();

            // Collect diverse data
            fetchBySortStrategy("votes", customFilter);    // Historic high quality
            fetchBySortStrategy("activity", customFilter); // Currently active
            fetchBySortStrategy("creation", customFilter); // Newest
            fetchBySortStrategy("hot", customFilter);      // Trending

            System.out.println("------------------------------------------------");
            System.out.println("SCRAPING COMPLETE.");
            System.out.println("Total threads collected: " + threadCount);
            System.out.println("Data saved to: " + threadsDir.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fetchBySortStrategy(String sort, String filter) throws Exception {
        int count = 0;
        int page = 1;
        System.out.println("--> Fetching strategy: " + sort);

        while (count < CHUNK_SIZE) {
            String url = String.format(
                    "https://api.stackexchange.com/2.3/questions?page=%d&pagesize=50&order=desc&sort=%s&tagged=%s&site=stackoverflow&filter=%s&key=%s",
                    page, sort, TAG, filter, API_KEY
            );

            String response = makeApiCall(url);
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.get("items");

            if (items == null || items.size() == 0) break;

            for (JsonNode item : items) {
                int qId = item.get("question_id").asInt();
                if (!processedIds.contains(qId)) {
                    processedIds.add(qId);
                    // Save individual thread
                    threadCount++;
                    File threadFile = new File("src/main/resources/data", "thread_" + threadCount + ".json");
                    mapper.writerWithDefaultPrettyPrinter().writeValue(threadFile, item);
                    count++;
                }
                if (count >= CHUNK_SIZE) break;
            }

            // Respect rate limits
            Thread.sleep(100);
            page++;
        }
        System.out.println("    Collected " + count + " items for " + sort);
    }

    private static String makeApiCall(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept-Encoding", "gzip")
                .GET()
                .build();

        HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        String encoding = response.headers().firstValue("Content-Encoding").orElse("");
        if ("gzip".equalsIgnoreCase(encoding)) {
            return new String(new GZIPInputStream(response.body()).readAllBytes(), StandardCharsets.UTF_8);
        }
        return new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
    }
}
