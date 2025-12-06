package com.sustech.so_java_stats.init;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustech.so_java_stats.model.*;
import com.sustech.so_java_stats.repository.QuestionRepository;
import com.sustech.so_java_stats.repository.StackUserRepository;
import com.sustech.so_java_stats.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final QuestionRepository questionRepository;
    private final StackUserRepository stackUserRepository;
    private final TagRepository tagRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Tag> tagCache;
    private Map<Long, StackUser> userCache;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        long existingQuestions = questionRepository.count();
        if (existingQuestions > 0) {
            log.info("Database already contains {} questions. Skipping initialization.", existingQuestions);
            return;
        }

        String dataDir = "src/main/resources/data";
        log.info("Starting optimized initialization from: {}", dataDir);

        log.info("Pre-loading caches...");
        tagCache = tagRepository.findAll().stream()
                .collect(Collectors.toMap(Tag::getTagName, Function.identity()));
        userCache = stackUserRepository.findAll().stream()
                .collect(Collectors.toMap(StackUser::getStackUserId, Function.identity()));

        log.info("Caches loaded. Tags: {}, Users: {}", tagCache.size(), userCache.size());

        List<Question> batchList = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;
        int totalFilesToCheck = 7500;
        int BATCH_SIZE = 500;

        for (int x = 1; x <= totalFilesToCheck; x++) {
            File file = new File(dataDir + "/thread_" + x + ".json");

            if (!file.exists()) {
                skipCount++;
                continue;
            }

            try {
                JsonNode root = objectMapper.readTree(file);

                Question question = parseQuestion(root);
                batchList.add(question);
                successCount++;

                if (batchList.size() >= BATCH_SIZE) {
                    saveBatch(batchList);
                    log.info("Processed {}/{} files...", x, totalFilesToCheck);
                }

            } catch (Exception e) {
                log.error("Failed to process file: {}", file.getName(), e);
            }
        }

        if (!batchList.isEmpty()) {
            saveBatch(batchList);
        }

        log.info("Finished. Processed: {}, Skipped: {}", successCount, skipCount);
    }

    private void saveBatch(List<Question> batch) {
        questionRepository.saveAll(batch);
        questionRepository.flush();
        batch.clear();
    }

    private Question parseQuestion(JsonNode root) {
        Question question = new Question();
        question.setQuestionId(root.path("question_id").asLong());
        question.setTitle(root.path("title").asText());
        question.setBody(root.path("body").asText());
        question.setCreationDate(Instant.ofEpochSecond(root.path("creation_date").asLong()));
        question.setScore(root.path("score").asInt());
        question.setViewCount(root.path("view_count").asInt());
        question.setAnswerCount(root.path("answer_count").asInt());
        question.setIsAnswered(root.path("is_answered").asBoolean());

        if (root.has("last_edit_date"))
            question.setLastEditDate(Instant.ofEpochSecond(root.path("last_edit_date").asLong()));
        if (root.has("last_activity_date"))
            question.setLastActivityDate(Instant.ofEpochSecond(root.path("last_activity_date").asLong()));
        if (root.has("accepted_answer_id")) question.setAcceptedAnswerId(root.path("accepted_answer_id").asLong());

        // Owner
        if (!root.path("owner").isMissingNode()) {
            question.setOwner(resolveUser(root.path("owner")));
        }

        // Tags
        List<Tag> tags = new ArrayList<>();
        for (JsonNode tagNode : root.path("tags")) {
            tags.add(resolveTag(tagNode.asText()));
        }
        question.setTags(tags);

        // Comments
        question.setComments(createComments(root.path("comments")));

        // Answers
        List<Answer> answers = new ArrayList<>();
        for (JsonNode ansNode : root.path("answers")) {
            Answer answer = new Answer();
            answer.setAnswerId(ansNode.path("answer_id").asLong());
            answer.setBody(ansNode.path("body").asText());
            answer.setCreationDate(Instant.ofEpochSecond(ansNode.path("creation_date").asLong()));
            answer.setScore(ansNode.path("score").asInt());
            answer.setIsAccepted(ansNode.path("is_accepted").asBoolean());

            if (ansNode.has("last_edit_date"))
                answer.setLastEditDate(Instant.ofEpochSecond(ansNode.path("last_edit_date").asLong()));
            if (ansNode.has("last_activity_date"))
                answer.setLastActivityDate(Instant.ofEpochSecond(ansNode.path("last_activity_date").asLong()));

            if (!ansNode.path("owner").isMissingNode()) {
                answer.setOwner(resolveUser(ansNode.path("owner")));
            }
            answer.setComments(createComments(ansNode.path("comments")));
            answer.setQuestion(question);
            answers.add(answer);
        }
        question.setAnswers(answers);

        // Handle logic for accepted answer ID if missing from root but present in answers
        if (question.getAcceptedAnswerId() == null) {
            for (Answer ans : answers) {
                if (Boolean.TRUE.equals(ans.getIsAccepted())) {
                    question.setAcceptedAnswerId(ans.getAnswerId());
                    break;
                }
            }
        }

        return question;
    }

    private Tag resolveTag(String tagName) {
        if (tagCache.containsKey(tagName)) {
            return tagCache.get(tagName);
        }
        Tag newTag = new Tag(tagName);
        tagCache.put(tagName, newTag);
        return newTag;
    }

    private StackUser resolveUser(JsonNode userNode) {
        long userId = userNode.path("user_id").asLong();
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }

        StackUser user = new StackUser();
        user.setStackUserId(userId);
        user.setReputation(userNode.path("reputation").asInt());
        user.setDisplayName(userNode.path("display_name").asText());

        userCache.put(userId, user);
        return user;
    }

    private List<Comment> createComments(JsonNode commentsArray) {
        List<Comment> comments = new ArrayList<>();
        for (JsonNode comNode : commentsArray) {
            Comment comment = new Comment();
            comment.setCommentId(comNode.path("comment_id").asLong());
            comment.setBody(comNode.path("body").asText());
            comment.setCreationDate(Instant.ofEpochSecond(comNode.path("creation_date").asLong()));
            comment.setScore(comNode.path("score").asInt());
            comment.setEdited(comNode.path("edited").asBoolean());

            if (!comNode.path("owner").isMissingNode()) {
                comment.setOwner(resolveUser(comNode.path("owner")));
            }
            if (!comNode.path("reply_to_user").isMissingNode()) {
                comment.setReplyToUser(resolveUser(comNode.path("reply_to_user")));
            }
            comments.add(comment);
        }
        return comments;
    }
}
