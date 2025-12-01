package com.sustech.so_java_stats.init;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustech.so_java_stats.model.*;
import com.sustech.so_java_stats.repository.QuestionRepository;
import com.sustech.so_java_stats.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;

@RequiredArgsConstructor
@Component
public class DatabaseInitializer implements CommandLineRunner {
    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;

    private final String DATA_FOLDER = "/Users/layhenghok/Desktop/SUSTech/Year4Semester1/CS209A-Computer-System-Design-and-Applications-A/Project/Codebase/StackOverflow-Java-Stats/backend/so-java-stats/data";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (questionRepository.count() > 0) {
            System.out.println("Data already exists. Skipping load.");
            return;
        }

        System.out.println("Starting Data Import from " + DATA_FOLDER + "...");
        ObjectMapper mapper = new ObjectMapper();
        File folder = new File(DATA_FOLDER);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        JsonNode root = mapper.readTree(file);
                        processThread(root);
                    } catch (Exception e) {
                        System.err.println("Failed to parse " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Data Import Completed.");
    }

    private void processThread(JsonNode root) {
        Question q = new Question();
        q.setQuestionId(root.get("question_id").asLong());
        q.setTitle(root.path("title").asText());
        q.setBody(root.path("body").asText());
        q.setCreationDate(Instant.ofEpochSecond(root.get("creation_date").asLong()));
        q.setScore(root.path("score").asInt());
        q.setViewCount(root.path("view_count").asInt());
        q.setAnswerCount(root.path("answer_count").asInt());
        q.setIsAnswered(root.path("is_answered").asBoolean());
        if (root.has("accepted_answer_id")) {
            q.setAcceptedAnswerId(root.get("accepted_answer_id").asLong());
        }

        // Parse Owner
        if (root.has("owner")) q.setOwner(parseUser(root.get("owner")));

        // Parse Tags
        if (root.has("tags")) {
            for (JsonNode tagNode : root.get("tags")) {
                String tagName = tagNode.asText();
                // Ensure tag exists or create new (Caching recommended for performance)
                Tag tag = tagRepository.findById(Long.valueOf(tagName)).orElse(new Tag(tagName));
                q.getTags().add(tag);
            }
        }

        // Parse Answers
        if (root.has("answers")) {
            for (JsonNode ansNode : root.get("answers")) {
                Answer a = new Answer();
                a.setAnswerId(ansNode.get("answer_id").asLong());
                a.setBody(ansNode.path("body").asText());
                a.setCreationDate(Instant.ofEpochSecond(ansNode.get("creation_date").asLong()));
                a.setScore(ansNode.path("score").asInt());
                a.setIsAccepted(ansNode.path("is_accepted").asBoolean());
                if (ansNode.has("owner")) a.setOwner(parseUser(ansNode.get("owner")));

                // Answer Comments
                if (ansNode.has("comments")) {
                    for (JsonNode commentNode : ansNode.get("comments")) {
                        a.getComments().add(parseComment(commentNode));
                    }
                }
                a.setQuestion(q);
                q.getAnswers().add(a);
            }
        }

        // Parse Question Comments
        if (root.has("comments")) {
            for (JsonNode commentNode : root.get("comments")) {
                q.getComments().add(parseComment(commentNode));
            }
        }

        questionRepository.save(q);
    }

    private StackUser parseUser(JsonNode ownerNode) {
        if (!ownerNode.has("user_id")) return null; // Deleted users
        StackUser user = new StackUser();
        user.setUserId(ownerNode.get("user_id").asLong());
        user.setReputation(ownerNode.path("reputation").asInt());
        user.setDisplayName(ownerNode.path("display_name").asText());
        return user;
    }

    private Comment parseComment(JsonNode commentNode) {
        Comment c = new Comment();
        c.setCommentId(commentNode.get("comment_id").asLong());
        c.setBody(commentNode.path("body").asText());
        c.setScore(commentNode.path("score").asInt());
        c.setCreationDate(Instant.ofEpochSecond(commentNode.get("creation_date").asLong()));
        if (commentNode.has("owner")) c.setOwner(parseUser(commentNode.get("owner")));
        return c;
    }
}