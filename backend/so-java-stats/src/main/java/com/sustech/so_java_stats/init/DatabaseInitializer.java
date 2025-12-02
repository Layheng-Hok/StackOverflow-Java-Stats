package com.sustech.so_java_stats.init;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustech.so_java_stats.model.*;
import com.sustech.so_java_stats.repository.QuestionRepository;
import com.sustech.so_java_stats.repository.StackUserRepository;
import com.sustech.so_java_stats.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final QuestionRepository questionRepository;
    private final StackUserRepository stackUserRepository;
    private final TagRepository tagRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        String dataDir = "src/main/resources/data";

        for (int x = 1; x <= 7500; x++) {
            File file = new File(dataDir + "/thread_" + x + ".json");
            if (!file.exists()) {
                continue;
            }

            JsonNode root = objectMapper.readTree(file);

            Map<Long, StackUser> userCache = new HashMap<>();

            Question question = new Question();
            question.setQuestionId(root.path("question_id").asLong());
            question.setTitle(root.path("title").asText());
            question.setBody(root.path("body").asText());
            question.setCreationDate(Instant.ofEpochSecond(root.path("creation_date").asLong()));
            question.setScore(root.path("score").asInt());
            question.setViewCount(root.path("view_count").asInt());
            question.setAnswerCount(root.path("answer_count").asInt());
            question.setIsAnswered(root.path("is_answered").asBoolean());

            if (root.has("last_edit_date")) {
                question.setLastEditDate(Instant.ofEpochSecond(root.path("last_edit_date").asLong()));
            }
            if (root.has("last_activity_date")) {
                question.setLastActivityDate(Instant.ofEpochSecond(root.path("last_activity_date").asLong()));
            }
            if (root.has("accepted_answer_id")) {
                question.setAcceptedAnswerId(root.path("accepted_answer_id").asLong());
            }

            // Owner
            JsonNode ownerNode = root.path("owner");
            if (!ownerNode.isMissingNode()) {
                StackUser owner = getOrCreateStackUser(ownerNode, userCache);
                question.setOwner(owner);
            }

            // Tags
            List<Tag> tags = new ArrayList<>();
            JsonNode tagsArray = root.path("tags");
            for (JsonNode tagNode : tagsArray) {
                String tagName = tagNode.asText();
                Tag tag = tagRepository.findById(tagName).orElseGet(() -> {
                    Tag newTag = new Tag(tagName);
                    return tagRepository.save(newTag);
                });
                tags.add(tag);
            }
            question.setTags(tags);

            // Question comments
            List<Comment> qComments = createComments(root.path("comments"), userCache);
            question.setComments(qComments);

            // Answers
            List<Answer> answers = new ArrayList<>();
            JsonNode answersArray = root.path("answers");
            for (JsonNode ansNode : answersArray) {
                Answer answer = new Answer();
                answer.setAnswerId(ansNode.path("answer_id").asLong());
                answer.setBody(ansNode.path("body").asText());
                answer.setCreationDate(Instant.ofEpochSecond(ansNode.path("creation_date").asLong()));
                answer.setScore(ansNode.path("score").asInt());
                answer.setIsAccepted(ansNode.path("is_accepted").asBoolean());

                if (ansNode.has("last_edit_date")) {
                    answer.setLastEditDate(Instant.ofEpochSecond(ansNode.path("last_edit_date").asLong()));
                }
                if (ansNode.has("last_activity_date")) {
                    answer.setLastActivityDate(Instant.ofEpochSecond(ansNode.path("last_activity_date").asLong()));
                }

                // Answer owner
                JsonNode ansOwnerNode = ansNode.path("owner");
                if (!ansOwnerNode.isMissingNode()) {
                    StackUser ansOwner = getOrCreateStackUser(ansOwnerNode, userCache);
                    answer.setOwner(ansOwner);
                }

                // Answer comments
                List<Comment> ansComments = createComments(ansNode.path("comments"), userCache);
                answer.setComments(ansComments);

                answer.setQuestion(question);
                answers.add(answer);
            }
            question.setAnswers(answers);

            // Set acceptedAnswerId if not set but present in answers
            if (question.getAcceptedAnswerId() == null) {
                for (Answer ans : answers) {
                    if (Boolean.TRUE.equals(ans.getIsAccepted())) {
                        question.setAcceptedAnswerId(ans.getAnswerId());
                        break;
                    }
                }
            }

            // Save the question (cascades to answers, comments, etc.)
            questionRepository.save(question);
        }
    }

    private StackUser getOrCreateStackUser(JsonNode userNode, Map<Long, StackUser> userCache) {
        long userId = userNode.path("user_id").asLong();
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        Optional<StackUser> existingUser = stackUserRepository.findById(userId);
        StackUser user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = new StackUser();
            user.setStackUserId(userId);
            user.setReputation(userNode.path("reputation").asInt());
            user.setDisplayName(userNode.path("display_name").asText());
        }
        userCache.put(userId, user);
        return user;
    }

    private List<Comment> createComments(JsonNode commentsArray, Map<Long, StackUser> userCache) {
        List<Comment> comments = new ArrayList<>();
        for (JsonNode comNode : commentsArray) {
            Comment comment = new Comment();
            comment.setCommentId(comNode.path("comment_id").asLong());
            comment.setBody(comNode.path("body").asText());
            comment.setCreationDate(Instant.ofEpochSecond(comNode.path("creation_date").asLong()));
            comment.setScore(comNode.path("score").asInt());
            comment.setEdited(comNode.path("edited").asBoolean());

            // Comment owner
            JsonNode comOwnerNode = comNode.path("owner");
            if (!comOwnerNode.isMissingNode()) {
                StackUser comOwner = getOrCreateStackUser(comOwnerNode, userCache);
                comment.setOwner(comOwner);
            }

            // Reply to user
            JsonNode replyToNode = comNode.path("reply_to_user");
            if (!replyToNode.isMissingNode()) {
                StackUser replyTo = getOrCreateStackUser(replyToNode, userCache);
                comment.setReplyToUser(replyTo);
            }

            comments.add(comment);
        }
        return comments;
    }
}
