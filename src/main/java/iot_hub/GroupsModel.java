package iot_hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GroupsModel {
    
    private final PlugsModel plugs;
    private final DatabaseController databaseController;

    public GroupsModel(PlugsModel plugs, DatabaseController databaseController) {
        this.plugs = plugs;
        this.databaseController = databaseController;
    }

    synchronized public void publishAction(String groupName, String action) {
        for (String plugName : databaseController.getMembers(groupName)) {
            plugs.publishAction(plugName, action);
        }
    }

    synchronized public void createGroup(String groupName) {
        createGroup(groupName, new ArrayList<String>());
    }

    synchronized public void createGroup(String groupName, ArrayList<String> plugsName) {
        databaseController.createGroup(groupName, plugsName);
    }

    synchronized public void removeGroup(String groupName) {
        databaseController.removeGroup(groupName);
    }

    synchronized public HashMap<String, Object> getGroup(String groupName) {
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("name", groupName);
        ArrayList<HashMap<String, Object>> members = new ArrayList<HashMap<String, Object>>();
        for (String plugName : databaseController.getMembers(groupName)) {
            members.add(plugs.getPlug(plugName));
        }
        ret.put("members", members);
        return ret;
    }

    synchronized public List<String> getGroupsNames() {
        return databaseController.getGroups();
    }
}
