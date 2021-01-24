package com.codesoom.assignment;

import com.codesoom.assignment.models.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DemoHttpHandler implements HttpHandler {
    private Long newId = 0L;
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<Task> tasks = new ArrayList<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        System.out.println(method + " " + path);

        if(path.equals("/tasks")) {
            handleCollection(exchange, method);
            return;
        }

        if(path.startsWith("/tasks/")) {
            Long id = Long.parseLong(path.substring("/tasks/".length()));
            handleItem(exchange, method, id);
            return;
        }

        send(exchange, 200, "나는 진정 행복한 부자가 될 것이다.");
    }

    private void send(HttpExchange exchange, int statusCode, String content) throws IOException {

        exchange.sendResponseHeaders(statusCode, content.getBytes().length);

        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(content.getBytes());
        responseBody.flush();
        responseBody.close();
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        if(method.equals("GET")) {
            send(exchange, 200, toJSON(tasks));
        }

        if(method.equals("POST")) {
            String body = getBody(exchange);

            Task task = toTask(body);
            task.setId(generateId());
            tasks.add(task);

            System.out.println(body);

            send(exchange, 201, "New task is added : " + toJSON(task));
        }
    }

    private void handleItem(HttpExchange exchange, String method, Long id) throws IOException {
        if(method.equals("GET")) {
            Task task = findTask(id);
            send(exchange, 200, toJSON(task));
        }
    }

    private Task findTask(Long id) {
        return tasks.stream().filter(task -> task.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Long generateId() {
        newId++;
        return newId;
    }

    private String getBody(HttpExchange exchange) {
        InputStream requestBody = exchange.getRequestBody();
        return new BufferedReader(new InputStreamReader(requestBody))
            .lines()
            .collect(Collectors.joining("\n"));
    }

    private Task toTask(String content) throws JsonProcessingException {
        return objectMapper.readValue(content, Task.class);
    }

    private String toJSON(Object object) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, object);

        return outputStream.toString();
    }
}
