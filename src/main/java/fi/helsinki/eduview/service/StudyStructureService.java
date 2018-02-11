package fi.helsinki.eduview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
public class StudyStructureService extends AbstractService {

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
            } else if(file.getName().contains("module")){
                root = modules;
            } else {
                continue;
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
        return filterResultsByLvAndPrint(array, null);
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

    public String getById(String id, String lv) throws Exception {
        JsonNode response = findFromAllById(id);
        if(response != null) {
            return filterResultsByLvAndPrint(response, lv);
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

    // note: traverse uses only rules and tries to play with ids instead of just working with groupids, needs to be fixed
    public String traverseTree(String id, boolean filterTree) throws Exception {
        ArrayNode resultNode = traverseTreeInternal(id);
        return filterTree ? filterResultsByLvAndPrint(resultNode, null) : mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);
    }

    private ArrayNode traverseTreeInternal(String id) throws Exception {
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
            List<String> newIds = handleNodeRuleTraverse(node);
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

        return resultNode;
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
                    newLocalIds = handleNodeRuleTraverse(mod);
                    order.addAll(order.indexOf(id) + 1, newLocalIds);
                    idsToCheck.remove(id);

                } else if(idsToCheck.contains(groupId)) {
                    results.add(mod);
                    found++;
                    newLocalIds = handleNodeRuleTraverse(mod);
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

    private List<String> handleNodeRuleTraverse(JsonNode node) {
        List<String> newIds = new ArrayList<>();
        if(!node.has("rule")) {
            return newIds;
        }
        newIds.addAll(readRules(node.get("rule")));
        return newIds;
    }

    private List<String> readRules(JsonNode ruleNode) {
        List<String> newIds = new ArrayList<>();
        if(ruleNode.has("moduleGroupId")) {
            newIds.add(ruleNode.get("moduleGroupId").asText());
        }
        if(ruleNode.has("rules")) {
            for (JsonNode rule : ruleNode.get("rules")) {
                newIds.addAll(readRules(rule));
            }
        }
        return newIds;
    }

    public String getByGroupId(String groupId) throws IOException {
        ArrayNode moduleResults = findByGroupId(groupId);
        return filterResultsByLvAndPrint(moduleResults, null);
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

    public String getByAllIds(List<String> idList, String lv) throws IOException {
        ArrayNode results = mapper.createArrayNode();
        for(String id : idList) {
            results.addAll(findByGroupId(id));
        }
        return filterResultsByLvAndPrint(results, lv);
    }

    public String getAvailableLVs(String id) throws Exception {
        ArrayNode tree = traverseTreeInternal(id);
        ArrayNode node = mapper.createArrayNode();
        Set<String> lvs = new TreeSet<>();
        for(JsonNode mod : tree) {
            if(mod.has("curriculumPeriodIds")) {
                for(JsonNode lv : mod.get("curriculumPeriodIds")) {
                    lvs.add(lv.asText());
                }
            }
        }
        for(String lv : lvs) {
            node.add(new TextNode(lv));
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    public String getAvailableLVs() throws JsonProcessingException {
        ArrayNode node = mapper.createArrayNode();
        Set<String> lvs = new TreeSet<>();
        for(JsonNode mod : modules) {
            if(mod.has("curriculumPeriodIds")) {
                for(JsonNode lv : mod.get("curriculumPeriodIds")) {
                    lvs.add(lv.asText());
                }
            }
        }
        for(String lv : lvs) {
            node.add(new TextNode(lv));
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }
}
