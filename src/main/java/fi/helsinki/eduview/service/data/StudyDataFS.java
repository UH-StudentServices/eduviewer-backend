package fi.helsinki.eduview.service.data;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: hpr
 * @date: 14/02/2019
 */
@Service
public class StudyDataFS implements StudyData {

    @Autowired protected Environment env;

    private static Logger logger = LoggerFactory.getLogger(StudyDataFS.class);
    private static ObjectMapper mapper = new ObjectMapper();

    private List<JsonNode> educations = new ArrayList<>();
    private List<JsonNode> modules = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String dataLocation = env.getProperty("data-location", "backup/");
        String educationsFN = env.getProperty("educations-dir", "kori-educations");
        String modulesFN = env.getProperty("modules-dir", "kori-modules");
        initAllFilesFromSameDirectory(mapper, dataLocation + educationsFN);
        initAllFilesFromSameDirectory(mapper, dataLocation + modulesFN);
    }

    private void initAllFilesFromSameDirectory(ObjectMapper mapper, String dir) throws IOException {
        if(!new File(dir).exists()) {
            logger.warn("directory: " + dir + " does not exist");
            return;
        }
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
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        }
        logger.info("init from " + dir + " done");
    }

    private void initToCorrectCollection(JsonNode childNode) {
        if (childNode.get("type").asText().toLowerCase().contains("education")) {
            educations.add(childNode);
        } else {
            modules.add(childNode);
        }
    }

    private JsonNode findFromAllById(String id) throws Exception {
        JsonNode node = findEducationById(id);
        if (node == null) {
            node = findModulesById(id);
        }
        return node;
    }


    @Override
    public JsonNode getById(String id) throws Exception {
        return findFromAllById(id);
    }

    @Override
    public JsonNode find(String id) throws Exception {
        return findNodesByGroupId(id, modules, true);
    }

    @Override
    public JsonNode getDegreeProgrammeNode(String code) {
        for (JsonNode module : modules) {
            if (module.has("code") && module.get("code").asText().toUpperCase().equals(code.toUpperCase())
                    && module.get("documentState").asText().equals("ACTIVE")) {
                return module;
            }
        }
        return null;
    }

    @Override
    public JsonNode findByGroupId(String groupId) throws Exception {
        JsonNode results = findNodesByGroupId(groupId, educations);
        if (results == null || results.size() == 0) {
            results = findNodesByGroupId(groupId, modules);
        }
        return results;
    }

    @Override
    public JsonNode findEducationById(String id) throws Exception {
        return findNodeById(id, educations);
    }

    @Override
    public JsonNode findModulesById(String id) throws Exception {
        return findNodeById(id, modules);
    }

    private JsonNode findNodeById(String id, List<JsonNode> root) throws Exception {
        for (JsonNode child : root) {
            if (child.get("id").asText().equals(id) && child.get("documentState").asText().equals("ACTIVE")) {
                return child;
            }
        }
        return null;
    }

    @Override
    public ArrayNode findModulesByGroupId(String groupId) {
        return findNodesByGroupId(groupId, modules);
    }

    private ArrayNode findNodesByGroupId(String id, List<JsonNode> nodeList) {
        return findNodesByGroupId(id, nodeList, false);
    }

    private ArrayNode findNodesByGroupId(String id, List<JsonNode> nodeList, boolean acceptAllStates) {
        ArrayNode array = mapper.createArrayNode();
        for (JsonNode child : nodeList) {
            if (child.get("groupId").asText().equals(id) && (acceptAllStates || child.get("documentState").asText().equals("ACTIVE"))) {
                array.add(child);
            }
        }
        return array;
    }


    @Override
    public List<JsonNode> getEducations() {
        List<JsonNode> filtered = new ArrayList<>();
        for(JsonNode edu : educations) {
            if (!edu.get("documentState").asText().equals("ACTIVE")) {
                continue;
            }
            filtered.add(edu);
        }
        return filtered;
    }

    public void setEducations(List<JsonNode> educations) {
        this.educations = educations;
    }
}
