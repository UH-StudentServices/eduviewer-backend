package fi.helsinki.eduview.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public String getNodesById(String id) throws Exception {
        ArrayNode array = mapper.createArrayNode();
        JsonNode subNode = findFromAllById(id);
        if(subNode != null) {
            array.add(subNode);
        }
//        subNode = getByGroupId(id);
//        if(subNode != null) {
//            array.addAll((ArrayNode)subNode);
//        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(array);
    }

    public String getEducations() throws IOException {
        ObjectNode wrapper = mapper.createObjectNode();
        ArrayNode array = mapper.createArrayNode();
        for(JsonNode node : educations) {
            if(node.get("documentState").asText().equals("ACTIVE")) {
                array.add(node);
            }
        }
        wrapper.putArray("educations").addAll(array);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrapper);
    }

    public String getById(String id) throws Exception {
        JsonNode response = findFromAllById(id);
        if(response != null) {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        }
        return null;
    }

    private JsonNode findFromAllById(String id) throws Exception {
        JsonNode node = findNodeById(id, educations);
        if(node == null) {
            node = findNodeById(id, modules);
        }
        return node;
    }

    private ArrayNode findNodeByGroupId(String id, List<JsonNode> nodeList) {
        ArrayNode array = mapper.createArrayNode();
        for(JsonNode child : nodeList) {
            if (child.get("groupId").asText().equals(id) && child.get("documentState").asText().equals("ACTIVE")) {
                array.add(child);
            }
        }
        return array;
    }

    private JsonNode findNodeById(String id, List<JsonNode> root) throws Exception {
        for(JsonNode child : root) {
            if(child.get("id").asText().equals(id) && child.get("documentState").asText().equals("ACTIVE")) {
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

        List<JsonNode> notActive = new ArrayList<>();
        for(JsonNode sub : results) {
            if(!sub.get("documentState").asText().equals("ACTIVE")) {
                notActive.add(sub);
            }
        }
        results.removeAll(notActive);

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

        while(!idsToCheck.isEmpty()) {
            int found = 0;
            List<String> newIds = new ArrayList<>();
            for(JsonNode mod : array) {
                if(results.contains(mod)) {
                    continue;
                }
                List<String> newLocalIds = new ArrayList<>();
                String id = mod.get("id").asText();
                String groupId = mod.get("groupId").asText();

                if(idsToCheck.contains(id)) {
                    results.add(mod);
                    found++;
                    newLocalIds = handleNodeTraverse(mod);
                    order.addAll(order.indexOf(id) + 1, newLocalIds);
                    idsToCheck.remove(id);

                } else if(idsToCheck.contains(groupId)) {
                    results.add(mod);
                    found++;
                    newLocalIds = handleNodeTraverse(mod);
                    order.add(order.indexOf(groupId)+1, id);
                    order.addAll(order.indexOf(id)+1, newLocalIds);
                }
                if(!newLocalIds.isEmpty()) {
                    newIds.addAll(newLocalIds);
                }
            }
            if(found == 0) {
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

    public String getByGroupId(String groupId) throws JsonProcessingException {
        ArrayNode moduleResults = findByGroupId(groupId);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(moduleResults);
    }

    private ArrayNode findByGroupId(String groupId) {
        ArrayNode node = mapper.createArrayNode();
//        ArrayNode results = findNodeByGroupId(groupId, educations);
//        if(results != null) {
//            node.addAll(results);
//        }
        ArrayNode moduleResults = findNodeByGroupId(groupId, modules);
        if(moduleResults != null) {
            node.addAll(moduleResults);
        }
        return moduleResults;
    }

    public String getByAllIds(List<String> idList) throws JsonProcessingException {
        ArrayNode results = mapper.createArrayNode();
        for(String id : idList) {
            results.addAll(findByGroupId(id));
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);
    }
}
