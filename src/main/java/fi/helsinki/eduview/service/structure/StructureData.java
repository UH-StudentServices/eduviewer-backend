package fi.helsinki.eduview.service.structure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.List;

/**
 * @author: hpr
 * @date: 19/02/2019
 */
public interface StructureData {
    JsonNode getById(String id) throws Exception;

    JsonNode find(String id) throws Exception;

    JsonNode getDegreeProgrammeNode(String code) throws IOException;

    JsonNode findByGroupId(String groupId) throws Exception;

    JsonNode findEducationById(String id) throws Exception;

    JsonNode findModulesById(String id) throws Exception;

    ArrayNode findModulesByGroupId(String groupId) throws IOException;

    List<JsonNode> getEducations() throws IOException;
}
