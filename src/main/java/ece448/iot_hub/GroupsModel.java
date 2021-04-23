package ece448.iot_hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GroupsModel {
    
    private final PlugsModel plugs;
    private final HashMap<String, ArrayList<String>> groups = new HashMap<>();

    public GroupsModel(PlugsModel plugs) {
        this.plugs = plugs;
    }

    synchronized public void publishAction(String groupName, String action) {
        for (String plugName : groups.get(groupName)) {
            plugs.publishAction(plugName, action);
        }
    }

    synchronized public void createGroup(String groupName) {
        createGroup(groupName, new ArrayList<String>());
    }

    synchronized public void createGroup(String groupName, ArrayList<String> plugsName) {
        groups.put(groupName, plugsName);
    }

    synchronized public void removeGroup(String groupName) {
        groups.remove(groupName);
    }

/*
    synchronized public void removePlugFromGroup(String groupName, String plugName) {
        groups.get(groupName).remove(plugName);
    }

    synchronized public void addPlugToGroup(String groupName, String plugName) {
        if (!groups.get(groupName).contains(plugName)) {
            groups.get(groupName).add(plugName);
        }
    }
*/

    synchronized public HashMap<String, Object> getGroup(String groupName) {
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("name", groupName);
        ArrayList<HashMap<String, Object>> members = new ArrayList<HashMap<String, Object>>();
        if (groups.get(groupName) != null){
            for (String plugName : groups.get(groupName)) {
                members.add(plugs.getPlug(plugName));
            }
        }
        ret.put("members", members);
        return ret;
    }

    synchronized public List<String> getGroupsNames() {
        return new ArrayList<String>(groups.keySet());
    }

    private static final Logger logger = LoggerFactory.getLogger(GroupsModel.class);
}
