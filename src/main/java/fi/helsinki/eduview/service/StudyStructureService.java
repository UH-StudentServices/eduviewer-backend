package fi.helsinki.eduview.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * @author: Hannu-Pekka Rajaniemi (h-p@iki.fi)
 * @date: 04/01/2018
 */
@Service
public class StudyStructureService extends AbstractService {

    private Logger logger = Logger.getLogger(StudyStructureService.class);

    @Autowired private CourseService courseService;

    private boolean init = false;
    private boolean filteringEnabled = true;
    private ObjectMapper mapper = new ObjectMapper();
    private List<JsonNode> educations = new ArrayList<>();
    private List<JsonNode> modules = new ArrayList<>();
    private List<String> whitelisted = Arrays.asList("name", "id", "groupId", "rule", "code", "credits");

    @PostConstruct
    public void init() throws IOException {
        if(init) {
           return;
        }
        ObjectMapper mapper = new ObjectMapper();
        String dataLocation = env.getProperty("data-location", "backup/");
        String educationsFN = env.getProperty("educations-dir", "kori-educations");
        String modulesFN = env.getProperty("modules-dir", "kori-modules");
//        String degreeProgrammesFN = env.getProperty("degree-programmes-dir", "kori-degree-programmes");
//        initFiles(mapper, dataLocation + educationsFN, educations);
//        initFiles(mapper, dataLocation + modulesFN, modules);
//        initFiles(mapper, dataLocation + degreeProgrammesFN, modules);
        initAllFilesFromSameDirectory(mapper, dataLocation + educationsFN);
        initAllFilesFromSameDirectory(mapper, dataLocation + modulesFN);
        init = true;
    }

    private void initAllFilesFromSameDirectory(ObjectMapper mapper, String dir) throws IOException {
        for (File file : new File(dir).listFiles()) {
            try {
                JsonNode root = mapper.readTree(Files.readAllBytes(file.toPath()));
                if (root.isArray()) {
                    for (JsonNode childNode : root) {
                        initToCorrectCollection(childNode);
                    }
                } else {
                    initToCorrectCollection(root);
                }
            }
            catch(JsonParseException e) {
                e.printStackTrace();
            }
        }
        logger.info("init from " + dir + " done");
    }

    private void initToCorrectCollection(JsonNode childNode) {
        if(childNode.get("type").asText().toLowerCase().contains("education")) {
            educations.add(childNode);
        } else {
            modules.add(childNode);
        }
    }


    public String getNodesById(String id) throws Exception {
        ArrayNode array = mapper.createArrayNode();
        JsonNode subNode = findFromAllById(id);
        if(subNode != null) {
            array.add(subNode);
        }
        return filterResultsByLvAndPrint(array, null);
    }

    public String getEducations() throws IOException {
        ObjectNode wrapper = mapper.createObjectNode();
        List<JsonNode> array = new ArrayList<>();
        for(JsonNode node : educations) {
            if(node.get("documentState").asText().equals("ACTIVE")) {
                array.add(node);
            }
        }
        array.sort(new Comparator<JsonNode>() {
            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                return o1.get("name").get("fi").asText().toLowerCase().compareTo(o2.get("name").get("fi").asText().toLowerCase());
            }
        });
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

    public String getByGroupId(String groupId) throws IOException {
        ArrayNode moduleResults = findByGroupId(groupId);
        return filterResultsByLvAndPrint(moduleResults, null);
    }

    private JsonNode findByGroupIdAndFilter(String groupId, String lv) throws Exception {
        JsonNode results = findNodeByGroupId(groupId, educations);
        if(results == null || results.size() == 0) {
            results = findNodeByGroupId(groupId, modules);
        }
        JsonNode filtered = filterResults(results, lv);
        if(filtered.size() > 1) {
            logger.warn("uh oh, returning multiple values for " + groupId + " + and " + lv);
        }
        return filtered.get(0);
    }

    private ArrayNode findByGroupId(String groupId) {
        ArrayNode node = mapper.createArrayNode();
        ArrayNode moduleResults = findNodeByGroupId(groupId, modules);
        if(moduleResults != null) {
            node.addAll(moduleResults);
        }
        return moduleResults;
    }

    public String getByAllIds(List<String> idList, String lv) throws Exception {
        ArrayNode results = mapper.createArrayNode();
        for(String id : idList) {
            JsonNode node = findFromAllById(id);
            if(node != null) {
                results.add(node);
            } else {
                results.addAll(findByGroupId(id));
            }
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

    public String getTree(String id, String lv) throws Exception {
        JsonNode node = findByGroupIdAndFilter(id, lv);
        if(node == null) {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.createObjectNode());
        }
        if(node.get("type").asText().equals("Education")) {
            return getTreeFromEducation(node, lv);
        } else {
            return getTreeFromModule(node, lv);
        }

    }

    private JsonNode findNodeByIdFiltered(String id, String lv) throws Exception {
        JsonNode node = findFromAllById(id);
        if(node == null) {
            return mapper.createObjectNode();
        }
        return filterResults(node, lv);
    }

    private String getTreeFromModule(JsonNode node, String lv) throws Exception {
        traverseModule(node, lv);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    private String getTreeFromEducation(JsonNode node, String lv) throws Exception {
        JsonNode lowerDegree = node.get("structure").get("phase1").get("options").get(0);
        JsonNode firstModule = findByGroupIdAndFilter(lowerDegree.get("moduleGroupId").asText(), lv);
        if(firstModule != null) {
            traverseModule(firstModule, lv);
        } else {
            firstModule = mapper.createObjectNode();
        }
        addDataNode((ObjectNode)node, firstModule);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    private void traverseModule(JsonNode node, String lv) throws Exception {
        JsonNode ruleNode = node.get("rule");
        if(ruleNode != null) {
            handleRule(ruleNode, lv);
        }
    }

    private void handleRule(JsonNode ruleNode, String lv) throws Exception {
        String type = ruleNode.get("type").asText();
        switch(type) {
            case "CompositeRule":
                handleCompositeRule(ruleNode, lv);
                break;
            case "CreditsRule":
                handleCreditRule(ruleNode, lv);
                break;
            case "ModuleRule":
                handleModuleRule(ruleNode, lv);
                break;
            case "CourseUnitRule":
                handleCourseUnitRule(ruleNode, lv);
                break;
            case "AnyCourseUnitRule":
                handleAnyCourseUnitRule(ruleNode, lv);
                break;
            case "AnyModuleRule":
                handleAnyModuleRule(ruleNode, lv);
                break;
            default:
                logger.warn("encountered new role type: " + type + " + for " + ruleNode.get("localId").asText());
        }

    }

    private void handleCourseUnitRule(JsonNode ruleNode, String lv) throws IOException {
        String groupId = ruleNode.get("courseUnitGroupId").asText();
        JsonNode node = courseService.getCUNameById(groupId, lv);
        if(node == null) {
            logger.warn("could not find course unit with group id " + groupId + " / lv " + lv);
            return;
        }
        addDataNode((ObjectNode) ruleNode, node);
    }

    private void handleAnyCourseUnitRule(JsonNode ruleNode, String lv) {
        ObjectNode textNode = mapper.createObjectNode();
        textNode.set("name", new TextNode("anyCourseUnitRule"));
        addDataNode((ObjectNode) ruleNode, textNode);
    }

    private void handleAnyModuleRule(JsonNode ruleNode, String lv) {
        ObjectNode textNode = mapper.createObjectNode();
        textNode.set("name", new TextNode("anyModuleRule"));
        addDataNode((ObjectNode) ruleNode, textNode);
    }

    private void handleCompositeRule(JsonNode ruleNode, String lv) throws Exception {
        for(JsonNode subRule : ruleNode.get("rules")) {
            handleRule(subRule, lv);
        }
    }

    private void handleCreditRule(JsonNode ruleNode, String lv) throws Exception {
        handleRule(ruleNode.get("rule"), lv);
    }

    private void handleModuleRule(JsonNode ruleNode, String lv) throws Exception {
        String moduleGroupId = ruleNode.get("moduleGroupId").asText();
        JsonNode node = findByGroupIdAndFilter(moduleGroupId, lv);

        if(node == null) {
            logger.warn("moduleGroupId " + moduleGroupId + " / " + lv + " is missing");
            return;
        }
        traverseModule(node, lv);
        addDataNode((ObjectNode) ruleNode, node);
    }

    private void addDataNode(ObjectNode ruleNode, JsonNode node) {
        if(!filteringEnabled) {
            ruleNode.set("dataNode", node);
            return;
        }
        ObjectNode filtered = mapper.createObjectNode();
        ObjectNode original = (ObjectNode)node;
        if(original == null) {
            System.out.println("rip");
        }
        Iterator<String> fieldNames = original.fieldNames();
        while(fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if(whitelisted.contains(fieldName)) {
                filtered.set(fieldName, original.get(fieldName));
            }
        }
        ruleNode.set("dataNode", filtered);
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
}
