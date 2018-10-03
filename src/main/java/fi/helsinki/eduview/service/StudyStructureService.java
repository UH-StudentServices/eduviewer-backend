package fi.helsinki.eduview.service;

import com.fasterxml.jackson.core.JsonParseException;
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
    private List<String> whitelisted = Arrays.asList("name", "id", "groupId", "rule", "code", "credits", "targetCredits");

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

    public String getById(String id, String lv) throws Exception {
        JsonNode response = findFromAllById(id);
        if(response != null) {
            return filterResultsByLvAndPrint(id, response, lv);
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

    private void initToCorrectCollection(JsonNode childNode) {
        if(childNode.get("type").asText().toLowerCase().contains("education")) {
            educations.add(childNode);
        } else {
            modules.add(childNode);
        }
    }

    public String getEducationsWithDegreeProgrammeCodes() throws Exception {
        ArrayNode arrayNode = mapper.createArrayNode();
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
        for(JsonNode edu : array) {
            JsonNode lowerDegree = edu.get("structure").get("phase1").get("options").get(0);
            ArrayNode results = findNodesByGroupId(lowerDegree.get("moduleGroupId").asText(), modules);
            String code = null;
            for(JsonNode node : results) {
                if(node.has("code")) {
                    code = node.get("code").asText();
                    break;
                }
            }
            ((ObjectNode)edu).set("degreeProgrammeCode", new TextNode(code));
        }
        arrayNode.addAll(array);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
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

    private ArrayNode findNodesByGroupId(String id, List<JsonNode> nodeList) {
        return findNodesByGroupId(id, nodeList, false);
    }

    private ArrayNode findNodesByGroupId(String id, List<JsonNode> nodeList, boolean acceptAllStates) {
        ArrayNode array = mapper.createArrayNode();
        for(JsonNode child : nodeList) {
            if (child.get("groupId").asText().equals(id) && (acceptAllStates || child.get("documentState").asText().equals("ACTIVE"))) {
                array.add(child);
            }
        }
        return array;
    }

    protected JsonNode find(String id) throws Exception {
        return findNodesByGroupId(id, modules, true);
    }

    private JsonNode findByGroupIdAndFilter(String groupId, String lv) throws Exception {
        JsonNode results = findNodesByGroupId(groupId, educations);
        if(results == null || results.size() == 0) {
            results = findNodesByGroupId(groupId, modules);
        }
        return filterResults(groupId, results, lv);
    }

    private JsonNode findNodeById(String id, List<JsonNode> root) throws Exception {
        for(JsonNode child : root) {
            if(child.get("id").asText().equals(id) && child.get("documentState").asText().equals("ACTIVE")) {
                return child;
            }
        }
        return null;
    }

    public String getAvailableLVsByDPCode(String degreeProgrammeCode) throws Exception {
        JsonNode node = getDegreeProgrammeNode(degreeProgrammeCode);
        if(node != null) {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node.get("curriculumPeriodIds"));
        }
        return "[]";
    }


    public String getAvailableLVs(String id) throws Exception {
        JsonNode education = findNodeById(id, educations);
        JsonNode phase1 = getStudyPhase1(education);
        String dpId = phase1.get("moduleGroupId").asText();
        JsonNode degreeProgrammes = findNodesByGroupId(dpId, modules);

        Set<String> lvs = new TreeSet<>();
        ArrayNode node = mapper.createArrayNode();

        for(JsonNode dp : degreeProgrammes) {
            if(dp.has("curriculumPeriodIds")) {
                for(JsonNode lv : dp.get("curriculumPeriodIds")) {
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

    private void handleCourseUnitRule(JsonNode ruleNode, String lv) throws Exception {
        String groupId = ruleNode.get("courseUnitGroupId").asText();
        JsonNode node = courseService.getCUNameById(groupId, lv);
        if(node == null) {
            logger.warn("could not find course unit with group id " + groupId + " / lv " + lv);
            return;
        }
        addDataNode((ObjectNode) ruleNode, node);
    }

    private void handleAnyCourseUnitRule(JsonNode ruleNode, String lv) throws Exception {
        ObjectNode textNode = mapper.createObjectNode();
        textNode.set("name", new TextNode("anyCourseUnitRule"));
        addDataNode((ObjectNode) ruleNode, textNode);
    }

    private void handleAnyModuleRule(JsonNode ruleNode, String lv) throws Exception {
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
            logger.warn("moduleGroupId " + moduleGroupId + " / " + lv + " is structuralNotActive");
            return;
        }
        traverseModule(node, lv);
        addDataNode((ObjectNode) ruleNode, node);
    }

    private void addDataNode(ObjectNode ruleNode, JsonNode node) throws Exception {
        if(!filteringEnabled) {
            ruleNode.set("dataNode", node);
            return;
        }
        ObjectNode filtered = mapper.createObjectNode();
        ObjectNode original = (ObjectNode)node;
        if(original == null) {
            logger.error("original node is null for ruleNode " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ruleNode));
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

    private JsonNode getEducationByCode(String code) throws Exception {
        JsonNode degreeProgramme = getDegreeProgrammeNode(code);

        if(degreeProgramme == null) {
            return null;
        }

        String groupId = degreeProgramme.get("groupId").asText();
        for(JsonNode education : educations) {
            JsonNode phase1 = getStudyPhase1(education);
            if(phase1 != null && phase1.get("moduleGroupId").asText().equals(groupId)) {
                return education;
            }
        }
        return null;
    }

    private JsonNode getDegreeProgrammeNode(String code) {
        for(JsonNode module : modules) {
            if(module.has("code") && module.get("code").asText().toUpperCase().equals(code.toUpperCase())) {
                return module;
            }
        }
        return null;
    }

    public String getTreeByCode(String code, String lv) throws Exception {
        JsonNode ed = getEducationByCode(code);
        if(ed == null) {
            return "{}";
        }
        return getTree(ed.get("groupId").asText(), lv);
    }

    private JsonNode getStudyPhase1(JsonNode node) {
        JsonNode phase1 = node.get("structure").get("phase1");
        if(phase1 == null) {
            return null;
        }
        return phase1.get("options").get(0);
    }

    public String dataCheck(String lv) throws Exception {
        try {
            dataCheck = true;
            JsonNode educations = mapper.readTree(getEducations()).get("educations");
            for (JsonNode education : educations) {
                logger.info("processing " + education.get("code").asText());
                educationName = education.get("name").get("fi").asText();
                getTree(education.get("groupId").asText(), lv);
            }
            return "RAKENTEEN DUPLIKAATIT:" + String.join("\r\n", structuralDuplicates) + "\r\n\r\nRAKENTEEN VIRHETILAT:\r\n" + String.join("\r\n", structuralNotActive)
                                                            + "\r\n\r\nVIRHETILAISET OPINTOJAKSOT (draft, deleted, pallo puuttuu kokonaan):\r\n" + String.join("\r\n", missingCU);
        } finally {
            educationName = null;
            dataCheck = false;
            structuralDuplicates.clear();
            structuralNotActive.clear();
            missingCU.clear();
        }
    }
}
