package fi.helsinki.eduview.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * @author: hpr
 * @date: 04/01/2018
 */
@Service
public class JsonService {

    private boolean init = false;
    private ObjectMapper mapper = new ObjectMapper();
    private List<JsonNode> educations = new ArrayList<>();
    private List<JsonNode> modules = new ArrayList<>();

    @PostConstruct
    private void init() throws IOException {
        if(init) {
           return;
        }
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> root;
        for(File file : new File("test/").listFiles()) {
            if(file.getName().contains("educations")) {
                root = educations;
            } else {
                root = modules;
            }
            ArrayNode fileTree = (ArrayNode)mapper.readTree(Files.readAllBytes(file.toPath()));
            for(JsonNode child : fileTree) {
                root.add(child);
            }
        }
        init = true;
    }

    public String getEducations() throws IOException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(educations);
    }

    public String getById(String id) throws Exception {
        JsonNode node = findNodeById(id, educations);
        if(node == null) {
            node = findNodeById(id, modules);
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    private JsonNode findNodeByGroupId(String id, JsonNode root) {
        ArrayNode array = mapper.createArrayNode();
        for(JsonNode child : root) {
            if (child.get("groupId").asText().equals(id)) {
                array.add(child);
            }
        }
        return array;
    }

    private JsonNode findNodeById(String id, List<JsonNode> root) throws Exception {
        for(JsonNode child : root) {
            if(child.get("id").asText().equals(id)) {
                return child;
            }
        }
        return null;
    }

    public String traverseTree(String id) throws Exception {
        Set<String> unfetchedIds = new TreeSet<>();
        List<JsonNode> results = new ArrayList<>();
        final List<String> order = new ArrayList<>();

        JsonNode node = findNodeById(id, educations);
        if(node == null) {
            node = findNodeById(id, modules);
        }
        if(node != null) {
            results.add(node);
            order.add(id);
            List<String> newIds = handleNodeTraverse(node);
            order.addAll(order.indexOf(id)+1, newIds);
            unfetchedIds.addAll(newIds);
            traverse(results, modules, unfetchedIds, order);
        }
        Collections.sort(results, new Comparator<JsonNode>() {
            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                return order.indexOf(o1.get("id").asText()) - order.indexOf(o2.get("id").asText());
            }
        });
        ArrayNode resultNode = mapper.createArrayNode();
        resultNode.addAll(results);
        return mapper.writeValueAsString(resultNode);
    }

    private void traverse(List<JsonNode> results, List<JsonNode> array, Set<String> idsToCheck, List<String> order) {
        Set<String> knownGroups = new HashSet<>();

        while(!idsToCheck.isEmpty()) {
            List<String> found = new ArrayList<>();
            List<String> newIds = new ArrayList<>();
            for(JsonNode mod : array) {
                if(results.contains(mod)) {
                    continue;
                }
                List<String> newLocalIds = new ArrayList<>();
                String id = mod.get("id").asText();
                String groupId = mod.get("groupId").asText();

                if(idsToCheck.contains(id)) {
                    found.add(id);
                    results.add(mod);
                    newLocalIds = handleNodeTraverse(mod);
                    order.addAll(order.indexOf(id) + 1, newLocalIds);
                    idsToCheck.remove(id);

                } else if(idsToCheck.contains(groupId)) {
                    knownGroups.add(groupId);
                    found.add(groupId);
                    results.add(mod);
                    newLocalIds = handleNodeTraverse(mod);
                    order.add(order.indexOf(groupId)+1, id);
                    order.addAll(order.indexOf(id)+1, newLocalIds);
                }
                if(!newLocalIds.isEmpty()) {
                    newIds.addAll(newLocalIds);
                }
            }
            if(newIds.isEmpty() && knownGroups.equals(idsToCheck) && found.isEmpty()) {
                break;
            } else {
                idsToCheck.addAll(newIds);
            }
        }
    }

    private List<String> handleNodeTraverse(JsonNode node) {
        List<String> newIds = new ArrayList<>();
        if(!node.has("rule") || !node.get("rule").has("rules")) {
            return newIds;
        }
        for(JsonNode rule : node.get("rule").get("rules")) {
            if(rule.has("moduleGroupId")) {
                newIds.add(rule.get("moduleGroupId").asText());
            }
        }
        return newIds;
    }

    public String getByGroupId(String groupId) {
        return null;
    }
}
