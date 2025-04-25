package com.example.webhooksolver.service;

import com.example.webhooksolver.model.User;
import com.example.webhooksolver.model.WebhookRequest;
import com.example.webhooksolver.model.WebhookResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class WebhookService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

    @PostConstruct
    public void init() {
        try {
            // Create request body
            WebhookRequest request = new WebhookRequest();
            request.setName("John Doe");
            request.setRegNo("REG12347");
            request.setEmail("john@example.com");

            // Make initial request to get webhook URL and data
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
                GENERATE_WEBHOOK_URL,
                request,
                WebhookResponse.class
            );

            WebhookResponse webhookResponse = response.getBody();
            if (webhookResponse != null) {
                // Solve the problem based on registration number
                String regNo = request.getRegNo();
                int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
                List<List<Integer>> result;

                if (lastTwoDigits % 2 == 0) {
                    // Question 2: Nth-Level Followers
                    result = solveNthLevelFollowers(webhookResponse.getData().getUsers());
                } else {
                    // Question 1: Mutual Followers
                    result = solveMutualFollowers(webhookResponse.getData().getUsers());
                }

                // Send result to webhook
                sendResultToWebhook(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<List<Integer>> solveMutualFollowers(List<User> users) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, Set<Integer>> followsMap = new HashMap<>();

        // Build follows map
        for (User user : users) {
            followsMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        // Find mutual followers
        for (User user : users) {
            for (Integer followedId : user.getFollows()) {
                if (followsMap.containsKey(followedId) && 
                    followsMap.get(followedId).contains(user.getId())) {
                    List<Integer> pair = Arrays.asList(
                        Math.min(user.getId(), followedId),
                        Math.max(user.getId(), followedId)
                    );
                    if (!result.contains(pair)) {
                        result.add(pair);
                    }
                }
            }
        }

        return result;
    }

    private List<List<Integer>> solveNthLevelFollowers(List<User> users) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, Set<Integer>> followsMap = new HashMap<>();
        int n = 2; // nth level
        int findId = 1; // start ID

        // Build follows map
        for (User user : users) {
            followsMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        // Find nth level followers
        Set<Integer> currentLevel = new HashSet<>();
        currentLevel.add(findId);
        Set<Integer> visited = new HashSet<>(currentLevel);

        for (int level = 1; level <= n; level++) {
            Set<Integer> nextLevel = new HashSet<>();
            for (Integer userId : currentLevel) {
                if (followsMap.containsKey(userId)) {
                    for (Integer followedId : followsMap.get(userId)) {
                        if (!visited.contains(followedId)) {
                            nextLevel.add(followedId);
                            visited.add(followedId);
                        }
                    }
                }
            }
            if (level == n) {
                for (Integer followerId : nextLevel) {
                    result.add(Collections.singletonList(followerId));
                }
            }
            currentLevel = nextLevel;
        }

        return result;
    }

    private void sendResultToWebhook(String webhookUrl, String accessToken, List<List<Integer>> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("outcome", result);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Retry logic
        int maxRetries = 4;
        int currentTry = 0;
        boolean success = false;

        while (!success && currentTry < maxRetries) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
                );
                success = true;
            } catch (Exception e) {
                currentTry++;
                if (currentTry == maxRetries) {
                    throw new RuntimeException("Failed to send result to webhook after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(1000); // Wait 1 second before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
} 