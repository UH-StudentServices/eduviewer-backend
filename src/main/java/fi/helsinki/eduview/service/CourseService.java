/*
 * This file is part of Eduviewer application.
 *
 * Eduviewer application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eduviewer application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Eduviewer application.  If not, see <http://www.gnu.org/licenses/>.
 */

package fi.helsinki.eduview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService extends AbstractDataService {

    private Logger logger = LogManager.getLogger(CourseService.class);

    private ObjectMapper mapper = new ObjectMapper();
    private List<JsonNode> cus = new ArrayList<>();

    protected JsonNode find(String id) throws Exception {
        return openFile(id);
    }

    private JsonNode openFile(String id) throws IOException {
        File file = new File(env.getProperty("data-location") + env.getProperty("course-units-dir") + "/" + id + ".json");
        if (file.isFile()) {
            return mapper.readTree(Files.readAllBytes(file.toPath()));
        }
        return mapper.createArrayNode();
    }

    public JsonNode getCUNameById(String id, String lv) throws Exception {
        JsonNode root = openFile(id);
        return filterResults(id, root, lv);
    }
}
