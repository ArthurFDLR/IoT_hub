package iot_hub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class PlugsResource {
    
    private final PlugsModel plugs;
    
    public PlugsResource(PlugsModel plugs) {
        this.plugs = plugs;
    }

    @GetMapping("/api/plugs/{plug:.+}")
    public Object getPlug(
        @PathVariable("plug") String plug,
        @RequestParam(value = "action", required = false) String action ){
            if (action != null){
                plugs.publishAction(plug, action);
            }
            return plugs.getPlug(plug);
    }

    @GetMapping("/api/plugs")
    public Object getPlugs(){
        List<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
        for (String plug : plugs.getNames()){
            ret.add(plugs.getPlug(plug));
        }
        return ret;
    }
}
