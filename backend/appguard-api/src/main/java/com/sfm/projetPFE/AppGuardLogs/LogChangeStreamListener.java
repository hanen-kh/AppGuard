package com.sfm.projetPFE.AppGuardLogs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoChangeStreamCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class LogChangeStreamListener {

    private static final String OUTPUT_FILE = "app.logs";
    private static final String COLLECTION_NAME = "appguardlogs";
    private static final Logger logger = LoggerFactory.getLogger(LogChangeStreamListener.class);

    private final MongoTemplate mongoTemplate;

    private BufferedWriter writer;
    private ExecutorService listenerExecutor;

    public LogChangeStreamListener(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void init() {
        logger.info("Starting MongoDB change stream listener");

        try {
            writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true));
        } catch (IOException e) {
            logger.error("Unable to open log output file {}", OUTPUT_FILE, e);
            return;
        }

        listenerExecutor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "mongodb-log-change-listener");
            thread.setDaemon(true);
            return thread;
        });
        listenerExecutor.submit(this::listenToChanges);
    }

    public void listenToChanges() {
        try {
            MongoCollection<Document> collection = mongoTemplate.getCollection(COLLECTION_NAME);
            try (MongoChangeStreamCursor<ChangeStreamDocument<Document>> cursor = collection.watch().cursor()) {
                while (cursor.hasNext()) {
                    ChangeStreamDocument<Document> change = cursor.next();

                    if ("insert".equals(change.getOperationType().getValue())) {
                        Document logInserted = change.getFullDocument();
                        if (logInserted != null) {
                            writeToFile(logInserted.toJson());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("MongoDB change stream stopped unexpectedly", e);
        }
    }

    private void writeToFile(String json) {
        try {
            writer.write(json);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            logger.error("Unable to write MongoDB log change to {}", OUTPUT_FILE, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (listenerExecutor != null) {
            listenerExecutor.shutdownNow();
            try {
                if (!listenerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("MongoDB change stream listener did not stop within five seconds");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.warn("Unable to close log output file {}", OUTPUT_FILE, e);
            }
        }
    }
}
