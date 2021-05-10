package iot_hub;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class GroupsResource {
    
    private final GroupsModel groups;
    
    public GroupsResource(GroupsModel groups) {
        this.groups = groups;
    }

    @GetMapping("/api/groups/{group}")
    public Object getGroup(
        @PathVariable("group") String group,
        @RequestParam(value = "action", required = false) String action){
            if (action != null)
                groups.publishAction(group, action);
            Object ret = groups.getGroup(group);
            return ret;
    }

    @GetMapping("/api/groups")
    public Object getGroups(){
        ArrayList<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
        for (String groupName : groups.getGroupsNames()){ ret.add(groups.getGroup(groupName)); };
        return ret;
    }

    @PostMapping("/api/groups/{group}")
    public void createGroup(
        @PathVariable("group") String group,
        @RequestBody ArrayList<String> members) {
        logger.info("REST Create group " + group + ": [" + String.join(", " ,members) + "]");
        groups.createGroup(group, members);
    }

    @DeleteMapping("/api/groups/{group}")
    public void deleteGroup(@PathVariable("group") String group) {
        logger.info("REST Delete group " + group);
        groups.removeGroup(group);
    }

    private static final Logger logger = LoggerFactory.getLogger(GroupsResource.class);

}
