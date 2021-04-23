package ece448.iot_hub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            //logger.info("REST /api/plugs/"+plug+" - " + action);
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
        //logger.info("REST /api/plugs/");
        return ret;
    }

    private static final Logger logger = LoggerFactory.getLogger(PlugsResource.class);
}
